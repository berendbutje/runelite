package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.Getter;

public class TaskResult {
    @Getter
    private final Task next;
    @Getter
    private final TaskError error;

    public TaskResult(Task next, TaskError error) {
        this.next = next;
        this.error = error;
    }

    public static TaskResult fatalError(TaskError error) {
        return new TaskResult(null, error);
    }
    public static TaskResult finished() {
        return new TaskResult(null, null);
    }
    public static TaskResult continueAfter(Task next) {
        return new TaskResult(next, null);
    }

    public boolean isFinished() { return this.next == null && this.error == null; }
    public boolean isError() {
        return error != null;
    }
}
