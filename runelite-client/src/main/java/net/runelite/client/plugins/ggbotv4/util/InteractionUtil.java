package net.runelite.client.plugins.ggbotv4.util;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;

import java.awt.*;

public class InteractionUtil {
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


}
