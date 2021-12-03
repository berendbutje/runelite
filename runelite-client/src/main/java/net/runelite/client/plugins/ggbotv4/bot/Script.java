package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.client.plugins.ggbotv4.bot.task.Task;
import net.runelite.client.plugins.ggbotv4.plugin.BotPlugin;

import java.awt.*;

public interface Script {
    String getName();

    /**
     * Gets the next Task the executor can perform for the script.
     * @return
     */
    Task evaluate(BotPlugin bot);

    void renderDebug(Graphics2D graphics, BotPlugin bot);
}
