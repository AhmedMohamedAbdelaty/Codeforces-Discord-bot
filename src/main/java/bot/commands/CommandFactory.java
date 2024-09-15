package bot.commands;

import bot.commands.contestCommands.FinishedContestsCommand;
import bot.commands.contestCommands.RandomContestCommand;
import bot.commands.contestCommands.StandingCommand;
import bot.commands.contestCommands.UpcomingContestsCommand;
import bot.commands.problemCommands.RandomProblemCommand;
import bot.commands.userCommands.RatingHistoryCommand;
import bot.commands.userCommands.UserInfoCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class CommandFactory {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        // get user info
        register("userinfo", new UserInfoCommand());

        // get upcoming contests
        register("upcoming-contests", new UpcomingContestsCommand());

        // get finished contests
        register("finished-contests", new FinishedContestsCommand());

        // return standing of a contest for a user
        register("standing", new StandingCommand());

        // rating history, graph.
        register("rating-history", new RatingHistoryCommand());

        // random problem
        register("random-problem", new RandomProblemCommand());

        // return random contest, none of the given usernames have participated in
        register("random-contest", new RandomContestCommand());
    }

    public static void register(String name, Command command) {
        commands.put(name, command);
    }

    public static Command getCommand(String name) {
        return commands.get(name);
    }

    public static void registerCommands(@NotNull CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("userinfo", "Get user information")
                        .addOption(STRING, "username", "Codeforces username", true),

                Commands.slash("upcoming-contests", "Get upcoming contests"),

                Commands.slash("finished-contests", "Get finished contests"),

                Commands.slash("standing", "Get standing of a contest for a user")
                        .addOption(STRING, "username", "Codeforces username", true)
                        .addOption(STRING, "contest_id", "Contest ID", true),

                Commands.slash("rating-history", "Get rating history of a user")
                        .addOption(STRING, "username", "Codeforces username", true),

                Commands.slash("random-problem", "Get a random problem")
                        .addOption(STRING, "rating_start", "Rating start", true)
                        .addOption(STRING, "rating_end", "Rating end", true)
                        .addOption(STRING, "tags", "Problem tags(separated by comma)", false),

                Commands.slash("random-contest", "Get a random contest")
                        .addOption(STRING, "usernames", "Usernames(separated by comma)", true)
                        .addOption(STRING, "contest_type", "Contest type (\"div1\", \"div2\", \"div3\", \"div4\")", true)
                        .addOption(STRING, "start_time", "Start time (24 hour format) (e.g., 2023-10-01 15:00:00 +03:00)", true)
        ).queue();
    }
}