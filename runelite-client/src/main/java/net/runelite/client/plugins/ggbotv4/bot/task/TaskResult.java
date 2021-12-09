package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.Getter;

public class TaskResult {
    public enum State {
        Ok,
        Error,
        Finished,
    }

    @Getter
    private final State state;

    @Getter
    private final Task next;

    @Getter
    private final TaskError error;

    private TaskResult(State state, Task next, TaskError error) {
        this.state = state;
        this.next = next;
        this.error = error;
    }

    public static TaskResult fatalError(TaskError error) {
        return new TaskResult(State.Error,null, error);
    }

    public static TaskResult finished() {
        return new TaskResult(State.Finished, null, null);
    }

    public static TaskResult finishedAfter(final Task t) {
        return new TaskResult(State.Finished, t, null);
    }

    public static TaskResult continueAfter(Task next) {
        return new TaskResult(State.Ok, next, null);
    }
}
