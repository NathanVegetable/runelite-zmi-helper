package com.zmihelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

@Slf4j
class ZmiHelperHighlightOverlay extends Overlay
{
	private static final float PULSE_TIME = 2f * Constants.GAME_TICK_LENGTH;
	private static final double DARKEN_FACTOR = 0.36;

	private final Client client;
	private final ZmiHelperPlugin plugin;
	private final ZmiHelperConfig config;
	private Instant startOfLastTick = Instant.now();
	private boolean trackTick = true;

	@Inject
	private ZmiHelperHighlightOverlay(Client client, ZmiHelperPlugin plugin, ZmiHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	void onTick()
	{
		if (trackTick)
		{
			startOfLastTick = Instant.now();
			trackTick = false;
		}
		else
		{
			trackTick = true;
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		onTick();

		if (plugin.pouchNeedsRepair && config.highlightNpcContact() && plugin.isInUpperZmiArea())
		{
			highlightSpell(graphics, InterfaceID.MagicSpellbook.NPC_CONTACT);
		}

		if (plugin.runEnergyLow && config.highlightRunEnergySpell())
		{
			if (!config.runEnergyRequireAltar() || plugin.isInUpperZmiArea())
			{
				int spellbook = client.getVarbitValue(VarbitID.SPELLBOOK);
				if (spellbook == 2)
				{
					highlightSpell(graphics, InterfaceID.MagicSpellbook.SPELLBOOK_SWAP);
				}
				else if (spellbook == 3)
				{
					highlightSpell(graphics, InterfaceID.MagicSpellbook.VILE_VIGOUR);
				}
			}
		}

		if ((plugin.pouchNeedsRepair && config.highlightSpellbookTabForPouch())
			|| (plugin.runEnergyLow && config.highlightSpellbookTabForEnergy()))
		{
			if (!isSpellbookOpen())
			{
				highlightSpellbookTab(graphics);
			}
		}

		return null;
	}

	private void highlightSpell(Graphics2D graphics, int spellWidgetId)
	{
		Widget spell = client.getWidget(spellWidgetId);
		if (spell == null || spell.isHidden())
		{
			return;
		}

		Rectangle bounds = spell.getBounds();
		Color color = getPulseColor();
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(bounds);
	}

	private void highlightSpellbookTab(Graphics2D graphics)
	{
		Widget tab = client.getWidget(InterfaceID.Toplevel.STONE6);
		if (tab == null || tab.isHidden())
		{
			tab = client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE6);
		}
		if (tab == null || tab.isHidden())
		{
			tab = client.getWidget(InterfaceID.ToplevelPreEoc.STONE6);
		}

		if (tab == null || tab.isHidden())
		{
			return;
		}

		Rectangle bounds = tab.getBounds();
		Color color = getPulseColor();
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(bounds);
	}

	private boolean isSpellbookOpen()
	{
		Widget spellbook = client.getWidget(InterfaceID.MAGIC_SPELLBOOK, 0);
		return spellbook != null && !spellbook.isHidden();
	}

	private Color getPulseColor()
	{
		float tickProgress = Math.min(
			(float) Duration.between(startOfLastTick, Instant.now()).toMillis() / PULSE_TIME,
			1f);
		double t = tickProgress * Math.PI;
		Color baseColor = config.highlightColor();
		return ColorUtil.colorLerp(baseColor, darkenColor(baseColor), Math.sin(t));
	}

	private Color darkenColor(Color color)
	{
		return ColorUtil.colorWithAlpha(
			new Color(
				(int) (color.getRed() * (1 - DARKEN_FACTOR)),
				(int) (color.getGreen() * (1 - DARKEN_FACTOR)),
				(int) (color.getBlue() * (1 - DARKEN_FACTOR))
			),
			color.getAlpha());
	}
}
