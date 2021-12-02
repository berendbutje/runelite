package net.runelite.client.plugins.ggbotv4.bot.action;

import com.google.inject.Inject;
import net.runelite.api.Client;

public class ActionMove implements Action {
    private boolean started;

    @Inject
    private Client client;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ActionError run() {
        started = true;

        return ActionError.OK;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
