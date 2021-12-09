package net.runelite.client.plugins.ggbotv4.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.InventoryManager;
import net.runelite.client.plugins.ggbotv4.bot.NPCManager;
import net.runelite.client.plugins.ggbotv4.bot.Script;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.util.TaskUtil;
import net.runelite.client.plugins.ggbotv4.util.InteractionUtil;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import java.awt.*;
import java.util.List;
import java.util.*;

import static net.runelite.api.Constants.CLIENT_TICK_LENGTH;

@Slf4j
public class FightingScript implements Script {
    private final Client client;
    private final int targetNpcId;

    private final WorldPoint startPosition;
    private String overheadText = "";

    private final List<ItemStack> loot = new ArrayList<>();
    private final Set<Integer> ignoredItems = new HashSet<>();

    public FightingScript(Bot bot, int npcId) {
        this.client = bot.getClient();
        this.targetNpcId = npcId;

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
        final NPCManager npcs = bot.getNpcs();
        final Player local = client.getLocalPlayer();
        assert(local != null);

        switch(bot.getState()) {
            case Idle: {
                LocalPoint startPosition = LocalPoint.fromWorld(client, this.startPosition);
                if(startPosition == null) {
                    overheadText = "Unable to find start position!";
                    return null;
                }

                List<NPC> npcsAttackingMe = npcs.findList(startPosition,
                        npc -> !npc.isDead() && npc.getInteracting() == local
                );

                if(local.getInteracting() != null && local.getInteracting() instanceof NPC && !local.isDead()) {
                    overheadText = "En hier, en daar, en hier! " + local.getInteracting().getName() + "!";
                    // Player has a target, and it's not dead, but player is idle.
                    return null;
                } else if(local.getInteracting() == null && npcsAttackingMe.size() > 0) {
                    return TaskUtil.interact(npcsAttackingMe.get(0), MenuAction.NPC_SECOND_OPTION);
                } else {
//                        MenuOption=Take MenuTarget=<col=ff9040>Feather Id=314 Opcode=GROUND_ITEM_THIRD_OPTION/20 Param0=47 Param1=68 CanvasX=372 CanvasY=336
//                        MenuOption=Take MenuTarget=<col=ff9040>Bones Id=526 Opcode=GROUND_ITEM_THIRD_OPTION/20 Param0=47 Param1=68 CanvasX=370 CanvasY=378
                    while(loot.size() > 0) {
                        ItemStack item = loot.remove(0);

                        int sceneX = item.getLocation().getSceneX();
                        int sceneY = item.getLocation().getSceneY();

                        if(!ignoredItems.contains(item.getId())) {
                            return TaskUtil.interact(item.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION, sceneX, sceneY);
                        }
                    }

                    // Player does not have a target.
                    NPC nextTarget = npcs.find(startPosition,
                            npc -> npc.getId() == targetNpcId &&
                                    !npc.isDead() &&
                                    (npc.getInteracting() == null || npc.getInteracting() == local)
                    );

                    if (nextTarget != null) {
                        overheadText = "Well we couldn't find cancer, but I found this " + nextTarget.getName() + " with cancer!";

                        return TaskUtil.interact(nextTarget, MenuAction.NPC_SECOND_OPTION);
                    }
                }

                overheadText = "Making movies, making songs 'n fight-in' round the world!";
            } break;

            case Moving: {
                overheadText = "Making movies, making songs 'n fight-in' round the world!";
            } break;
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

        int animId = local.getAnimation();
        if(FIGHTING_ANIMATIONS.contains(animId)) {
            int itemSlot = -1;
            int itemId = 0;

            boolean didBuryBones = false;
            for(int i = 0; i < inventory.size(); i++) {
                Item item = inventory.getItem(i);

                if(item != null) {
                    if(item.getId() == ItemID.BONES && !didBuryBones) {
                        InteractionUtil.executeMenuAction(
                                item.getId(),
                                MenuAction.ITEM_FIRST_OPTION.getId(),
                                i,
                                WidgetInfo.INVENTORY.getId(),
                                0,
                                0
                        );

                        didBuryBones = true;
                    } else if(ignoredItems.contains(item.getId())) {
                        InteractionUtil.executeMenuAction(
                                item.getId(),
                                MenuAction.ITEM_FIFTH_OPTION.getId(),
                                i,
                                WidgetInfo.INVENTORY.getId(),
                                0,
                                0
                        );
                    }
                }
            }

            if(itemSlot >= 0) {
                InteractionUtil.executeMenuAction(
                        itemId,
                        MenuAction.ITEM_FIFTH_OPTION.getId(),
                        itemSlot,
                        WidgetInfo.INVENTORY.getId(),
                        0,
                        0
                );
            }
        }
    }

    @Subscribe
    public void onNpcLootReceived(NpcLootReceived event) {
        log.info("Received loot: {}", event.getItems().toString());

        loot.addAll(event.getItems());
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if(event.getMenuAction().getId() == MenuAction.EXAMINE_ITEM_GROUND.getId()) {
            MenuEntry[] menuEntries = client.getMenuEntries();
            menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

            MenuEntry menuEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();

            String option = ignoredItems.contains(event.getId()) ? "Pick up" : "Ignore";

            menuEntry.setId(event.getId());
            menuEntry.setOption(option);
            menuEntry.setTarget(ColorUtil.wrapWithColorTag(Text.removeTags(event.getTarget()), Color.ORANGE));
            menuEntry.setType(MenuAction.PRIO_GGBOT.getId());

            client.setMenuEntries(menuEntries);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        final Player local = client.getLocalPlayer();
        assert (local != null);

        String option = Text.removeTags(event.getMenuOption());
        if (event.getMenuAction().getId() == MenuAction.PRIO_GGBOT.getId()) {
            if(option.equalsIgnoreCase("Pick up")) {
                ignoredItems.remove(event.getId());
            } else if(option.equalsIgnoreCase("Ignore")) {
                ignoredItems.add(event.getId());
            }
        }
    }
}
