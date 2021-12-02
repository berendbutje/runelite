package net.runelite.client.plugins.ggbotv4.bot.task;

import net.runelite.client.plugins.ggbotv4.bot.action.Action;
import net.runelite.client.plugins.ggbotv4.bot.action.ActionAsync;
import net.runelite.client.plugins.ggbotv4.bot.action.ActionError;

import java.util.List;

public class Task {
    private final List<Action> actions;

    public Task(Action... actions) {
        this.actions = List.of(actions);
    }

    public TaskResult run() {
        while(!actions.isEmpty()) {
            Action action = actions.get(0);
            // Execute all asynchronous actions until a sync action comes along.
            if(action instanceof ActionAsync) {
                ActionError error = action.run();
                if(error != ActionError.OK) {
                    return TaskResult.of(error);
                } else {
                    actions.remove(0);
                }
            } else {
                // Sync action, wait till it's done.
                if(!action.isStarted()) {
                    ActionError error = action.run();
                    if(error != ActionError.OK) {
                        return TaskResult.of(error);
                    }
                } else if(action.isFinished()) {
                    actions.remove(0);
                } else {
                    // Wait till task is done.
                    return TaskResult.OK;
                }
            }
        }

        return TaskResult.FINISHED;
    }
}
