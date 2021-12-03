package net.runelite.client.plugins.ggbotv4.bot.scripts;

import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskUtil;
import net.runelite.client.plugins.ggbotv4.plugin.BotPlugin;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.util.List;
import java.util.*;

public class WoodcuttingScript implements Script {
    private long treeTarget = -1;
    private final LocalPoint startPosition;

    public WoodcuttingScript(BotPlugin bot) {
        startPosition = new LocalPoint(
                bot.getClient().getLocalPlayer().getLocalLocation().getX(),
                bot.getClient().getLocalPlayer().getLocalLocation().getY()
        );
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Task evaluate(BotPlugin bot) {
        switch(bot.getState()) {
            case Idle: {
                if (bot.isInventoryFull()) {
                    System.out.println("Inventory full, going to the bank...");

                    treeTarget = -1;
                    return TaskUtil.interact(bot.getBankTarget(), MenuAction.GAME_OBJECT_SECOND_OPTION);
                }

                if (bot.getTreeObjects().get(treeTarget) == null) {
                    Map<Long, GameObject> treeObjects = bot.getTreeObjects();

                    if (treeObjects.size() > 0) {
                        List<GameObject> trees = new ArrayList<>(treeObjects.values());
                        trees.removeIf(object -> TreeType.of(object.getId()) == null);

                        System.out.println(treeObjects.size() + " trees around");

                        GameObject tree = trees.stream().min(Comparator.comparing((GameObject a) -> a.getLocalLocation().distanceTo(startPosition))).orElseThrow();
                        treeTarget = tree.getHash();

                        System.out.println("Found new target at " + tree.getLocalLocation().getSceneX() + ", " + tree.getLocalLocation().getSceneY());

                        return TaskUtil.interact(tree, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    } else {
                        System.out.println("No trees around.");
                    }
                } else {
                    // Probably levelup or something...
                    System.out.println("Did I go levelup? Target is not null yet, retrying old target...");
                    return TaskUtil.interact(bot.getTreeObjects().get(treeTarget), MenuAction.GAME_OBJECT_FIRST_OPTION);
                }

                System.out.println("Player started idling...");
            } break;

            case Banking: {
                Item[] items = bot.getClient().getItemContainer(InventoryID.INVENTORY).getItems();
                Axe axe = Axe.findActive(bot.getClient());

                List<Task> tasks = new ArrayList<>();
                Set<Integer> processed = new HashSet<>();
                for(int slot = 0; slot < items.length; slot++) {
                    Item item = items[slot];

                    if((axe == null || item.getId() != axe.getItemId()) && !processed.contains(item.getId())) {
                        processed.add(item.getId());
                        tasks.add(TaskUtil.bankDepositAll(slot));
                    }
                }

                tasks.add(TaskUtil.clickWidgetChild(WidgetInfo.BANK_WINDOW, 11));
                return Task.of(tasks);
            }
        }

        return null;
    }

    @Override
    public void renderDebug(Graphics2D graphics, BotPlugin bot) {
        if(startPosition != null) {
            OverlayUtil.renderLocalPoint(bot.getClient(), graphics, startPosition, Color.YELLOW);
        }

        if(bot.getTreeObjects().get(treeTarget) != null) {
            OverlayUtil.renderTileOverlay(graphics, bot.getTreeObjects().get(treeTarget), "Current target", Color.GREEN);
        }
    }

}
