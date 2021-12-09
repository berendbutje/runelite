package net.runelite.client.plugins.ggbotv4.bot.scripts;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetInfo;

@Getter
public enum SmeltingRecipe {
    BRONZE_BAR(
            new int[] { ItemID.COPPER_ORE, ItemID.TIN_ORE },
            -1,
            ItemID.BRONZE_BAR,

            WidgetInfo.MULTI_SKILL_MENU_OPTION_1.getId()
    ),

    GOLD_RING(
            new int[] { ItemID.GOLD_BAR },
            ItemID.RING_MOULD,
            ItemID.GOLD_RING,

            WidgetInfo.PACK(
                    WidgetInfo.CRAFTING_SMELT_ROOT.getGroupId(),
                    WidgetInfo.CRAFTING_SMELT_RINGS_CONTENT.getChildId() + 1
            )
    )

    ;

    int[] ingredients;
    int mould;
    int result;

    int buttonId;

    SmeltingRecipe(int[] ingredients, int mould, int result, int buttonId) {
        this.ingredients = ingredients;
        this.mould = mould;
        this.result = result;
        this.buttonId = buttonId;
    }

    public boolean hasMould() {
        return this.mould > 0;
    }
}
