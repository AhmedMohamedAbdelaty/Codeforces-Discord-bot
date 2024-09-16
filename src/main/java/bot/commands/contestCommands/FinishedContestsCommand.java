package bot.commands.contestCommands;

import bot.api.CodeforcesApiCaller;
import bot.cache.RedisCache;
import bot.cache.RedisUtil;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.EmbedBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FinishedContestsCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;
    private final RedisUtil redisUtil;
    private final Gson gson;

    public FinishedContestsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
        this.redisUtil = new RedisUtil();
        this.gson = new Gson();
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
                // Check if contests are cached
                String cachedContests = jedis.get("finished_contests");
                if (cachedContests != null) {
                    // Deserialize the cached string to List<Contest> and build EmbedBuilder
                    Type contestType = new TypeToken<List<bot.domain.contest.Contest>>() {}.getType();
                    List<bot.domain.contest.Contest> contests = gson.fromJson(cachedContests, contestType);
                    return codeforcesAPI.buildContestsEmbed(contests, "Finished Contests", java.awt.Color.GREEN, "FINISHED");
                }

                // If not cached, fetch contests from the API
                EmbedBuilder embedBuilder = codeforcesAPI.getFinishedContests();

                // Serialize and store the fetched contests in Redis
                String serializedContests = gson.toJson(codeforcesAPI.getContests("https://codeforces.com/api/contest.list?gym=false"));
                jedis.setex("finished_contests", 604800, serializedContests); // Cache for 1 week

                return embedBuilder;
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
