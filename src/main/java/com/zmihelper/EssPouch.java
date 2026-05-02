package com.zmihelper;

import java.util.function.ToIntFunction;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;

enum EssPouch
{
	SMALL(VarbitID.SMALL_ESSENCE_POUCH, null, new int[]{}, 3),
	MEDIUM(VarbitID.MEDIUM_ESSENCE_POUCH, client -> client.getVarpValue(VarPlayerID.RCU_POUCH_DEGRADATION_MED), new int[]{
		800, 0,
		400, 3,
	}, 6),
	LARGE(VarbitID.LARGE_ESSENCE_POUCH, client -> client.getVarpValue(VarPlayerID.RCU_POUCH_DEGRADATION_LARGE), new int[]{
		1000, 0,
		800, 3,
		600, 5,
		400, 7,
	}, 9),
	GIANT(VarbitID.GIANT_ESSENCE_POUCH, client -> client.getVarpValue(VarPlayerID.RCU_POUCH_DEGRADATION_GIANT), new int[]{
		1200, 0,
		1000, 3,
		800, 5,
		600, 6,
		400, 7,
		300, 8,
		200, 9,
	}, 12),
	COLOSSAL(VarbitID.COLOSSAL_ESSENCE_POUCH, client -> client.getVarbitValue(VarbitID.RCU_POUCH_DEGRADATION_COLOSSAL), new int[]{
		1020, 0,
		1015, 5,
		995, 10,
		950, 15,
		870, 20,
		745, 25,
		565, 30,
		320, 35,
	}, 40)
	{
		@Override
		int scaleLimit(Client client, int limit)
		{
			int rc = client.getRealSkillLevel(Skill.RUNECRAFT);
			int scaledMax;
			if (rc >= 85)
			{
				scaledMax = 40;
			}
			else if (rc >= 75)
			{
				scaledMax = 27;
			}
			else if (rc >= 50)
			{
				scaledMax = 16;
			}
			else
			{
				scaledMax = 8;
			}

			return Math.max(1, (limit * scaledMax) / 40);
		}
	};

	@Varbit
	private final int amountVarb;
	final ToIntFunction<Client> getDegradation;
	private final int[] degradationLevels;
	private final int maxFill;

	int scaleLimit(Client client, int limit)
	{
		return limit;
	}

	int maxAmount(Client client)
	{
		int deg = getDegradation(client);
		int limit = this.maxFill;
		for (int i = 0; i < degradationLevels.length; i += 2)
		{
			if (deg >= degradationLevels[i])
			{
				limit = degradationLevels[i + 1];
				break;
			}
		}

		if (limit > 0)
		{
			limit = this.scaleLimit(client, limit);
		}

		return limit;
	}

	int nextDegradationBreakpoint(Client client)
	{
		int deg = this.getDegradation.applyAsInt(client);
		for (int i = degradationLevels.length - 2; i >= 0; i -= 2)
		{
			if (deg < degradationLevels[i])
			{
				return degradationLevels[i];
			}
		}
		return deg;
	}

	int getDegradation(Client client)
	{
		if (this.getDegradation == null)
		{
			return 0;
		}

		return this.getDegradation.applyAsInt(client);
	}

	int getAmount(Client client)
	{
		return client.getVarbitValue(amountVarb);
	}

	EssPouch(int amountVarb, ToIntFunction<Client> getDegradation, int[] degradationLevels, int maxFill)
	{
		this.amountVarb = amountVarb;
		this.getDegradation = getDegradation;
		this.degradationLevels = degradationLevels;
		this.maxFill = maxFill;
	}
}
