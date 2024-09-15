package bot.listener;

import bot.CodeforcesBot;
import bot.commands.Command;
import bot.commands.CommandFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DiscordEventListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordEventListener.class);
    private final CodeforcesBot bot;

    public DiscordEventListener(CodeforcesBot codeforcesBot) {
        this.bot = codeforcesBot;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
//        resetGuildCommands(Objects.requireNonNull(event.getJDA().getShardManager()), "423085629807788032");
//        registerGuildsCommands(bot.getShardManager());
        registerCommands(bot.getShardManager());
    }

    private void registerGuildsCommands(@NotNull ShardManager shardManager) {
        shardManager.getGuilds().forEach(guild -> {
            CommandListUpdateAction guildCommands = guild.updateCommands();
            CommandFactory.registerCommands(guildCommands);
            guildCommands.queue(
                    success -> logger.info("Guild-specific commands registered successfully for guild {}", guild.getId()),
                    error -> logger.error("Error registering guild-specific commands for guild {}", guild.getId(), error)
            );
        });
    }

    private void registerCommands(@NotNull ShardManager shardManager) {
//        // Register guild-specific commands
//        Guild g = shardManager.getGuildById("423085629807788032");
//        if (g != null) {
//            CommandListUpdateAction guildCommands = g.updateCommands();
//            CommandFactory.registerCommands(guildCommands);
//            guildCommands.queue(
//                    success -> logger.info("Guild-specific commands registered successfully for guild {}", g.getId()),
//                    error -> logger.error("Error registering guild-specific commands for guild {}", g.getId(), error)
//            );
//        }

        // Register global commands
        shardManager.getShards().forEach(jda -> {
            CommandListUpdateAction globalCommands = jda.updateCommands();
            CommandFactory.registerCommands(globalCommands);
            globalCommands.queue(
                    success -> logger.info("Global commands registered successfully for shard {}", jda.getShardInfo().getShardId()),
                    error -> logger.error("Error registering global commands for shard {}", jda.getShardInfo().getShardId(), error)
            );
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command = CommandFactory.getCommand(event.getName());
        if (command != null) {
            // Log the user who used the command
//            logger.info("Command used by: " + event.getUser().getName() + " (ID: " + event.getUser().getId() + ")");

            command.execute(event);
        } else {
            event.reply("Unknown command").queue();
        }
    }

    public void resetGuildCommands(@NotNull ShardManager shardManager, String guildId) {
        Objects.requireNonNull(shardManager.getGuildById(guildId)).updateCommands().queue();
    }

//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        logger.info("Server name: " + event.getGuild().getName()
//                    + " Server ID: " + event.getGuild().getId()
//                    + " Channel: " + event.getChannel()
//                    + " User: " + event.getAuthor()
//                    + " Message: " + event.getMessage().getContentDisplay()
//                    + " Message ID: " + event.getMessageId()
//                    + event.getMember());
//
//        /*
//        Output example:
//         Server name: am0103738 Server ID: 423085629807788032 Channel: TextChannel:general(id=423085629807788034) User: SelfUser:test-Codeforces bot(id=1284923117948370977) Message:  Message ID: 1284943171234828412Member:test-Codeforces bot(id=1284923117948370977, user=SelfUser:test-Codeforces bot(id=1284923117948370977), guild=Guild:am0103738(id=423085629807788032))
//         */
//    }
}