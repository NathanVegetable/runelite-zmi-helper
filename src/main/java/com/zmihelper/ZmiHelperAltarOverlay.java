package com.zmihelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class ZmiHelperAltarOverlay extends Overlay
{
	private final Client client;
	private final ZmiHelperPlugin plugin;
	private final ZmiHelperConfig config;

	@Inject
	private ZmiHelperAltarOverlay(Client client, ZmiHelperPlugin plugin, ZmiHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Check if feature is enabled and conditions are met
		if (!config.enablePrayerReminder()
			|| config.prayerThreshold() == 0
			|| plugin.currentPrayer >= config.prayerThreshold()
			|| plugin.chaosAltar == null)
		{
			return null;
		}

		// Render altar hull
		renderAltarHull(graphics, plugin.chaosAltar);

		return null;
	}

	private void renderAltarHull(Graphics2D graphics, GameObject altar)
	{
		Shape hull = altar.getConvexHull();
		if (hull == null)
		{
			return;
		}

		Color borderColor = config.altarHighlightColor();
		Color fillColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 50);
		Stroke stroke = new BasicStroke(2);

		OverlayUtil.renderPolygon(graphics, hull, borderColor, fillColor, stroke);
	}
}
