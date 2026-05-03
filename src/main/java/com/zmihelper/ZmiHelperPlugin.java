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
	GameObject rcAltar = null;
	boolean nearRcAltar = false;
	boolean allEssenceGone = false;

	private boolean pouchNotificationSent;
	private boolean runEnergyNotificationSent;
	private boolean prayerLowTracked;
	private boolean suppressNextNotifications = false;
	private boolean lastAltarVisible = true;
	private boolean lastPouchNeedsRepair = false;

	// Lower ZMI region where the altar is located
	// Observed bounds when running through: x=3013-3058, y=5579-5629
	// Added 25 tile buffer in all directions for safety
	private static final int LOWER_ZMI_REGION = 12119;
	private static final int LOWER_ZMI_MIN_X = 2988;
	private static final int LOWER_ZMI_MAX_X = 3083;
	private static final int LOWER_ZMI_MIN_Y = 5554;
	private static final int LOWER_ZMI_MAX_Y = 5654;

	// Upper ZMI region near the bank
	// Observed bounds when running through: x=2452-2469, y=3232-3249
	// Added 25 tile buffer in all directions for safety
	private static final int UPPER_ZMI_REGION = 9778;
	private static final int UPPER_ZMI_MIN_X = 2427;
	private static final int UPPER_ZMI_MAX_X = 2494;
	private static final int UPPER_ZMI_MIN_Y = 3207;
	private static final int UPPER_ZMI_MAX_Y = 3274;

	private static final String CHAOS_ALTAR_NAME = "Chaos altar";
	private static final String RUNECRAFT_ALTAR_NAME = "Altar";

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

		if (client.getLocalPlayer() == null)
		{
			return;
		}

		if (!isInZmiArea())
		{
			return;
		}

		currentRunEnergy = client.getEnergy();

		// Check pouch state every tick rather than on ItemContainerChanged events,
		// since NPC Contact repairs update the pouch's degradation state without
		// necessarily triggering a container change event
		checkPouchState();

		nearRcAltar = computeNearRcAltar();
		allEssenceGone = computeAllEssenceGone();

		boolean inUpperArea = isInUpperZmiArea();

		if (config.runEnergyThreshold() > 0)
		{
			int threshold = config.runEnergyThreshold() * 100;
			runEnergyLow = currentRunEnergy < threshold;

			boolean canNotifyRunEnergy = runEnergyLow && !suppressNextNotifications;
			boolean meetsLocationRequirement = !config.runEnergyRequireAltar() || inUpperArea;

			if (canNotifyRunEnergy && meetsLocationRequirement && !runEnergyNotificationSent)
			{
				notifier.notify(config.runEnergyNotification(), "Run energy low - cast Vile Vigour");
				runEnergyNotificationSent = true;
			}

			if (!runEnergyLow || (config.runEnergyRequireAltar() && !inUpperArea))
			{
				runEnergyNotificationSent = false;
			}
		}
		else
		{
			runEnergyLow = false;
			runEnergyNotificationSent = false;
		}

		if (pouchNeedsRepair)
		{
			boolean canNotifyPouch = pouchNeedsRepair && !suppressNextNotifications;
			boolean meetsLocationRequirement = !config.pouchRequireAltar() || inUpperArea;

			if (canNotifyPouch && meetsLocationRequirement && !pouchNotificationSent)
			{
				notifier.notify(config.pouchNotification(), "Pouch needs repair — cast NPC Contact!");
				pouchNotificationSent = true;
			}

			if (config.pouchRequireAltar() && !inUpperArea)
			{
				pouchNotificationSent = false;
			}
		}
		else
		{
			pouchNotificationSent = false;
		}

		lastAltarVisible = inUpperArea;
		lastPouchNeedsRepair = pouchNeedsRepair;

		suppressNextNotifications = false;
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
				pouchNeedsRepair = true;
				return;
			}
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
		if (!isInZmiArea() || event.getSkill() != Skill.PRAYER)
		{
			return;
		}

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

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!isInZmiArea())
		{
			return;
		}

		GameObject gameObject = event.getGameObject();
		if (gameObject == null)
		{
			return;
		}

		ObjectComposition composition = client.getObjectDefinition(gameObject.getId());
		if (composition == null)
		{
			return;
		}

		String name = composition.getName();
		if (CHAOS_ALTAR_NAME.equals(name) && isInUpperZmiArea())
		{
			chaosAltar = gameObject;
		}
		else if (RUNECRAFT_ALTAR_NAME.equals(name) && isInLowerZmiArea())
		{
			rcAltar = gameObject;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (!isInZmiArea())
		{
			return;
		}

		GameObject gameObject = event.getGameObject();
		if (gameObject == chaosAltar)
		{
			chaosAltar = null;
		}
		if (gameObject == rcAltar)
		{
			rcAltar = null;
		}
	}

	boolean isInUpperZmiArea()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		int region = client.getLocalPlayer().getWorldLocation().getRegionID();
		if (region != UPPER_ZMI_REGION)
		{
			return false;
		}

		int x = client.getLocalPlayer().getWorldLocation().getX();
		int y = client.getLocalPlayer().getWorldLocation().getY();

		return x >= UPPER_ZMI_MIN_X && x <= UPPER_ZMI_MAX_X
			&& y >= UPPER_ZMI_MIN_Y && y <= UPPER_ZMI_MAX_Y;
	}

	boolean isInLowerZmiArea()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		int region = client.getLocalPlayer().getWorldLocation().getRegionID();
		if (region != LOWER_ZMI_REGION)
		{
			return false;
		}

		int x = client.getLocalPlayer().getWorldLocation().getX();
		int y = client.getLocalPlayer().getWorldLocation().getY();

		return x >= LOWER_ZMI_MIN_X && x <= LOWER_ZMI_MAX_X
			&& y >= LOWER_ZMI_MIN_Y && y <= LOWER_ZMI_MAX_Y;
	}

	private boolean isInZmiArea()
	{
		return isInLowerZmiArea() || isInUpperZmiArea();
	}


	private boolean computeNearRcAltar()
	{
		if (rcAltar == null || client.getLocalPlayer() == null)
		{
			return false;
		}

		int distance = client.getLocalPlayer().getWorldLocation().distanceTo(rcAltar.getWorldLocation());
		return distance <= 10;
	}

	private boolean computeAllEssenceGone()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return false;
		}

		for (Item item : inventory.getItems())
		{
			int id = item.getId();
			if (id == ItemID.BLANKRUNE_HIGH || id == ItemID.BLANKRUNE_DAEYALT || id == ItemID.BLANKRUNE)
			{
				return false;
			}
		}

		for (EssPouch pouch : EssPouch.values())
		{
			if (pouch.getAmount(client) > 0)
			{
				return false;
			}
		}

		return true;
	}

	@Provides
	ZmiHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZmiHelperConfig.class);
	}
}
