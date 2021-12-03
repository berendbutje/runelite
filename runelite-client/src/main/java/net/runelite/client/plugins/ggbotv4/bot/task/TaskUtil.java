package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.ggbotv4.bot.task.error.ErrorTimeout;
import net.runelite.client.plugins.ggbotv4.plugin.BotPlugin;
import net.runelite.client.plugins.ggbotv4.util.InteractionUtil;

import java.util.function.Predicate;

public class TaskUtil {
    public static Task bankDepositAll(int slot) {
        return bot -> {
            int param1 = WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId();
            int objectId = 8;
            int actionId = MenuAction.CC_OP_LOW_PRIORITY.getId();

            Widget widget = bot.getClient().getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
            Point screenPosition;
            if(widget != null) {
                Widget item = widget.getChild(slot);
                if(item != null) {
                    screenPosition = new Point(
                            (int)item.getBounds().getCenterX(),
                            (int)item.getBounds().getCenterY()
                    );
                } else {
                    screenPosition = new Point(
                            widget.getCanvasLocation().getX() + (int)(Math.floor(Math.random() * widget.getWidth())),
                            widget.getCanvasLocation().getY() + (int)(Math.floor(Math.random() * widget.getHeight()))
                    );
                }
            } else {
                screenPosition = new Point(
                        (int)Math.floor(Math.random() * bot.getClient().getViewportWidth()),
                        (int)Math.floor(Math.random() * bot.getClient().getViewportHeight())
                );
            }

            InteractionUtil.executeMenuAction(objectId, actionId, slot, param1, screenPosition.getX(), screenPosition.getY());

            return TaskResult.finished();
        };
    }

    public static Task interact(GameObject object, MenuAction action) {
        return bot -> {
            if(object == null) {
                return TaskResult.fatalError(() -> "Interact<GameObject>: GameObject is null!");
            }

            InteractionUtil.executeMenuAction(object, action);

            return TaskResult.finished();
        };
    }


// |MenuAction|: MenuOption=Close MenuTarget= Id=1 Opcode=CC_OP/57 Param0=11 Param1=786434 CanvasX=501 CanvasY=24
// |MenuAction|: MenuOption=Withdraw-1 MenuTarget=Withdraw-1 Id=1 Opcode=CC_OP/57 Param0=25 Param1=786445 CanvasX=311 CanvasY=427

    public static Task clickWidgetChild(WidgetInfo widget, int childIndex) {
        return new TaskClickWidgetChild(widget, childIndex);
    }

    @Data
    private static final class TaskClickWidgetChild implements Task {
        private final WidgetInfo widget;
        private final int childIndex;

        @Override
        public TaskResult evaluate(BotPlugin bot) {
            Client client = RuneLite.getInjector().getInstance(Client.class);
            int actionId = MenuAction.CC_OP.getId();

            Widget parent = client.getWidget(widget);

            Point screenPosition;
            if(parent != null) {
                Widget child = parent.getChild(childIndex);
                if(child != null) {
                    screenPosition = new Point(
                            (int)child.getBounds().getCenterX(),
                            (int)child.getBounds().getCenterY()
                    );
                } else {
                    screenPosition = new Point(
                            parent.getCanvasLocation().getX() + (int)(Math.floor(Math.random() * parent.getWidth())),
                            parent.getCanvasLocation().getY() + (int)(Math.floor(Math.random() * parent.getHeight()))
                    );
                }
            } else {
                screenPosition = new Point(
                        (int)Math.floor(Math.random() * client.getViewportWidth()),
                        (int)Math.floor(Math.random() * client.getViewportHeight())
                );
            }

            InteractionUtil.executeMenuAction(1, actionId, childIndex, parent.getId(), screenPosition.getX(), screenPosition.getY());

            return TaskResult.finished();
        }
    }

    public static Task awaitGameTick() {
        return await(new PredicateGameTick(), 10000);
    }

    private static class PredicateGameTick implements Predicate<BotPlugin> {
        private int targetTick = -1;

        @Override
        public boolean test(BotPlugin botPlugin) {
            Client client = RuneLite.getInjector().getInstance(Client.class);

            if (targetTick == -1)
                targetTick = client.getTickCount() + 1;

            return client.getTickCount() >= targetTick;
        }
    }

    public static Task await(Predicate<BotPlugin> done, long timeout) {
        return new AwaitTask(done, timeout);
    }

    @Data
    private static class AwaitTask implements Task {
        private long start = -1;

        private final Predicate<BotPlugin> done;
        private final long timeout;

        @Override
        public TaskResult evaluate(BotPlugin bot) {
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
                return TaskResult.continueAfter(this);
            }
        }
    }

}
