package net.runelite.client.plugins.ggbotv4.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.*;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.util.TaskUtil;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.util.List;
import java.util.*;

import static net.runelite.api.Constants.CLIENT_TICK_LENGTH;

@Slf4j
public class FishingScript implements Script {
    private final Client client;

    private final WorldPoint startPosition;
    private String overheadText = "";

    public FishingScript(Bot bot) {
        this.client = bot.getClient();

        startPosition = new WorldPoint(
                client.getLocalPlayer().getWorldLocation().getX(),
                client.getLocalPlayer().getWorldLocation().getY(),
                client.getLocalPlayer().getWorldLocation().getPlane()
        );
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        Player local = client.getLocalPlayer();
        if(local == null)
            return;

        if(local.getOverheadCycle() == 0 && !overheadText.isEmpty()) {
            local.setOverheadText(overheadText);
            local.setOverheadCycle(2000 / CLIENT_TICK_LENGTH);

            overheadText = "";
        }
    }

    @Override
    public String getName() {
        return "Fighting";
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

                    if(gameObjects.get(bot.getBankTarget()) != null) {
                        return TaskUtil.interact(gameObjects.get(bot.getBankTarget()), MenuAction.GAME_OBJECT_SECOND_OPTION);
                    } else {
                        overheadText = "Bank is not in current scene!";
                        return null;
                    }
                }

                if(local.getInteracting() != null &&
                        local.getInteracting() instanceof NPC &&
                        FishingSpot.findSpot(((NPC)local.getInteracting()).getId()) != null
                    ) {
                    overheadText = "Spetter pieter pater, lekker in het water. Ga maar vast naar huis...";
                    // Player has a target, and it's not dead, but player is idle.
                    return TaskUtil.interact((NPC)local.getInteracting(), MenuAction.NPC_FIRST_OPTION);
                } else {
                    Task nextFish = fishNext(bot);
                    if(nextFish != null)
                        return nextFish;
                }

                overheadText = "...";
            } break;

            case Banking: {
                LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
                if(startPosition == null) {
                    overheadText = "Unable to find start position!";
                    return null;
                }

                Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
                int net = ItemID.SMALL_FISHING_NET;

                List<Task> tasks = new ArrayList<>();
                Set<Integer> processed = new HashSet<>();
                for(int slot = 0; slot < items.length; slot++) {
                    Item item = items[slot];

                    if( item.getId() != net && !processed.contains(item.getId())) {
                        processed.add(item.getId());
                        tasks.add(TaskUtil.bankDepositAll(slot));
                    }
                }

                Task fishNext = fishNext(bot);
                if (fishNext != null) {
                    tasks.add(fishNext);
                }

                return Task.chain(tasks);
            }
        }

        return null;
    }

    @Override
    public void renderDebug(Graphics2D graphics, Bot bot) {
        final Player local = client.getLocalPlayer();
        assert(local != null);

        LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
        if(startPosition != null) {
            OverlayUtil.renderLocalPoint(bot.getClient(), graphics, startPosition, Color.YELLOW);
        }

        if(local.getInteracting() != null) {
            OverlayUtil.renderActorOverlay(graphics, local.getInteracting(), "Current target", Color.GREEN);
        }
    }

    private static final Set<Integer> FIGHTING_ANIMATIONS = Set.of(
            AnimationID.FIGHTING_WEAPON_SLASH, AnimationID.FIGHTING_WEAPON_STAB,
            AnimationID.ATTACKING_KICK, AnimationID.ATTACKING_PUNCH
    );

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {
        Player local = client.getLocalPlayer();

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (local == null || event.getActor() != local || inventory == null) {
            return;
        }

    }

    private Task fishNext(Bot bot) {
        LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
        if(startPosition == null) {
            overheadText = "Unable to find start position!";
            return null;
        }

        final NPCManager npcs = bot.getNpcs();

        // Player does not have a target.
        NPC nextTarget = npcs.find(startPosition,
                npc -> FishingSpot.findSpot(npc.getId()) != null
        );

        if (nextTarget != null) {
            FishingSpot spot = FishingSpot.findSpot(nextTarget.getId());
            overheadText = "Fishing for some " +
                    Arrays.toString(
                            Arrays.stream(spot.getItemIds()).mapToObj(id -> client.getItemDefinition(id).getName())
                                    .toArray()
                    );

            return TaskUtil.interact(nextTarget, MenuAction.NPC_FIRST_OPTION);
        } else {
            overheadText = "No fishing spot around.";
        }

        return null;
    }
}
