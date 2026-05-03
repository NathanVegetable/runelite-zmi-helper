package com.zmihelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
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
		if (!plugin.isAltarVisibleOnSamePlane())
		{
			return null;
		}

		boolean shouldHighlight = false;

		if (config.highlightAltarLowPrayer() && config.prayerThreshold() > 0
			&& plugin.currentPrayer < config.prayerThreshold())
		{
			shouldHighlight = true;
		}

		if (!shouldHighlight && config.highlightAltarLowRunEnergy() && plugin.runEnergyLow)
		{
			shouldHighlight = true;
		}

		if (!shouldHighlight)
		{
			return null;
		}

		log.info("Highlighting altar - prayer: {}, runEnergy: {}", plugin.currentPrayer, plugin.currentRunEnergy);
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
