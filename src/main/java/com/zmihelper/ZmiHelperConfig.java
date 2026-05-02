package com.zmihelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("zmihelper")
public interface ZmiHelperConfig extends Config
{
	@ConfigSection(
		name = "Pouch Tracking",
		description = "Settings for pouch charge reminders",
		position = 0
	)
	String pouchSection = "pouch";

	@ConfigItem(
		keyName = "enablePouchReminder",
		name = "Enable Pouch Reminder",
		description = "Show reminder when pouch is at 1 charge",
		section = "pouch"
	)
	default boolean enablePouchReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enablePouchNotification",
		name = "Enable Pouch Notification",
		description = "Show notification when pouch is at 1 charge",
		section = "pouch"
	)
	default boolean enablePouchNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashPouchReminder",
		name = "Flash Pouch Reminder",
		description = "Flash the reminder on screen when pouch needs recharging",
		section = "pouch"
	)
	default boolean flashPouchReminder()
	{
		return true;
	}

	@ConfigSection(
		name = "Run Energy Tracking",
		description = "Settings for run energy reminders",
		position = 1
	)
	String runEnergySection = "runenergy";

	@ConfigItem(
		keyName = "enableRunEnergyReminder",
		name = "Enable Run Energy Reminder",
		description = "Show reminder when run energy is below threshold",
		section = "runenergy"
	)
	default boolean enableRunEnergyReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "runEnergyThreshold",
		name = "Run Energy Threshold",
		description = "Show reminder when run energy drops below this (0 to disable)",
		section = "runenergy"
	)
	default int runEnergyThreshold()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "enableRunEnergyNotification",
		name = "Enable Run Energy Notification",
		description = "Show notification when run energy is below threshold",
		section = "runenergy"
	)
	default boolean enableRunEnergyNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashRunEnergyReminder",
		name = "Flash Run Energy Reminder",
		description = "Flash the reminder on screen when run energy is low",
		section = "runenergy"
	)
	default boolean flashRunEnergyReminder()
	{
		return true;
	}

	@ConfigSection(
		name = "Prayer Altar Tracking",
		description = "Settings for prayer altar reminders",
		position = 2
	)
	String prayerSection = "prayer";

	@ConfigItem(
		keyName = "enablePrayerReminder",
		name = "Enable Prayer Reminder",
		description = "Show reminder to click prayer altar when nearby",
		section = "prayer"
	)
	default boolean enablePrayerReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "prayerThreshold",
		name = "Prayer Threshold",
		description = "Show reminder when prayer is below this (0 to disable)",
		section = "prayer"
	)
	default int prayerThreshold()
	{
		return 20;
	}
}
