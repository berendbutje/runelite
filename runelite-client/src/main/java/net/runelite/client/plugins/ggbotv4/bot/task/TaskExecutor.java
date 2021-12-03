package net.runelite.client.plugins.ggbotv4.bot.task;

import net.runelite.client.plugins.ggbotv4.plugin.BotPlugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TaskExecutor {
    private final List<Task> tasks = Collections.synchronizedList(new LinkedList<>());

    public void execute(BotPlugin bot) {
        synchronized (tasks) {
            while (tasks.size() > 0) {
                Task task = tasks.get(0);

                TaskResult result = task.evaluate(bot);
                if (result.getNext() == task) {
                    // Task is blocking, retry next tick.
                    break;
                } else if (result.getNext() != null) {
                    // Task is blocked by another task, try to execute it.
                    tasks.add(0, result.getNext());
                } else if (result.isError()) {
                    handleTaskError(task, result.getError());

                    tasks.clear();
                } else if (result.isFinished()) {
                    // Task is finished, try to start next task.
                    tasks.remove(0);
                }
            }
        }
    }

    private void handleTaskError(Task task, TaskError error) {
        System.out.println("Task " + task.getClass().getSimpleName() + " has encountered an error: " + error.getDescription());
    }

    public void clear() {
        synchronized (tasks) {
            tasks.clear();
        }
    }

    public void add(Task t) {
        if(t != null) {
            synchronized (tasks) {
                tasks.add(t);
            }
        }
    }

    public int size() {
        synchronized (tasks) {
            return tasks.size();
        }
    }
}
