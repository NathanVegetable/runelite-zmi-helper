package com.zmihelper;

import java.awt.Color;
import net.runelite.client.config.Alpha;
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
		keyName = "enablePouchNotification",
		name = "Enable Pouch Notification",
		description = "Show OS notification when pouch is at 1 charge",
		section = "pouch"
	)
	default boolean enablePouchNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightNpcContact",
		name = "Highlight NPC Contact Spell",
		description = "Highlight the NPC Contact spell in standard spellbook",
		section = "pouch"
	)
	default boolean highlightNpcContact()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightSpellbookTabForPouch",
		name = "Highlight Spellbook Tab",
		description = "Highlight magic tab icon if spellbook is not open",
		section = "pouch"
	)
	default boolean highlightSpellbookTabForPouch()
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
		keyName = "enableRunEnergyNotification",
		name = "Enable Run Energy Notification",
		description = "Show OS notification when run energy is below threshold",
		section = "runenergy"
	)
	default boolean enableRunEnergyNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "runEnergyThreshold",
		name = "Run Energy Threshold",
		description = "Show reminder when run energy drops below this % (0 to disable)",
		section = "runenergy"
	)
	default int runEnergyThreshold()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "highlightRunEnergySpell",
		name = "Highlight Run Energy Spell",
		description = "Highlight the relevant spell (Spellbook Swap or Vile Vigour)",
		section = "runenergy"
	)
	default boolean highlightRunEnergySpell()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightSpellbookTabForEnergy",
		name = "Highlight Spellbook Tab",
		description = "Highlight magic tab icon if spellbook is not open",
		section = "runenergy"
	)
	default boolean highlightSpellbookTabForEnergy()
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

	@ConfigItem(
		keyName = "prayerThreshold",
		name = "Prayer Threshold",
		description = "Highlight altar when prayer is below this % (0 to disable)",
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

	@Alpha
	@ConfigItem(
		keyName = "altarHighlightColor",
		name = "Altar Highlight Color",
		description = "Color to highlight the Chaos altar",
		section = "prayer"
	)
	default Color altarHighlightColor()
	{
		return new Color(255, 255, 0, 100);
	}
}
