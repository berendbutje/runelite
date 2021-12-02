package net.runelite.client.plugins.ggbotv4.util;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;

import java.util.*;

import static net.runelite.api.AnimationID.*;
import static net.runelite.api.ItemID.*;

@AllArgsConstructor
@Getter
public enum Axe
{
	BRONZE(WOODCUTTING_BRONZE, BRONZE_AXE, 0, 1),
	IRON(WOODCUTTING_IRON, IRON_AXE, 1, 1),
	STEEL(WOODCUTTING_STEEL, STEEL_AXE, 2, 6),
	BLACK(WOODCUTTING_BLACK, BLACK_AXE, 3, 11),
	MITHRIL(WOODCUTTING_MITHRIL, MITHRIL_AXE, 4, 21),
	ADAMANT(WOODCUTTING_ADAMANT, ADAMANT_AXE, 5, 31),
	RUNE(WOODCUTTING_RUNE, RUNE_AXE, 6, 41),
	GILDED(WOODCUTTING_GILDED, GILDED_AXE, 7, 41),
	DRAGON(WOODCUTTING_DRAGON, DRAGON_AXE, 8, 61),
	DRAGON_OR(WOODCUTTING_DRAGON_OR, DRAGON_AXE_OR, 9, 61),
	INFERNAL(WOODCUTTING_INFERNAL, INFERNAL_AXE, 10, 61),
	TRAILBLAZER(WOODCUTTING_TRAILBLAZER, INFERNAL_AXE_OR, 13, 61),
	THIRD_AGE(WOODCUTTING_3A_AXE, _3RD_AGE_AXE, 11, 61),
	CRYSTAL(WOODCUTTING_CRYSTAL, CRYSTAL_AXE, 12, 71),
	;

	private final Integer animId;
	private final Integer itemId;
	private final int tier;
	private final int requiredLevel;

	private static final Map<Integer, Axe> AXE_ITEM_IDS;

	static
	{
		ImmutableMap.Builder<Integer, Axe> builder = new ImmutableMap.Builder<>();

		for (Axe axe : values())
		{
			builder.put(axe.itemId, axe);
		}

		AXE_ITEM_IDS = builder.build();
	}

	private static final Map<Integer, Axe> AXE_ANIM_IDS;

	static
	{
		ImmutableMap.Builder<Integer, Axe> builder = new ImmutableMap.Builder<>();

		for (Axe axe : values())
		{
			builder.put(axe.animId, axe);
		}

		AXE_ANIM_IDS = builder.build();
	}

	public static Axe byItemId(int itemId)
	{
		return AXE_ITEM_IDS.get(itemId);
	}

	public static Axe byAnimId(int animId) {
		return AXE_ANIM_IDS.get(animId);
	}

	public String getName(Client client) {
		return client.getItemDefinition(getItemId()).getName();
	}

	public int getTier() {
		return tier;
	}

	public static Axe findActive(Client client) {
		// Check player equipment
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

		List<Axe> availableAxes = new ArrayList<>();
		if(equipment != null) {
			for(Axe axe : values()) {
				if(equipment.contains(axe.getItemId())) {
					availableAxes.add(axe);
				}
			}
		}
		if(inventory != null) {
			for(Axe axe : values()) {
				if(inventory.contains(axe.getItemId())) {
					availableAxes.add(axe);
				}
			}
		}

		int woodcuttingLevel = client.getBoostedSkillLevel(Skill.WOODCUTTING);
		if(availableAxes.size() > 0) {
			return availableAxes.stream().filter(a -> woodcuttingLevel >= a.requiredLevel).max(Comparator.comparingInt(Axe::getTier)).orElse(null);
		} else {
			return null;
		}
	}


}
