package net.runelite.client.plugins.ggbotv4.bot.task.error;

import lombok.Data;
import net.runelite.client.plugins.ggbotv4.bot.task.TaskError;

import java.util.function.Predicate;

@Data
public class ErrorTimeout implements TaskError {
 private final Predicate<?> predicate;

 @Override
 public String getDescription() {
  return "Task timed out during predicate " + predicate.getClass().getSimpleName() + ".";
 }
}
