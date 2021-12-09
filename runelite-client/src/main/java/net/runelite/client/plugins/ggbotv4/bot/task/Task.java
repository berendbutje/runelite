package net.runelite.client.plugins.ggbotv4.bot.task;

import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.ggbotv4.bot.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface Task {
    TaskResult evaluate(Bot bot);

    default TaskResult block() {
        return TaskResult.continueAfter(this);
    }

    static Task chain(final List<Task> tasks) {
        return new Task() {
            private final List<Task> taskList = new ArrayList<>(tasks);

            @Override
            public TaskResult evaluate(Bot bot) {
                if(!taskList.isEmpty()) {
                    return TaskResult.continueAfter(taskList.remove(0));
                }

                return TaskResult.finished();
            }
        };
    }

    static Task chain(Task... tasks) {
        return chain(List.of(tasks));
    }

    static Task once(Runnable runnable) {
        return bot -> {
            runnable.run();

            return TaskResult.finished();
        };
    }

    static Task once(Runnable... runnable) {
        return new Task() {
            private List<Runnable> runnables = List.of(runnable);
            @Override
            public TaskResult evaluate(Bot bot) {
                if(runnables.size() > 0) {
                    return TaskResult.continueAfter(Task.once(runnables.remove(0)));
                }

                return TaskResult.finished();
            }
        };
    }

    @RequiredArgsConstructor
    class TaskIf {
        private final Predicate<Bot> conditional;

        public Task then(Task task) {
            return bot -> {
                if(conditional.test(bot)) {
                    return TaskResult.finishedAfter(task);
                } else {
                    return TaskResult.finished();
                }
            };
        }
    }

    static TaskIf when(Predicate<Bot> condition) {
        return new TaskIf(condition);
    }

}
