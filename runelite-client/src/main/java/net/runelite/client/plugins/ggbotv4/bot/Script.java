package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.client.plugins.ggbotv4.bot.task.Task;

public interface Script {
    ScriptError OK = null;

    String getName();

    /**
     * Gets the next Task the executor can perform for the script.
     */
    Task evaluate(Bot bot);
}
