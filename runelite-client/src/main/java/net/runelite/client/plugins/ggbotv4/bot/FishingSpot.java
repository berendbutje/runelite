package net.runelite.client.plugins.ggbotv4.bot;


import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Map;

import static net.runelite.api.NpcID.*;

@Getter
public enum FishingSpot
{
    SHRIMP(
            new int[] {
                    ItemID.RAW_SHRIMPS,
                    ItemID.RAW_ANCHOVIES
            },

            new int[]{
                    FISHING_SPOT_1514, FISHING_SPOT_1517, FISHING_SPOT_1518,
                    FISHING_SPOT_1521, FISHING_SPOT_1523, FISHING_SPOT_1524,
                    FISHING_SPOT_1525, FISHING_SPOT_1528, FISHING_SPOT_1530,
                    FISHING_SPOT_1544, FISHING_SPOT_3913, FISHING_SPOT_7155,
                    FISHING_SPOT_7459, FISHING_SPOT_7462, FISHING_SPOT_7467,
                    FISHING_SPOT_7469, FISHING_SPOT_7947, FISHING_SPOT_10513
            }
    ),
    LOBSTER(
            new int [] {
                    ItemID.RAW_LOBSTER,
                    ItemID.RAW_SWORDFISH,
                    ItemID.RAW_TUNA
            },
            new int[] {
                    FISHING_SPOT_1510, FISHING_SPOT_1519, FISHING_SPOT_1522,
                    FISHING_SPOT_3914, FISHING_SPOT_5820, FISHING_SPOT_7199,
                    FISHING_SPOT_7460, FISHING_SPOT_7465, FISHING_SPOT_7470,
                    FISHING_SPOT_7946, FISHING_SPOT_9173, FISHING_SPOT_9174,
                    FISHING_SPOT_10515, FISHING_SPOT_10635
            }
    ),
    SHARK(
            new int[] {
                    ItemID.RAW_SHARK,
                    ItemID.RAW_BASS
            },

            new int[]{
                    FISHING_SPOT_1511, FISHING_SPOT_1520, FISHING_SPOT_3419,
                    FISHING_SPOT_3915, FISHING_SPOT_4476, FISHING_SPOT_4477,
                    FISHING_SPOT_5233, FISHING_SPOT_5234, FISHING_SPOT_5821,
                    FISHING_SPOT_7200, FISHING_SPOT_7461, FISHING_SPOT_7466,
                    FISHING_SPOT_8525, FISHING_SPOT_8526, FISHING_SPOT_8527,
                    FISHING_SPOT_9171, FISHING_SPOT_9172, FISHING_SPOT_10514
            }
    ),
    MONKFISH(
            new int[] { ItemID.RAW_MONKFISH },
            new int[] { FISHING_SPOT_4316 }
    ),
    SALMON(
            new int[] {
                    ItemID.RAW_SALMON,
                    ItemID.RAW_TROUT
            },
            new int[] {
                    ROD_FISHING_SPOT, ROD_FISHING_SPOT_1506, ROD_FISHING_SPOT_1507,
                    ROD_FISHING_SPOT_1508, ROD_FISHING_SPOT_1509, ROD_FISHING_SPOT_1513,
                    ROD_FISHING_SPOT_1515, ROD_FISHING_SPOT_1516, ROD_FISHING_SPOT_1526,
                    ROD_FISHING_SPOT_1527, ROD_FISHING_SPOT_3417, ROD_FISHING_SPOT_3418,
                    ROD_FISHING_SPOT_7463, ROD_FISHING_SPOT_7464, ROD_FISHING_SPOT_7468,
                    ROD_FISHING_SPOT_8524
            }
    ),
    LAVA_EEL(
            new int[] { ItemID.LAVA_EEL },
            new int[] { FISHING_SPOT_4928, FISHING_SPOT_6784 }
    ),
    BARB_FISH(
            new int[] {
                    ItemID.LEAPING_STURGEON,
                    ItemID.RAW_SALMON,
                    ItemID.RAW_TROUT
            },
            new int[]{
                    FISHING_SPOT_1542, FISHING_SPOT_7323
            }
    ),
    ANGLERFISH(
            new int[] { ItemID.RAW_ANGLERFISH },
            new int[] { ROD_FISHING_SPOT_6825 }
    ),
    MINNOW(
            new int[] { ItemID.MINNOW },
            new int[]{
                    FISHING_SPOT_7730,
                    FISHING_SPOT_7731,
                    FISHING_SPOT_7732,
                    FISHING_SPOT_7733
            }
    ),
    HARPOONFISH(
            new int[] { ItemID.RAW_HARPOONFISH },
            new int[] { FISHING_SPOT_10565, FISHING_SPOT_10568, FISHING_SPOT_10569 }
    ),
    INFERNAL_EEL(
            new int[] { ItemID.INFERNAL_EEL },
            new int[] { ROD_FISHING_SPOT_7676 }
    ),
    KARAMBWAN(
            new int[] { ItemID.RAW_KARAMBWAN },
            new int[] { FISHING_SPOT_4712, FISHING_SPOT_4713 }
    ),
    KARAMBWANJI(
            new int[] { ItemID.KARAMBWANJI, ItemID.RAW_SHRIMPS },
            new int[] { FISHING_SPOT_4710 }
    ),
    SACRED_EEL(
            new int[] { ItemID.SACRED_EEL },
            new int[] { FISHING_SPOT_6488 }
    ),
    CAVE_EEL(
            new int[] { ItemID.RAW_CAVE_EEL },
            new int[] { FISHING_SPOT_1497, FISHING_SPOT_1498, FISHING_SPOT_1499 }
    ),
    SLIMY_EEL(
            new int[] { ItemID.RAW_SLIMY_EEL },
            new int[] { FISHING_SPOT_2653, FISHING_SPOT_2654, FISHING_SPOT_2655 }
    ),
    DARK_CRAB(
            new int[] { ItemID.RAW_DARK_CRAB },
            new int[] { FISHING_SPOT_1535, FISHING_SPOT_1536 }
    ),
//    COMMON_TENCH("Common tench, Bluegill, Greater siren, Mottled eel", "Greater siren", ItemID.COMMON_TENCH,
//            FISHING_SPOT_8523
//    ),
    TUTORIAL_SHRIMP(
            new int[] { ItemID.RAW_SHRIMPS },
            new int[] { FISHING_SPOT_3317 }
    ),
//    ETCETERIA_LOBSTER("Lobster", "Lobster (Approval only)", ItemID.RAW_LOBSTER,
//            FISHING_SPOT_3657
//    ),
//    QUEST_RUM_DEAL("Sluglings", "Rum deal (Quest)", ItemID.SLUGLINGS,
//            FISHING_SPOT
//    ),
//    QUEST_TAI_BWO_WANNAI_TRIO("Karambwan", "Tai Bwo Wannai Trio (Quest)", ItemID.RAW_KARAMBWAN,
//            FISHING_SPOT_4714
//    ),
//    QUEST_FISHING_CONTEST("Giant carp", "Fishing Contest (Quest)", ItemID.GIANT_CARP,
//            FISHING_SPOT_4079, FISHING_SPOT_4080, FISHING_SPOT_4081, FISHING_SPOT_4082
//    ),
    ;

    private static final Map<Integer, FishingSpot> SPOTS;

    private final int[] itemIds;
    private final int[] npcIds;

    static
    {
        ImmutableMap.Builder<Integer, FishingSpot> builder = new ImmutableMap.Builder<>();

        for (FishingSpot spot : values())
        {
            for (int spotId : spot.getNpcIds())
            {
                builder.put(spotId, spot);
            }
        }

        SPOTS = builder.build();
    }

    FishingSpot(int[] fishSpriteIds, int[] ids)
    {
        this.itemIds = fishSpriteIds;
        this.npcIds = ids;
    }

    public static FishingSpot findSpot(int id)
    {
        return SPOTS.get(id);
    }
}
