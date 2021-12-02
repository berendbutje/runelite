package net.runelite.client.plugins.ggbotv4.bot.action;

import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;

public class ActionInteractGameObject implements Action {
    private final GameObject target;
    private final MenuAction action;

    public ActionInteractGameObject(GameObject target, MenuAction action) {
        this.target = target;
        this.action = action;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ActionError run() {
        return null;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
