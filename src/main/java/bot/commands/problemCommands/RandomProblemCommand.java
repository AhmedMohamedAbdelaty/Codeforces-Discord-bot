package bot.commands.problemCommands;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RandomProblemCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public RandomProblemCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        // tags, seperated by comma (optional)
        // rating interval
        // start rating (required)
        // end rating (required)

        String tags = event.getOption("tags") == null ? "" : Objects.requireNonNull(event.getOption("tags")).getAsString();
        String ratingStart = Objects.requireNonNull(event.getOption("rating_start")).getAsString();
        String ratingEnd = Objects.requireNonNull(event.getOption("rating_end")).getAsString();

        // convert tags to list, it might be like this "dp,greedy" or "db, greedy" or "dp , greedy", be careful about spaces, convert tags to lowercase
        List<String> tagsList = new ArrayList<>(List.of(tags.split(",")));
        tagsList.replaceAll(s -> s.trim().toLowerCase());

        // Convert rating to int
        int ratingStartInt = Integer.parseInt(ratingStart);
        int ratingEndInt = Integer.parseInt(ratingEnd);

        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getRandomProblem(tagsList, ratingStartInt, ratingEndInt);
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve random problem: " + e.getMessage());
            }
        }).thenAccept(embedBuilder ->
                hook.sendMessageEmbeds(embedBuilder.build()).queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}
