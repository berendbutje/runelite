package net.runelite.client.plugins.ggbotv4.bot.scripts;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.GameObjectManager;
import net.runelite.client.plugins.ggbotv4.bot.InventoryManager;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.util.TaskUtil;
import net.runelite.client.plugins.ggbotv4.util.Axe;
import net.runelite.client.plugins.ggbotv4.util.TreeType;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

import static net.runelite.api.Constants.CLIENT_TICK_LENGTH;

public class WoodcuttingScript implements Script {
    private final Client client;
    private final TreeType tree;

    private long currentTarget = -1;
    private final WorldPoint startPosition;

    private String overheadText = "";

    public WoodcuttingScript(Bot bot, TreeType tree) {
        this.client = bot.getClient();
        this.tree = tree;

        startPosition = new WorldPoint(
                client.getLocalPlayer().getWorldLocation().getX(),
                client.getLocalPlayer().getWorldLocation().getY(),
                client.getLocalPlayer().getWorldLocation().getPlane()
        );
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        final Player local = client.getLocalPlayer();
        assert(local != null);

        if(local.getOverheadCycle() == 0 && !overheadText.isEmpty()) {
            local.setOverheadText(overheadText);
            local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);

            overheadText = "";
        }
    }

    @Override
    public String getName() {
        return "Woodcutting";
    }

    @Override
    public Task evaluate(Bot bot) {
        final InventoryManager inventory = bot.getInventory();
        final GameObjectManager gameObjects = bot.getGameObjects();
        final Player local = client.getLocalPlayer();
        assert(local != null);

        switch(bot.getState()) {
            case Idle: {
                LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
                if(startPosition == null) {
                    overheadText = "Unable to find start position!";
                    return null;
                }

                if (inventory.isFull()) {
                    overheadText = "My inventory is full, I'm going to move to the bank";

                    currentTarget = -1;
                    if(gameObjects.get(bot.getBankTarget()) != null) {
                        return TaskUtil.interact(gameObjects.get(bot.getBankTarget()), MenuAction.GAME_OBJECT_SECOND_OPTION);
                    } else {
                        overheadText = "Bank is not in current scene!";
                        return null;
                    }
                }

                if (gameObjects.get(currentTarget) == null) {
                    Task nextTarget = chopNext(bot);
                    if(nextTarget != null)
                        return nextTarget;
                } else {
                    // Probably level up or something...
                    return TaskUtil.interact(gameObjects.get(currentTarget), MenuAction.GAME_OBJECT_FIRST_OPTION);
                }

                overheadText = "Ladadi ladada~";
            } break;

            case Woodcutting: {
                if (gameObjects.get(currentTarget) == null) {
                    overheadText = "Beat you to it!";
                    // Tree disappeared, but still chopping.
                    return chopNext(bot);
                }

                overheadText = "Chop, chop, chop...";
            } break;

            case Banking: {
                Axe axe = Axe.findActive(bot.getClient());

                return Task.chain(
                        TaskUtil.Bank.depositItems((id) -> id == (axe != null ? axe.getItemId() : -1)),
                        chopNext(bot)
                );
            }
        }

        return null;
    }

    @Override
    public void renderDebug(Graphics2D graphics, Bot bot) {
        final GameObjectManager gameObjects = bot.getGameObjects();

        final LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
        if(startPosition != null) {
            OverlayUtil.renderLocalPoint(bot.getClient(), graphics, startPosition, Color.YELLOW);
        }

        if(gameObjects.get(currentTarget) != null) {
            OverlayUtil.renderTileOverlay(graphics, gameObjects.get(currentTarget), "Current target", Color.GREEN);
        }
    }

    private Task chopNext(Bot bot) {
        final GameObjectManager gameObjects = bot.getGameObjects();
        final LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
        GameObject nextTarget = gameObjects.find(startPosition, obj -> TreeType.of(obj.getId()) == tree);

        if (nextTarget != null) {
            currentTarget = nextTarget.getHash();

            return TaskUtil.interact(nextTarget, MenuAction.GAME_OBJECT_FIRST_OPTION);
        } else {
            // TODO: 'Walk here' to startPosition
            // No trees around
            currentTarget = -1;
        }

        return null;
    }
}
