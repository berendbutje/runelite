package net.runelite.client.plugins.ggbotv4.bot.task;

import net.runelite.client.plugins.ggbotv4.bot.Bot;

import java.util.LinkedList;

public class TaskExecutor {
    private final LinkedList<Task> tasks = new LinkedList<>();

    public void execute(Bot bot) {
        while (!tasks.isEmpty()) {
            Task task = tasks.getFirst();

            TaskResult result = task.evaluate(bot);
            if(result.getState() == TaskResult.State.Ok) {
                if (result.getNext() == task) {
                    // Task is blocking, retry next tick.
                    break;
                } else if (result.getNext() != null) {
                    // Task is blocked by another task, try to execute it.
                    tasks.addFirst(result.getNext());
                }
            } else if (result.getState() == TaskResult.State.Error) {
                handleTaskError(task, result.getError());

                tasks.clear();
            } else if (result.getState() == TaskResult.State.Finished) {
                // Task is finished, try to start next task.
                tasks.removeFirst();

                if (result.getNext() != null) {
                    tasks.addFirst(result.getNext());
                }
            }
        }
    }

    private void handleTaskError(Task task, TaskError error) {
        System.out.println("Task " + task.getClass().getSimpleName() + " has encountered an error: " + error.getDescription());
    }

    public void clear() {
        tasks.clear();
    }

    public void add(Task t) {
        if(t != null) {
            tasks.add(t);
        }
    }

    public int size() {
        return tasks.size();
    }
}
