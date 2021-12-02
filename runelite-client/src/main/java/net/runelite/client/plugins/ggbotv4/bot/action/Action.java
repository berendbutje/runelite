package net.runelite.client.plugins.ggbotv4.bot.action;

/**
 * Actions are single interactions that can be done in the game.
 */
public interface Action {
    String getName();
    ActionError run();

    boolean isStarted();
    boolean isFinished();
}
