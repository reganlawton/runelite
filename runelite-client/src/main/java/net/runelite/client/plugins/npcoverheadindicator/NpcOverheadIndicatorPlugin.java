/*
 * Copyright (c) 2026
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.npcoverheadindicator;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "NPC Overhead Indicator",
	description = "Highlights NPC overhead prayers as a visual aid",
	tags = {"npc", "overhead", "prayer", "indicator", "visual"},
	enabledByDefault = true,
	developerPlugin = false
)
public class NpcOverheadIndicatorPlugin extends Plugin
{
	private static final String NO_OVERHEAD = "None";

	private final Map<Integer, NpcObservation> npcPrayerOverheads = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private NpcOverlayService npcOverlayService;

	@Inject
	private NpcOverheadIndicatorConfig config;

	private final Function<NPC, HighlightedNpc> overheadHighlighter = npc ->
	{
		final int primaryPrayerSpriteId = getPrimaryPrayerSpriteId(npc);
		if (primaryPrayerSpriteId == -1)
		{
			return null;
		}

		final Color highlightColor = getHighlightColor(primaryPrayerSpriteId);
		return HighlightedNpc.builder()
			.npc(npc)
			.hull(config.highlightHull())
			.tile(config.highlightTile())
			.trueTile(config.highlightTrueTile())
			.outline(config.highlightOutline())
			.name(config.drawNames())
			.borderWidth((float) config.borderWidth())
			.outlineFeather(config.outlineFeather())
			.highlightColor(highlightColor)
			.fillColor(withAlpha(highlightColor, config.fillAlpha()))
			.build();
	};

	@Provides
	NpcOverheadIndicatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NpcOverheadIndicatorConfig.class);
	}

	@Override
	protected void startUp()
	{
		npcPrayerOverheads.clear();
		npcOverlayService.registerHighlighter(overheadHighlighter);
		npcOverlayService.rebuild();
		emit("NPC Overhead Indicator started");
	}

	@Override
	protected void shutDown()
	{
		npcPrayerOverheads.clear();
		npcOverlayService.unregisterHighlighter(overheadHighlighter);
		npcOverlayService.rebuild();
		emit("NPC Overhead Indicator stopped");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!NpcOverheadIndicatorConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		npcOverlayService.rebuild();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			npcPrayerOverheads.clear();
			return;
		}

		final WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return;
		}

		final Set<Integer> activeNpcIndexes = new HashSet<>();
		boolean rebuildHighlights = false;

		for (NPC npc : worldView.npcs())
		{
			if (npc == null)
			{
				continue;
			}

			final int npcIndex = npc.getIndex();
			final String currentPrayerOverhead = getPrayerOverheadState(npc);
			final NpcObservation previousObservation = npcPrayerOverheads.get(npcIndex);
			final String npcDescription = formatNpc(npc);

			if (NO_OVERHEAD.equals(currentPrayerOverhead))
			{
				if (previousObservation != null)
				{
					npcPrayerOverheads.remove(npcIndex);
					emit("NPC overhead cleared: " + npcDescription + " previous=" + previousObservation.overhead);
					rebuildHighlights = true;
				}

				continue;
			}

			activeNpcIndexes.add(npcIndex);

			if (previousObservation != null && Objects.equals(previousObservation.overhead, currentPrayerOverhead))
			{
				continue;
			}

			if (previousObservation == null)
			{
				npcPrayerOverheads.put(npcIndex, new NpcObservation(npcDescription, currentPrayerOverhead));
				emit("NPC overhead detected: " + npcDescription + " overhead=" + currentPrayerOverhead);
				rebuildHighlights = true;
				continue;
			}

			npcPrayerOverheads.put(npcIndex, new NpcObservation(npcDescription, currentPrayerOverhead));
			emit("NPC overhead changed: " + npcDescription + " overhead=" + currentPrayerOverhead + " previous=" + previousObservation.overhead);
			rebuildHighlights = true;
		}

		npcPrayerOverheads.entrySet().removeIf(entry ->
		{
			if (activeNpcIndexes.contains(entry.getKey()))
			{
				return false;
			}

			emit("NPC despawned: " + entry.getValue().description + " lastOverhead=" + entry.getValue().overhead);
			return true;
		});

		if (rebuildHighlights)
		{
			npcOverlayService.rebuild();
		}
	}

	private static String getPrayerOverheadState(NPC npc)
	{
		final int[] archiveIds = npc.getOverheadArchiveIds();
		final short[] spriteIds = npc.getOverheadSpriteIds();

		if (archiveIds == null || spriteIds == null)
		{
			return NO_OVERHEAD;
		}

		return Arrays.stream(getPrayerSpriteIds(archiveIds, spriteIds))
			.mapToObj(NpcOverheadIndicatorPlugin::formatPrayerSpriteId)
			.collect(Collectors.collectingAndThen(Collectors.joining(", "), sprites -> sprites.isEmpty() ? NO_OVERHEAD : sprites));
	}

	private static int[] getPrayerSpriteIds(int[] archiveIds, short[] spriteIds)
	{
		final int len = Math.min(archiveIds.length, spriteIds.length);
		final int[] prayerSprites = new int[len];
		int count = 0;

		for (int i = 0; i < len; i++)
		{
			if (archiveIds[i] == SpriteID.HEADICONS_PRAYER && spriteIds[i] != -1)
			{
				prayerSprites[count++] = spriteIds[i];
			}
		}

		return Arrays.copyOf(prayerSprites, count);
	}

	private static int getPrimaryPrayerSpriteId(NPC npc)
	{
		final int[] archiveIds = npc.getOverheadArchiveIds();
		final short[] spriteIds = npc.getOverheadSpriteIds();

		if (archiveIds == null || spriteIds == null)
		{
			return -1;
		}

		final int len = Math.min(archiveIds.length, spriteIds.length);
		for (int i = 0; i < len; i++)
		{
			if (archiveIds[i] == SpriteID.HEADICONS_PRAYER && spriteIds[i] != -1)
			{
				return spriteIds[i];
			}
		}

		return -1;
	}

	private static boolean hasPrayerOverhead(NPC npc)
	{
		return getPrimaryPrayerSpriteId(npc) != -1;
	}

	private static String formatNpc(NPC npc)
	{
		return String.format("name=%s id=%d index=%d", npc.getName(), npc.getId(), npc.getIndex());
	}

	private static String formatPrayerSpriteId(int spriteId)
	{
		switch (spriteId)
		{
			case 0:
				return "Protect from melee (0)";
			case 1:
				return "Protect from ranged (1)";
			case 2:
				return "Protect from magic (2)";
			case 3:
				return "Retribution (3)";
			case 4:
				return "Smite (4)";
			case 5:
				return "Redemption (5)";
			case 6:
				return "Protect from ranged and magic (6)";
			case 7:
				return "Protect from ranged and melee (7)";
			case 8:
				return "Protect from magic and melee (8)";
			case 9:
				return "Protect from ranged, magic, and melee (9)";
			case 10:
				return "Wrath (10)";
			case 11:
				return "Soul split (11)";
			case 12:
				return "Deflect melee (12)";
			case 13:
				return "Deflect ranged (13)";
			case 14:
				return "Deflect magic (14)";
			default:
				return "Unknown (" + spriteId + ")";
		}
	}

	private void emit(String message)
	{
		log.warn(message);
		System.out.println("[NpcOverheadIndicator] " + message);
	}

	private Color getHighlightColor(int spriteId)
	{
		switch (spriteId)
		{
			case 0:
			case 12:
				return config.meleeColor();
			case 1:
			case 13:
				return config.rangedColor();
			case 2:
			case 14:
				return config.magicColor();
			case 6:
				return config.rangedMagicColor();
			case 7:
				return config.rangedMeleeColor();
			case 8:
				return config.magicMeleeColor();
			case 9:
				return config.tripleProtectionColor();
			case 3:
			case 4:
			case 5:
			case 10:
			case 11:
				return config.utilityColor();
			default:
				return config.otherColor();
		}
	}

	private static Color withAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private static final class NpcObservation
	{
		private final String description;
		private final String overhead;

		private NpcObservation(String description, String overhead)
		{
			this.description = description;
			this.overhead = overhead;
		}
	}
}
