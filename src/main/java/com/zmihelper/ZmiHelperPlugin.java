package com.zmihelper;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "ZMI Helper",
	tags = {"zmi", "ourania", "runecraft", "rc", "pouch"}
)
public class ZmiHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ZmiHelperConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ZmiHelperPouchReminder pouchReminder;

	@Inject
	private ZmiHelperRunEnergyReminder runEnergyReminder;

	@Inject
	private ZmiHelperHighlightOverlay highlightOverlay;

	@Inject
	private ZmiHelperAltarOverlay altarOverlay;

	int currentPrayer = 0;
	int currentRunEnergy = 0;
	boolean pouchNeedsRepair = false;
	boolean runEnergyLow = false;
	GameObject chaosAltar = null;

	private boolean pouchNotificationSent;
	private boolean runEnergyNotificationSent;
	private boolean prayerLowTracked;
	private boolean suppressNextNotifications = false;
	private boolean lastAltarVisible = true;
	private int cachedPlayerPlane = -1;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(pouchReminder);
		overlayManager.add(runEnergyReminder);
		overlayManager.add(highlightOverlay);
		overlayManager.add(altarOverlay);

		if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN)
		{
			suppressNextNotifications = true;
		}

		log.debug("ZMI Helper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(pouchReminder);
		overlayManager.remove(runEnergyReminder);
		overlayManager.remove(highlightOverlay);
		overlayManager.remove(altarOverlay);
		log.debug("ZMI Helper stopped!");
	}

	@Subscribe
	public void onGameStateChanged(net.runelite.api.events.GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGING_IN:
			case HOPPING:
			case CONNECTION_LOST:
			case LOGGED_IN:
				suppressNextNotifications = true;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		highlightOverlay.onTick();

		if (client.getLocalPlayer() != null)
		{
			cachedPlayerPlane = client.getLocalPlayer().getWorldLocation().getPlane();
		}

		currentRunEnergy = client.getEnergy();

		if (config.runEnergyThreshold() > 0)
		{
			int threshold = config.runEnergyThreshold() * 100;
			boolean shouldAlert = currentRunEnergy < threshold;
			runEnergyLow = shouldAlert;

			if (shouldAlert && !runEnergyNotificationSent && !suppressNextNotifications)
			{
				notifier.notify(config.runEnergyNotification(), "Run energy low - cast Vile Vigour");
				runEnergyNotificationSent = true;
			}
			else if (!shouldAlert)
			{
				runEnergyNotificationSent = false;
			}
		}
		else
		{
			runEnergyLow = false;
			runEnergyNotificationSent = false;
		}

		boolean altarCurrentlyVisible = isAltarVisibleOnSamePlane();

		if (!lastAltarVisible && altarCurrentlyVisible && !suppressNextNotifications)
		{
			if (pouchNeedsRepair && !pouchNotificationSent)
			{
				notifier.notify(config.pouchNotification(), "Pouch needs repair — cast NPC Contact!");
				pouchNotificationSent = true;
			}
			if (runEnergyLow && !runEnergyNotificationSent)
			{
				notifier.notify(config.runEnergyNotification(), "Run energy low - cast Vile Vigour");
				runEnergyNotificationSent = true;
			}
		}
		lastAltarVisible = altarCurrentlyVisible;

		suppressNextNotifications = false;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
		{
			return;
		}

		checkPouchState();
	}

	private void checkPouchState()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			pouchNeedsRepair = false;
			return;
		}

		for (Item item : inventory.getItems())
		{
			if (item.getId() == -1)
			{
				continue;
			}

			EssPouch pouch = getEssPouchFromItemId(item.getId());
			if (pouch == null)
			{
				continue;
			}

			boolean isDegraded = isDegradedPouch(item.getId());
			boolean isAt1Charge = pouch.getDegradation != null && isPouchAt1Charge(pouch);

			if (isDegraded || isAt1Charge)
			{
				if (!pouchNotificationSent && !suppressNextNotifications)
				{
					notifier.notify(config.pouchNotification(), "Pouch needs repair — cast NPC Contact!");
					pouchNotificationSent = true;
				}
				pouchNeedsRepair = true;
				return;
			}
		}

		pouchNotificationSent = false;
		pouchNeedsRepair = false;
	}

	private boolean isPouchAt1Charge(EssPouch pouch)
	{
		int breakpoint = pouch.nextDegradationBreakpoint(client);
		int remDura = breakpoint - pouch.getDegradation(client);
		int remEss = (pouch == EssPouch.COLOSSAL) ? remDura : durabilityToEssence(remDura);
		int limit = pouch.maxAmount(client);
		int remFills = (remEss + limit - 1) / limit;

		return remFills <= 1;
	}

	private int durabilityToEssence(int remainingDurability)
	{
		// Copied from EssencePouchOverlay
		return (int) Math.ceil(0.4 * Math.pow(remainingDurability, 1.07));
	}

	private EssPouch getEssPouchFromItemId(int itemId)
	{
		switch (itemId)
		{
			case ItemID.RCU_POUCH_SMALL:
				return EssPouch.SMALL;
			case ItemID.RCU_POUCH_MEDIUM:
			case ItemID.RCU_POUCH_MEDIUM_DEGRADE:
				return EssPouch.MEDIUM;
			case ItemID.RCU_POUCH_LARGE:
			case ItemID.RCU_POUCH_LARGE_DEGRADE:
				return EssPouch.LARGE;
			case ItemID.RCU_POUCH_GIANT:
			case ItemID.RCU_POUCH_GIANT_DEGRADE:
				return EssPouch.GIANT;
			case ItemID.RCU_POUCH_COLOSSAL:
			case ItemID.RCU_POUCH_COLOSSAL_DEGRADE:
				return EssPouch.COLOSSAL;
			default:
				return null;
		}
	}

	private boolean isDegradedPouch(int itemId)
	{
		return itemId == ItemID.RCU_POUCH_MEDIUM_DEGRADE
			|| itemId == ItemID.RCU_POUCH_LARGE_DEGRADE
			|| itemId == ItemID.RCU_POUCH_GIANT_DEGRADE
			|| itemId == ItemID.RCU_POUCH_COLOSSAL_DEGRADE;
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() == Skill.PRAYER)
		{
			currentPrayer = event.getBoostedLevel();

			if (config.prayerThreshold() > 0)
			{
				boolean shouldAlert = currentPrayer < config.prayerThreshold();

				if (shouldAlert && !prayerLowTracked)
				{
					prayerLowTracked = true;
				}
				else if (!shouldAlert && prayerLowTracked)
				{
					prayerLowTracked = false;
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject == null)
		{
			return;
		}

		ObjectComposition composition = client.getObjectDefinition(gameObject.getId());
		if (composition != null && "Chaos altar".equals(composition.getName()))
		{
			chaosAltar = gameObject;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject == chaosAltar)
		{
			chaosAltar = null;
		}
	}

	boolean isAltarVisibleOnSamePlane()
	{
		if (chaosAltar == null)
		{
			return false;
		}

		int playerPlane = cachedPlayerPlane;
		if (playerPlane == -1 && client.getLocalPlayer() != null)
		{
			playerPlane = client.getLocalPlayer().getWorldLocation().getPlane();
		}

		return playerPlane == chaosAltar.getWorldLocation().getPlane();
	}

	@Provides
	ZmiHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZmiHelperConfig.class);
	}
}
