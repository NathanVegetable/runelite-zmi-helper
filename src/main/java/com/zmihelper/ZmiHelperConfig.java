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
		name = "Rune Pouch",
		description = "Settings for pouch charge reminders",
		position = 0
	)
	String pouchSection = "pouch";

	@ConfigItem(
		keyName = "enablePouchTextReminder",
		name = "Interface Reminder",
		description = "Show on-screen reminder when pouch needs repair",
		section = "pouch",
		position = 0
	)
	default boolean enablePouchTextReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashPouchReminder",
		name = "Flash Interface",
		description = "Flash the reminder on screen",
		section = "pouch",
		position = 1
	)
	default boolean flashPouchReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pouchNotification",
		name = "Notify",
		description = "Notification when pouch needs repair",
		section = "pouch",
		position = 2
	)
	default Notification pouchNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "highlightNpcContact",
		name = "Highlight Spell",
		description = "Highlight NPC Contact spell and spellbook tab if not open",
		section = "pouch",
		position = 3
	)
	default boolean highlightNpcContact()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pouchRequireAltar",
		name = "Only when nearby altar",
		description = "Only remind when you're at the Chaos altar. Disable to get reminders anywhere in ZMI",
		section = "pouch",
		position = 4
	)
	default boolean pouchRequireAltar()
	{
		return true;
	}

	@ConfigSection(
		name = "Run Energy",
		description = "Settings for run energy reminders",
		position = 1
	)
	String runEnergySection = "runenergy";

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "runEnergyThreshold",
		name = "Threshold",
		description = "Show reminder when energy drops below this (0 to disable)",
		section = "runenergy",
		position = 0
	)
	default int runEnergyThreshold()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "enableRunEnergyTextReminder",
		name = "Interface Reminder",
		description = "Show on-screen reminder when energy is below threshold",
		section = "runenergy",
		position = 1
	)
	default boolean enableRunEnergyTextReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashRunEnergyReminder",
		name = "Flash Interface",
		description = "Flash the reminder on screen",
		section = "runenergy",
		position = 2
	)
	default boolean flashRunEnergyReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "runEnergyNotification",
		name = "Notify",
		description = "Notification when energy is below threshold",
		section = "runenergy",
		position = 3
	)
	default Notification runEnergyNotification()
	{
		return Notification.ON;
	}

	@ConfigItem(
		keyName = "highlightRunEnergySpell",
		name = "Highlight Spell",
		description = "Highlight the relevant spell (Spellbook Swap or Vile Vigour) and spellbook tab if not open",
		section = "runenergy",
		position = 4
	)
	default boolean highlightRunEnergySpell()
	{
		return true;
	}

	@ConfigItem(
		keyName = "runEnergyRequireAltar",
		name = "Only when nearby altar",
		description = "Only remind when you're at the Chaos altar. Disable to get reminders anywhere in ZMI",
		section = "runenergy",
		position = 5
	)
	default boolean runEnergyRequireAltar()
	{
		return true;
	}

	@ConfigSection(
		name = "Prayer Altar",
		description = "Settings for Chaos altar highlighting",
		position = 2
	)
	String prayerSection = "prayer";

	@Range(min = 0, max = 99)
	@ConfigItem(
		keyName = "prayerThreshold",
		name = "Highlight Low Prayer",
		description = "Highlight altar when prayer is below this (0 to disable)",
		section = "prayer",
		position = 0
	)
	default int prayerThreshold()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "highlightAltarLowRunEnergy",
		name = "Highlight Low Energy",
		description = "Highlight Chaos altar when about to restore run energy with Vile Vigour (which drains prayer)",
		section = "prayer",
		position = 1
	)
	default boolean highlightAltarLowRunEnergy()
	{
		return true;
	}

	@ConfigSection(
		name = "Ourania Teleport",
		description = "Settings for Ourania Teleport highlighting",
		position = 3
	)
	String teleportSection = "teleport";

	@ConfigItem(
		keyName = "highlightOuraniaTeleport",
		name = "Highlight Spell",
		description = "Highlight Ourania Teleport spell and spellbook tab when ready to leave",
		section = "teleport",
		position = 0
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
		keyName = "spellHighlightColor",
		name = "Spell",
		description = "Color for spell highlights (NPC Contact, Spellbook Swap, Vile Vigour, Ourania Teleport)",
		section = "highlight",
		position = 0
	)
	default Color spellHighlightColor()
	{
		return new Color(0, 255, 0, 255);
	}

	@Alpha
	@ConfigItem(
		keyName = "prayerAltarHighlightColor",
		name = "Prayer Altar",
		description = "Color for Chaos altar highlight",
		section = "highlight",
		position = 1
	)
	default Color prayerAltarHighlightColor()
	{
		return new Color(0, 255, 0, 255);
	}

}
