package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.Getter;
import net.runelite.client.plugins.ggbotv4.bot.action.ActionError;

public class TaskResult {
    public static final TaskResult OK = new TaskResult(null);
    public static final TaskResult FINISHED = new TaskResult(null);

    @Getter
    private ActionError error;

    public TaskResult(ActionError error) {
        this.error = error;
    }

    public static final TaskResult of(ActionError error) {
        return new TaskResult(error);
    }

    public boolean isOk() {
        return error == ActionError.OK;
    }

    public boolean isError() {
        return error != ActionError.OK;
    }

}
