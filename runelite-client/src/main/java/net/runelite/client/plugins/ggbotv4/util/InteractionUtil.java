package net.runelite.client.plugins.ggbotv4.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;

import java.awt.*;
import java.awt.event.KeyEvent;

@Slf4j
public class InteractionUtil {
    public static void pressKey(int key) {
        final Client client = RuneLite.getInjector().getInstance(Client.class);

        client.getCanvas().dispatchEvent(
                new KeyEvent(
                        client.getCanvas(),
                        KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis() - (long)Math.floor(Math.random() * 500),
                        0,
                        key,
                        (char)0
                )
        );
    }

    public static void clickWidget(Widget widget, MenuAction action, int id, int param0) {
        assert(widget != null);

        Point screenPosition = new Point(
                widget.getCanvasLocation().getX() + (int)(Math.floor(Math.random() * widget.getWidth())),
                widget.getCanvasLocation().getY() + (int)(Math.floor(Math.random() * widget.getHeight()))
        );

//        WidgetInfo info = WidgetInfo.find(widget.getId());
//        log.info("clickWidget(): Clicking on widget {} with {} at {}, {}", info.name(), action.name(), screenPosition.getX(), screenPosition.getY());

        InteractionUtil.executeMenuAction(id, action.getId(), param0, widget.getId(), screenPosition.getX(), screenPosition.getY());
    }

    public static void executeMenuAction(int objectId, int actionId, int param0, int param1, int mouseX, int mouseY) {
        Client client = RuneLite.getInjector().getInstance(Client.class);
        ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);

        clientThread.invoke(() -> {
            client.setMouseIdleTicks(0);
            client.getPacketWriter().sendClickPacket(mouseX, mouseY, (long)(Math.floor(Math.random() * 400)), 1);
            client.sendMenuAction(
                    param0,
                    param1,
                    actionId,
                    objectId,
                    "",
                    "",
                    mouseX,
                    mouseY
            );
        });
    }

    public static void executeMenuAction(GameObject target, MenuAction action) {
        Client client = RuneLite.getInjector().getInstance(Client.class);

        Shape hull = target.getConvexHull();
        net.runelite.api.Point screenPosition;
        if(hull != null) {
            screenPosition = new net.runelite.api.Point((int)hull.getBounds().getCenterX(), (int)hull.getBounds().getCenterY());
        } else {
            screenPosition = new Point(
                    (int)Math.floor(Math.random() * client.getViewportWidth()),
                    (int)Math.floor(Math.random() * client.getViewportHeight())
            );
        }

        executeMenuAction(
                target.getId(),
                action.getId(),
                target.getSceneMinLocation().getX(),
                target.getSceneMinLocation().getY(),
                screenPosition.getX(),
                screenPosition.getY()
        );
    }

    //|MenuAction|: MenuOption=Attack MenuTarget=<col=ffff00>Chicken<col=c0ff00>  (level-1) Id=2612 Opcode=NPC_SECOND_OPTION/10 Param0=0 Param1=0 CanvasX=397 CanvasY=378
    public static void executeMenuAction(NPC target, MenuAction action) {
        Client client = RuneLite.getInjector().getInstance(Client.class);

        Shape hull = target.getConvexHull();
        net.runelite.api.Point screenPosition;
        if(hull != null) {
            screenPosition = new net.runelite.api.Point((int)hull.getBounds().getCenterX(), (int)hull.getBounds().getCenterY());
        } else {
            screenPosition = new Point(
                    (int)Math.floor(Math.random() * client.getViewportWidth()),
                    (int)Math.floor(Math.random() * client.getViewportHeight())
            );
        }

        executeMenuAction(
                target.getIndex(),
                action.getId(),
                0,
                0,
                screenPosition.getX(),
                screenPosition.getY()
        );
    }


}
