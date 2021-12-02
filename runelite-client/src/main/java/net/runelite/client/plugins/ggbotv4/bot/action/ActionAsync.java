package net.runelite.client.plugins.ggbotv4.bot.action;

/**
 * Whether this task can be asynchronously executed.
 *
 * An example of this is toggling run, this can be done always.
 * This tells the executor it can execute this action parallel to other actions.
 */
public interface ActionAsync extends Action {
    @Override
    default boolean isFinished() {
        return true;
    }

    @Override
    default boolean isStarted() {
        return true;
    }
}
