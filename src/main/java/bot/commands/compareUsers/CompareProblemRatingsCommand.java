package bot.commands.compareUsers;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

public class CompareProblemRatingsCommand implements Command {

    private final CodeforcesAPIImpl codeforcesAPI;

    public CompareProblemRatingsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String username1 = Objects.requireNonNull(event.getOption("username1")).getAsString();
        String username2 = Objects.requireNonNull(event.getOption("username2")).getAsString();

        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.compareProblemRatings(username1, username2);
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve problem ratings: " + e.getMessage(), e);
            }
        }).thenAccept(file -> {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle(username1 + " vs " + username2 + " Problem Ratings");
                    if (file.exists()) {
                        hook.sendFiles(FileUpload.fromData(file)).addEmbeds(embed.build()).queue();
                    } else {
                        hook.sendMessage("Failed to generate problem ratings graph.").queue();
                    }
                }
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}
