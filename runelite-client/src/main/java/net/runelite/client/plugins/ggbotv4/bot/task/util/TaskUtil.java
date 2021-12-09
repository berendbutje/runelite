package net.runelite.client.plugins.ggbotv4.bot.task.util;

import com.google.common.primitives.Ints;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.ggbotv4.bot.Bot;
import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskResult;
import net.runelite.client.plugins.ggbotv4.bot.task.error.ErrorTimeout;
import net.runelite.client.plugins.ggbotv4.util.InteractionUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class TaskUtil {

    public static class Bank {
        /**
         * Deposits all items currently in the inventory into the bank.
         * @return
         */
        public static Task depositItems() {
            // |MenuAction|: MenuOption=Deposit inventory MenuTarget= Id=1 Opcode=CC_OP/57 Param0=-1 Param1=BANK_DEPOSIT_INVENTORY CanvasX=443 CanvasY=510
            return clickWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY, MenuAction.CC_OP, 1, -1);
        }

        private static Task depositItemAll(int slot) {
            // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Bronze bar</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=617 CanvasY=491
            return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP_LOW_PRIORITY, 8, slot);
        }

        private static Task depositItem1(int slot) {
            // |MenuAction|: MenuOption=Deposit-1 MenuTarget=<col=ff9040>Tin ore</col> Id=2 Opcode=CC_OP/57 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=597 CanvasY=408
            return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP, 2, slot);
        }

        private static Task depositItem5(int slot) {
            // |MenuAction|: MenuOption=Deposit-5 MenuTarget=<col=ff9040>Tin ore</col> Id=4 Opcode=CC_OP/57 Param0=1 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=651 CanvasY=426
            return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP, 4, slot);
        }

        private static Task depositItem10(int slot) {
            // |MenuAction|: MenuOption=Deposit-10 MenuTarget=<col=ff9040>Tin ore</col> Id=5 Opcode=CC_OP/57 Param0=6 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=699 CanvasY=490
            return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP, 5, slot);
        }

//        private static Task depositItemX(int slot) {
//            // |MenuAction|: MenuOption=Deposit-X MenuTarget=<col=ff9040>Tin ore</col> Id=7 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=604 CanvasY=475
//        }

        private static Task withdrawItemAll(int slot) {
            // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Bronze bar</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=617 CanvasY=491
            return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP_LOW_PRIORITY, 8, slot);
        }

        private static Task withdrawItem1(int slot) {
            // |MenuAction|: MenuOption=Withdraw-1 MenuTarget=<col=ff9040>Tin ore</col> Id=1 Opcode=CC_OP/57 Param0=14 Param1=BANK_ITEM_CONTAINER CanvasX=379 CanvasY=157
            return clickWidget(WidgetInfo.BANK_ITEM_CONTAINER, MenuAction.CC_OP, 1, slot);
        }

        private static Task withdrawItem5(int slot) {
            // |MenuAction|: MenuOption=Withdraw-5 MenuTarget=<col=ff9040>Tin ore</col> Id=3 Opcode=CC_OP/57 Param0=14 Param1=BANK_ITEM_CONTAINER CanvasX=392 CanvasY=177
            return clickWidget(WidgetInfo.BANK_ITEM_CONTAINER, MenuAction.CC_OP, 3, slot);
        }

        private static Task withdrawItem10(int slot) {
            // |MenuAction|: MenuOption=Withdraw-10 MenuTarget=<col=ff9040>Tin ore</col> Id=4 Opcode=CC_OP/57 Param0=14 Param1=BANK_ITEM_CONTAINER CanvasX=391 CanvasY=187
            return clickWidget(WidgetInfo.BANK_ITEM_CONTAINER, MenuAction.CC_OP, 4, slot);
        }

        /**
         * Withdraw-{NUMBER}
         * @param slot
         * @return
         */
        private static Task withdrawItemAmount(int slot) {
            // |MenuAction|: MenuOption=Withdraw-14 MenuTarget=<col=ff9040>Tin ore</col> Id=5 Opcode=CC_OP/57 Param0=14 Param1=BANK_ITEM_CONTAINER CanvasX=389 CanvasY=200
            return clickWidget(WidgetInfo.BANK_ITEM_CONTAINER, MenuAction.CC_OP, 5, slot);
        }

        private static Task withdrawItemX(int slot) {
            // |MenuAction|: MenuOption=Withdraw-X MenuTarget=<col=ff9040>Tin ore</col> Id=6 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=14 Param1=BANK_ITEM_CONTAINER CanvasX=375 CanvasY=210
            return clickWidget(WidgetInfo.BANK_ITEM_CONTAINER, MenuAction.CC_OP_LOW_PRIORITY, 6, slot);
        }

        /**
         * Deposits all equipment currently in use into the bank.
         * @return
         */
        public static Task depositEquipment() {
            // |MenuAction|: MenuOption=Deposit worn items MenuTarget= Id=1 Opcode=CC_OP/57 Param0=-1 Param1=BANK_DEPOSIT_EQUIPMENT CanvasX=494 CanvasY=549
            return clickWidget(WidgetInfo.BANK_DEPOSIT_EQUIPMENT, MenuAction.CC_OP, 1, -1);
        }

        /**
         * Deposits all items currently in the inventory, except those matching the predicate, into the bank.
         * @return
         */
        public static Task depositItems(Predicate<Integer> isException) {
            final Client client = RuneLite.getInjector().getInstance(Client.class);

            return new Task() {
                private final Set<Integer> processed = new HashSet<>();
                private int currentSlot = 0;

                @Override
                public TaskResult evaluate(Bot bot) {
                    ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
                    if(inventory == null) {
                        // Inventory is empty.
                        return TaskResult.finished();
                    }

                    Item[] items = inventory.getItems();
                    for(; currentSlot < items.length; currentSlot++) {
                        Item item = items[currentSlot];

                        if(item != null && item.getId() != -1 &&
                                !processed.contains(item.getId()) &&
                                !isException.test(item.getId())) {
                            processed.add(item.getId());

                            log.info("Depositing all {}", client.getItemDefinition(item.getId()).getName());
                            return TaskResult.continueAfter(
                                    depositItemAll(currentSlot++)
                            );
                        }
                    }

                    return TaskResult.finished();
                }
            };
        }

