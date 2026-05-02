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
	name = "ZMI Helper"
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
	private ZmiHelperWidgetOverlay widgetOverlay;

	@Inject
	private ZmiHelperAltarOverlay altarOverlay;

	@Getter(AccessLevel.PACKAGE)
	private int currentPrayer;

	@Getter(AccessLevel.PACKAGE)
	private int currentRunEnergy;

	@Getter(AccessLevel.PACKAGE)
	private boolean pouchNeedsRepair;

	@Getter(AccessLevel.PACKAGE)
	private boolean runEnergyLow;

	@Getter(AccessLevel.PACKAGE)
	private GameObject chaosAltar;

	private boolean lastPouchState;
	private boolean lastRunEnergyState;
	private boolean lastPrayerState;

	private static final int CHAOS_ALTAR_ID = 34571;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(widgetOverlay);
		overlayManager.add(altarOverlay);
		log.debug("ZMI Helper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(widgetOverlay);
		overlayManager.remove(altarOverlay);
		log.debug("ZMI Helper stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		widgetOverlay.onTick();

		currentRunEnergy = client.getEnergy();

		if (config.runEnergyThreshold() > 0)
		{
			int threshold = config.runEnergyThreshold() * 100;
			boolean shouldAlert = currentRunEnergy < threshold;

			if (shouldAlert && !lastRunEnergyState)
			{
				if (config.enableRunEnergyNotification())
				{
					notifier.notify("Run energy low!");
				}
			}
			lastRunEnergyState = shouldAlert;
			runEnergyLow = shouldAlert;
		}
		else
		{
			runEnergyLow = false;
			lastRunEnergyState = false;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV.getId())
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

			// Check if already degraded (has DEGRADE item ID)
			if (isDegradedPouch(item.getId()))
			{
				if (!lastPouchState)
				{
					if (config.enablePouchNotification())
					{
						notifier.notify("Pouch degraded — repair with NPC Contact!");
					}
					lastPouchState = true;
				}
				pouchNeedsRepair = true;
				return;
			}

			// Check if at 1 charge left before next degradation
			if (pouch.getDegradation != null && isPouchAt1Charge(pouch))
			{
				if (!lastPouchState)
				{
					if (config.enablePouchNotification())
					{
						notifier.notify("Pouch at 1 charge — prepare NPC Contact!");
					}
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

		ObjectComposition composition = client.getObjectComposition(gameObject.getId());
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

	@Provides
	ZmiHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZmiHelperConfig.class);
	}
}
