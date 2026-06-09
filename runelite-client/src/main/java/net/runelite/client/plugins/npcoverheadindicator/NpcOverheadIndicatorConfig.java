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

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(NpcOverheadIndicatorConfig.GROUP)
public interface NpcOverheadIndicatorConfig extends Config
{
	String GROUP = "npcoverheadindicator";

	@ConfigSection(
		name = "Render style",
		description = "Controls how prayer overhead highlights are rendered.",
		position = 0
	)
	String renderStyleSection = "renderStyleSection";

	@ConfigSection(
		name = "Colors",
		description = "Colors used for NPC prayer overhead highlights.",
		position = 1
	)
	String colorsSection = "colorsSection";

	@ConfigItem(
		keyName = "highlightHull",
		name = "Highlight hull",
		description = "Highlights the NPC hull using the overhead color.",
		section = renderStyleSection,
		position = 0
	)
	default boolean highlightHull()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightTile",
		name = "Highlight tile",
		description = "Highlights the NPC tile using the overhead color.",
		section = renderStyleSection,
		position = 1
	)
	default boolean highlightTile()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightTrueTile",
		name = "Highlight true tile",
		description = "Highlights the NPC true tile using the overhead color.",
		section = renderStyleSection,
		position = 2
	)
	default boolean highlightTrueTile()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightOutline",
		name = "Highlight outline",
		description = "Highlights the NPC outline using the overhead color.",
		section = renderStyleSection,
		position = 3
	)
	default boolean highlightOutline()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawNames",
		name = "Draw names",
		description = "Draws NPC names using the overhead color.",
		section = renderStyleSection,
		position = 4
	)
	default boolean drawNames()
	{
		return true;
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Width of the highlighted border.",
		section = renderStyleSection,
		position = 5
	)
	default double borderWidth()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 4
	)
	@ConfigItem(
		keyName = "outlineFeather",
		name = "Outline feather",
		description = "How much of the model outline should be faded.",
		section = renderStyleSection,
		position = 6
	)
	default int outlineFeather()
	{
		return 4;
	}

	@Range(
		min = 0,
		max = 255
	)
	@ConfigItem(
		keyName = "fillAlpha",
		name = "Fill alpha",
		description = "Alpha applied to the fill color.",
		section = renderStyleSection,
		position = 7
	)
	default int fillAlpha()
	{
		return 30;
	}

	@Alpha
	@ConfigItem(
		keyName = "meleeColor",
		name = "Melee color",
		description = "Color used for melee protection or deflect melee overheads.",
		section = colorsSection,
		position = 0
	)
	default Color meleeColor()
	{
		return new Color(232, 76, 61);
	}

	@Alpha
	@ConfigItem(
		keyName = "rangedColor",
		name = "Ranged color",
		description = "Color used for ranged protection or deflect ranged overheads.",
		section = colorsSection,
		position = 1
	)
	default Color rangedColor()
	{
		return new Color(46, 204, 113);
	}

	@Alpha
	@ConfigItem(
		keyName = "magicColor",
		name = "Magic color",
		description = "Color used for magic protection or deflect magic overheads.",
		section = colorsSection,
		position = 2
	)
	default Color magicColor()
	{
		return new Color(52, 152, 219);
	}

	@Alpha
	@ConfigItem(
		keyName = "rangedMagicColor",
		name = "Ranged+magic color",
		description = "Color used for combined ranged and magic protection overheads.",
		section = colorsSection,
		position = 3
	)
	default Color rangedMagicColor()
	{
		return new Color(232, 76, 61);
	}

	@Alpha
	@ConfigItem(
		keyName = "rangedMeleeColor",
		name = "Ranged+melee color",
		description = "Color used for combined ranged and melee protection overheads.",
		section = colorsSection,
		position = 4
	)
	default Color rangedMeleeColor()
	{
		return new Color(52, 152, 219);
	}

	@Alpha
	@ConfigItem(
		keyName = "magicMeleeColor",
		name = "Magic+melee color",
		description = "Color used for combined magic and melee protection overheads.",
		section = colorsSection,
		position = 5
	)
	default Color magicMeleeColor()
	{
		return new Color(46, 204, 113);
	}

	@Alpha
	@ConfigItem(
		keyName = "tripleProtectionColor",
		name = "Triple protection color",
		description = "Color used for combined ranged, magic, and melee protection overheads.",
		section = colorsSection,
		position = 6
	)
	default Color tripleProtectionColor()
	{
		return new Color(241, 196, 15);
	}

	@Alpha
	@ConfigItem(
		keyName = "utilityColor",
		name = "Utility color",
		description = "Color used for utility overheads such as Smite, Redemption, and Soul Split.",
		section = colorsSection,
		position = 7
	)
	default Color utilityColor()
	{
		return new Color(243, 156, 18);
	}

	@Alpha
	@ConfigItem(
		keyName = "otherColor",
		name = "Other color",
		description = "Fallback color for any unrecognized overheads.",
		section = colorsSection,
		position = 8
	)
	default Color otherColor()
	{
		return Color.WHITE;
	}
}