//        public static Task depositItem(int itemId) {
//
//        }
//
//        public static Task depositItem(int itemId, int amount) {
//
//        }

        public static Task withdrawItems(int amount, int... itemId) {
            final Client client = RuneLite.getInjector().getInstance(Client.class);

            return new Task() {
                private final List<Integer> items = new ArrayList<>(Ints.asList(itemId));

                @Override
                public TaskResult evaluate(Bot bot) {
                    if(items.isEmpty())
                        return TaskResult.finished();

                    ItemContainer bank = client.getItemContainer(InventoryID.BANK);
                    if (bank == null)
                        return TaskResult.fatalError(() -> "withdrawItems(): Unable to find bank container.");

                    int slot = -1;
                    for (int i = 0; i < bank.size(); i++) {
                        Item item = bank.getItem(i);

                        if (
                                item != null &&
                                items.contains(item.getId()) &&
                                ArrayUtils.contains(itemId, item.getId())
                        ) {
                            items.remove((Integer) item.getId());
                            slot = i;
                            break;
                        }
                    }

                    if (slot == -1) {
                        return TaskResult.fatalError(() -> "withdrawItems(): Unable to any items of " +
                                items.stream().map(id -> client.getItemDefinition(id).getName()).collect(Collectors.toList()));
                    }

                    switch (amount) {
                        case 1:
                            return TaskResult.continueAfter(withdrawItem1(slot));

                        case 5:
                            return TaskResult.continueAfter(withdrawItem5(slot));

                        case 10:
                            return TaskResult.continueAfter(withdrawItem10(slot));

                        default: {
                            // Arbitrary amounts.
                            int xAmount = client.getVar(Varbits.X_AMOUNT);
                            if (xAmount == amount) {
                                // If the right amount is set already, use the shortcut.
                                return TaskResult.continueAfter(withdrawItemAmount(slot));
                            } else {
                                // Set the correct amount
                                return TaskResult.continueAfter(Task.chain(
                                        withdrawItemX(slot),
                                        awaitWidget(WidgetInfo.CHATBOX_FULL_INPUT),
                                        Task.once(() -> {
                                            client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 0);
                                            client.getPacketWriter().sendSubmitAmount(amount);
                                            client.setVarbit(Varbits.X_AMOUNT, amount);
                                        })
                                ));
                            }
                        }
                    }
                }
            };
        }
    }

    public static Task bankDepositAll(int slot) {
        log.info("Depositing all at slot {}", slot);
        // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Adamant axe</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=609 CanvasY=485
        // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Mithril axe</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=1 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=641 CanvasY=482
        // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Clue scroll (beginner)</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=2 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=667 CanvasY=482
        // |MenuAction|: MenuOption=Deposit-All MenuTarget=<col=ff9040>Bronze bar</col> Id=8 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=0 Param1=BANK_INVENTORY_ITEMS_CONTAINER CanvasX=617 CanvasY=491
        return clickWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER, MenuAction.CC_OP_LOW_PRIORITY, 8, slot);
    }

    public static Task interact(GameObject object, MenuAction action) {
        return bot -> {
            if(object == null)
                return TaskResult.fatalError(() -> "Interact<GameObject>: GameObject is null!");

            InteractionUtil.executeMenuAction(object, action);
            return TaskResult.finished();
        };
    }

    public static Task interact(NPC npc, MenuAction action) {
        return bot -> {
            if(npc == null)
                return TaskResult.fatalError(() -> "Interact<NPC>: NPC is null!");

            InteractionUtil.executeMenuAction(npc, action);
            return TaskResult.finished();
        };
    }

    public static Task interact(int id, MenuAction action, int sceneX, int sceneY) {
        return bot -> {
            InteractionUtil.executeMenuAction(id, action.getId(), sceneX, sceneY, 0, 0);

            return TaskResult.finished();
        };
    }

    public static Task clickWidget(WidgetInfo widgetInfo, MenuAction action, int id, int param0) {
        return clickWidget(widgetInfo.getId(), action, id, param0);
    }

    public static Task clickWidget(int widgetId, MenuAction action, int id, int param0) {
        Client client = RuneLite.getInjector().getInstance(Client.class);

        return bot -> {
            Widget widget = client.getWidget(widgetId);
            if(widget == null) {
                WidgetInfo info = WidgetInfo.find(widgetId);
                return TaskResult.fatalError(() -> "clickWidget(): " + (info != null ? info.name() : widgetId) + " is null");
            }

            WidgetInfo info = WidgetInfo.find(widgetId);
            log.info("clickWidget(): Clicking on {}", (info != null ? info.name() : widgetId));

            InteractionUtil.clickWidget(widget, action, id, param0);
            return TaskResult.finished();
        };
    }

    public static Task clickWidgetChild(WidgetInfo parent, int childIndex, MenuAction action, int id, int param0) {
        Client client = RuneLite.getInjector().getInstance(Client.class);

        return bot -> {
            Widget widget = client.getWidget(parent);
            if(widget == null)
                return TaskResult.fatalError(() -> "clickWidgetChild: Parent is null");

            Widget child = widget.getChild(childIndex);
            if(child == null)
                return TaskResult.fatalError(() -> "clickWidgetChild: Child at " + childIndex + " is null");

            InteractionUtil.clickWidget(child, action, id, param0);
            return TaskResult.finished();
        };
    }

    public static Task awaitGameTick() {
        return await(new PredicateGameTick(), 10000);
    }

    public static Task awaitWidget(WidgetInfo widgetInfo) {
        return awaitWidget(widgetInfo.getId());
    }

    public static Task awaitWidget(int widgetId) {
        Client client = RuneLite.getInjector().getInstance(Client.class);

        return await(bot -> {
            Widget widget = client.getWidget(widgetId);

            // Keep polling the widget.
            return widget != null && !widget.isHidden();
        }, 60 * 1000);

    }

    private static class PredicateGameTick implements Predicate<Bot> {
        private int targetTick = -1;

        @Override
        public boolean test(Bot botPlugin) {
            Client client = RuneLite.getInjector().getInstance(Client.class);

            if (targetTick == -1)
                targetTick = client.getTickCount() + 1;

            return client.getTickCount() >= targetTick;
        }
    }

    public static Task await(Predicate<Bot> done, long timeout) {
        return new AwaitTask(done, timeout);
    }

    @Data
    private static class AwaitTask implements Task {
        private long start = -1;

        private final Predicate<Bot> done;
        private final long timeout;

        @Override
        public TaskResult evaluate(Bot bot) {
            if(start == -1) {
                start = System.currentTimeMillis();
            }

            long now = System.currentTimeMillis();
            long delta = now - start;

            if(delta >= timeout) {
                return TaskResult.fatalError(new ErrorTimeout(done));
            }

            if(done.test(bot)) {
                return TaskResult.finished();
            } else {
                System.out.println("Awaiting... " + delta + " / " + timeout);
                return this.block();
            }
        }
    }

}
