package bot.commands.contestCommands;

import bot.api.CodeforcesApiCaller;
import bot.cache.RedisCache;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class FinishedContestsCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public FinishedContestsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        JedisPool jedisPool = RedisCache.getPool();

        CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String cachedContests = jedis.get("contest_0");

                if (cachedContests != null) {
                    // convert cached contest string to EmbedBuilder
                }

                return codeforcesAPI.getFinishedContests();
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve finished contests: " + e.getMessage(), e);
            }
        }).thenAccept(embedBuilder ->
                hook.sendMessageEmbeds(embedBuilder.build()).queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}
