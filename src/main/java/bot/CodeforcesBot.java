package bot;

import javax.security.auth.login.LoginException;

import bot.listener.DiscordEventListener;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeforcesBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeforcesBot.class);
    protected static CodeforcesBot selfBot;
    private ShardManager shardManager;

    public CodeforcesBot(String token) {
        try {
            shardManager = buildShardManager(token);
        } catch (LoginException e) {
            LOGGER.error("Failed to start bot! Please check the console for any errors.", e);
            System.exit(0);
        }
    }

    @NotNull
    private ShardManager buildShardManager(String token) throws LoginException {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token)
                .addEventListeners(new DiscordEventListener(this));

        return builder.build();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
