package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.ggbotv4.bot.action.ActionError;

public class TaskExecutor {
    @Setter
    @Getter
    private Task task = null;

    public void execute() {
        if(task != null) {
            TaskResult result = task.run();
            if(result.isError()) {
                handleTaskError(task, result.getError());

                task = null;
            } else {
                if(result == TaskResult.FINISHED) {
                    // Task done.
                    task = null;
                } else {
                    // Ok, keep running.
                }
            }
        }
    }

    private void handleTaskError(Task task, ActionError error) {

    }
}
