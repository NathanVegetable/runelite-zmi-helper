package com.zmihelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

class ZmiHelperPouchReminder extends OverlayPanel
{
	private final Client client;
	private final ZmiHelperPlugin plugin;
	private final ZmiHelperConfig config;

	@Inject
	private ZmiHelperPouchReminder(Client client, ZmiHelperPlugin plugin, ZmiHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.pouchNeedsRepair || !config.enablePouchTextReminder())
		{
			return null;
		}

		panelComponent.getChildren().clear();

		String text = "Repair pouches — NPC Contact";
		Color color = ZmiHelperColors.getReminderColor(client, config.flashPouchReminder());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(text)
			.leftColor(color)
			.build());

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		return panelComponent.render(graphics);
	}
}
