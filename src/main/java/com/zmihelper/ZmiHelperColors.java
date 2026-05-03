package com.zmihelper;

import java.awt.Color;
import net.runelite.api.Client;

class ZmiHelperColors
{
	static final Color REMINDER_BRIGHT = new Color(255, 255, 0);
	static final Color REMINDER_DIM = new Color(145, 145, 0);

	static Color getReminderColor(Client client, boolean flashEnabled)
	{
		if (!flashEnabled)
		{
			return REMINDER_BRIGHT;
		}

		boolean shouldFlash = (client.getGameCycle() % 40) < 20;
		return shouldFlash ? REMINDER_BRIGHT : REMINDER_DIM;
	}
}
