package com.zmihelper;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

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
		keyName = "enablePouchTextReminder",
		name = "Enable Pouch Text Reminder",
		description = "Show on-screen text reminder when pouch is at 1 charge",
		section = "pouch"
	)
	default boolean enablePouchTextReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashPouchReminder",
		name = "Flash Pouch Reminder",
		description = "Flash the text reminder on screen",
		section = "pouch"
	)
	default boolean flashPouchReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pouchNotification",
		name = "Pouch Notification",
		description = "Notification when pouch needs repair",
		section = "pouch"
	)
	default Notification pouchNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "pouchRequireAltar",
		name = "Only Remind At Altar",
		description = "Only notify when you're at the Chaos altar. Disable to get reminders anywhere in ZMI",
		section = "pouch"
	)
	default boolean pouchRequireAltar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightNpcContact",
		name = "Highlight NPC Contact Spell & Spellbook Tab",
		description = "Highlight the NPC Contact spell and spellbook tab if not open",
		section = "pouch"
	)
	default boolean highlightNpcContact()
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
		keyName = "enableRunEnergyTextReminder",
		name = "Enable Run Energy Text Reminder",
		description = "Show on-screen text reminder when run energy is below threshold",
		section = "runenergy"
	)
	default boolean enableRunEnergyTextReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashRunEnergyReminder",
		name = "Flash Run Energy Reminder",
		description = "Flash the text reminder on screen",
		section = "runenergy"
	)
	default boolean flashRunEnergyReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "runEnergyNotification",
		name = "Run Energy Notification",
		description = "Notification when run energy is below threshold",
		section = "runenergy"
	)
	default Notification runEnergyNotification()
	{
		return Notification.ON;
	}

	@Range(min = 0, max = 100)
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
		keyName = "runEnergyRequireAltar",
		name = "Only Remind At Altar",
		description = "Only notify when you're at the Chaos altar. Disable to get reminders anywhere in ZMI",
		section = "runenergy"
	)
	default boolean runEnergyRequireAltar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightRunEnergySpell",
		name = "Highlight Run Energy Spell & Spellbook Tab",
		description = "Highlight the relevant spell (Spellbook Swap or Vile Vigour) and spellbook tab if not open",
		section = "runenergy"
	)
	default boolean highlightRunEnergySpell()
	{
		return true;
	}

	@ConfigSection(
		name = "Prayer Altar Tracking",
		description = "Settings for Chaos altar highlighting",
		position = 2
	)
	String prayerSection = "prayer";

	@ConfigItem(
		keyName = "highlightAltarLowPrayer",
		name = "Highlight Altar When Prayer Low",
		description = "Highlight Chaos altar when prayer drops below threshold",
		section = "prayer"
	)
	default boolean highlightAltarLowPrayer()
	{
		return true;
	}

	@Range(min = 0, max = 99)
	@ConfigItem(
		keyName = "prayerThreshold",
		name = "Prayer Threshold",
		description = "Highlight altar when prayer is below this (0 to disable)",
		section = "prayer"
	)
	default int prayerThreshold()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "highlightAltarLowRunEnergy",
		name = "Highlight Altar When Run Energy Low",
		description = "Highlight Chaos altar when about to restore run energy (which drains prayer)",
		section = "prayer"
	)
	default boolean highlightAltarLowRunEnergy()
	{
		return true;
	}

	@ConfigSection(
		name = "Runecrafting Teleport",
		description = "Settings for Ourania Teleport highlighting",
		position = 3
	)
	String teleportSection = "teleport";

	@ConfigItem(
		keyName = "highlightOuraniaTeleport",
		name = "Highlight Ourania Teleport",
		description = "Highlight Ourania Teleport spell and spellbook tab when ready to leave",
		section = "teleport"
	)
	default boolean highlightOuraniaTeleport()
	{
		return true;
	}

	@ConfigSection(
		name = "Highlight Colors",
		description = "Customize colors for spell highlights and altar",
		position = 4
	)
	String highlightSection = "highlight";

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Spell & Altar Highlight Color",
		description = "Color for spell highlights (NPC Contact, Spellbook Swap, Vile Vigour, Ourania Teleport) and Chaos altar",
		section = "highlight"
	)
	default Color highlightColor()
	{
		return new Color(0, 255, 0, 255);
	}

}
