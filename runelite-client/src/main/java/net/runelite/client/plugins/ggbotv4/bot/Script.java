package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.client.plugins.ggbotv4.bot.task.Task;

import java.awt.*;

public interface Script {
    String getName();

    /**
     * Gets the next Task the executor can perform for the script.
     * @return
     * @param bot
     */
    Task evaluate(Bot bot);

    default void stop() { }

    void renderDebug(Graphics2D graphics, Bot bot);

}
