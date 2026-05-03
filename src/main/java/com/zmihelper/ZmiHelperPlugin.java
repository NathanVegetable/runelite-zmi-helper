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

	private boolean lastPouchState;
	private boolean lastRunEnergyState;
	private boolean lastPrayerState;
	private boolean loginFlag = false;
	private boolean lastAltarVisible = true;
	private int cachedPlayerPlane = -1;

	private static final int CHAOS_ALTAR_ID = 34571;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(pouchReminder);
		overlayManager.add(runEnergyReminder);
		overlayManager.add(highlightOverlay);
		overlayManager.add(altarOverlay);

		if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN)
		{
			loginFlag = true;
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
				loginFlag = true;
				break;
			case LOGGED_IN:
				loginFlag = true;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		highlightOverlay.onTick();

		// Cache player plane for this tick
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

			if (shouldAlert && !lastRunEnergyState && !loginFlag)
			{
				notifier.notify(config.runEnergyNotification(), "Run energy low - cast Vile Vigour");
				lastRunEnergyState = true;
			}
			else if (!shouldAlert)
			{
				lastRunEnergyState = false;
			}
		}
		else
		{
			runEnergyLow = false;
			lastRunEnergyState = false;
		}

		// Check if altar just became visible and notify if conditions are met
		boolean altarCurrentlyVisible = isAltarVisibleOnSamePlane();

		if (!lastAltarVisible && altarCurrentlyVisible && !loginFlag)
		{
			if (pouchNeedsRepair && !lastPouchState)
			{
				notifier.notify(config.pouchNotification(), "Pouch needs repair — cast NPC Contact!");
				lastPouchState = true;
			}
			if (runEnergyLow && !lastRunEnergyState)
			{
				notifier.notify(config.runEnergyNotification(), "Run energy low - cast Vile Vigour");
				lastRunEnergyState = true;
			}
		}
		lastAltarVisible = altarCurrentlyVisible;

		loginFlag = false;
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

			// Check if already degraded OR at 1 charge left before next degradation
			boolean isDegraded = isDegradedPouch(item.getId());
			boolean isAt1Charge = pouch.getDegradation != null && isPouchAt1Charge(pouch);

			if (isDegraded || isAt1Charge)
			{
				if (!lastPouchState && !loginFlag)
				{
					notifier.notify(config.pouchNotification(), "Pouch needs repair — cast NPC Contact!");
					lastPouchState = true;
				}
				pouchNeedsRepair = true;
				return;
			}
		}

		// No degraded or critical pouch found
		if (lastPouchState)
		{
			lastPouchState = false;
		}
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

				if (shouldAlert && !lastPrayerState)
				{
					lastPrayerState = true;
				}
				else if (!shouldAlert && lastPrayerState)
				{
					lastPrayerState = false;
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

		// Use cached player plane if available, otherwise look it up
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
