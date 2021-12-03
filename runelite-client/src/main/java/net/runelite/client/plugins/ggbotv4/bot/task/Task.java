package net.runelite.client.plugins.ggbotv4.bot.task;

import net.runelite.client.plugins.ggbotv4.plugin.BotPlugin;

import java.util.List;

public interface Task {
    TaskResult evaluate(BotPlugin bot);

    static Task of(final List<Task> tasks) {
        return new Task() {
            private List<Task> taskList = tasks;

            @Override
            public TaskResult evaluate(BotPlugin bot) {
                if(!taskList.isEmpty()) {
                    return TaskResult.continueAfter(taskList.remove(0));
                }

                return TaskResult.finished();
            }
        };
    }

    static Task of(Task... tasks) {
        return of(List.of(tasks));
    }
}
