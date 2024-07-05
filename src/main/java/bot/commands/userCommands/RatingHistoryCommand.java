package bot.commands.userCommands;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import bot.util.EmbedBuilderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RatingHistoryCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public RatingHistoryCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        String username = Objects.requireNonNull(event.getOption("username")).getAsString();

        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getRatingHistory(username);
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve rating history: " + e.getMessage(), e);
            }
        }).thenAccept(ratingHistory -> {
            // Check if the rating history is empty before creating the embed
            if (ratingHistory.isEmpty()) {
                hook.sendMessage("No rating history found for `" + username + "`").queue();
                return;
            }

            EmbedBuilder embed = EmbedBuilderUtil.buildRatingHistoryEmbed(ratingHistory, username);
            String filePath = EmbedBuilderUtil.generateGraphImage(ratingHistory, username);
            File file = new File(filePath);
            if (file.exists()) {
                hook.sendFiles(FileUpload.fromData(file, "rating_history.png"))
                        .addEmbeds(embed.build())
                        .queue();

                // Delete the file after sending it
                file.delete();
            } else {
                hook.sendMessage("Failed to generate rating history graph.").queue();
            }
        }).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}
