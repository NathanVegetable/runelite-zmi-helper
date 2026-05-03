package com.zmihelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

class ZmiHelperRunEnergyReminder extends OverlayPanel
{
	private final Client client;
	private final ZmiHelperPlugin plugin;
	private final ZmiHelperConfig config;

	@Inject
	private ZmiHelperRunEnergyReminder(Client client, ZmiHelperPlugin plugin, ZmiHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.runEnergyLow || !config.enableRunEnergyTextReminder())
		{
			return null;
		}

		if (config.runEnergyRequireAltar() && plugin.chaosAltar == null)
		{
			return null;
		}

		panelComponent.getChildren().clear();

		int spellbook = client.getVarbitValue(VarbitID.SPELLBOOK);
		String spellName = spellbook == 2 ? "Spellbook Swap" : "Vile Vigour";
		String text = "Low run energy — " + spellName;
		Color color = ZmiHelperColors.getReminderColor(client, config.flashRunEnergyReminder());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(text)
			.leftColor(color)
			.build());

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		return panelComponent.render(graphics);
	}
}
