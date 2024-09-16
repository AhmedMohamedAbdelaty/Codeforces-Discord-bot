package bot.commands.userCommands;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

public class ProblemRatingsCommand implements Command {

    private final CodeforcesAPIImpl codeforcesAPI;

    public ProblemRatingsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String username = Objects.requireNonNull(event.getOption("username")).getAsString();

        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getProblemRatings(username);
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve problem ratings: " + e.getMessage(), e);
            }
        }).thenAccept(file -> {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle(username + " Problem Ratings");
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
