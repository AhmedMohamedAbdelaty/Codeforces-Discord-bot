package bot.commands.contestCommands;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class UpcomingContestsCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public UpcomingContestsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();


        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getUpcomingContests();
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve upcoming contests: " + e.getMessage(), e);
            }
        }).thenAccept(embedBuilder ->
                hook.sendMessageEmbeds(embedBuilder.build()).queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}
