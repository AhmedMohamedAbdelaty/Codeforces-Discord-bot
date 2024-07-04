package bot.commands;

import bot.commands.contestCommands.FinishedContestsCommand;
import bot.commands.contestCommands.StandingCommand;
import bot.commands.contestCommands.UpcomingContestsCommand;
import bot.commands.userCommands.UserInfoCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.HashMap;
import java.util.Map;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class CommandFactory {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        // get user info
        register("userinfo", new UserInfoCommand());
        // get upcoming contests
        register("upcomingcontests", new UpcomingContestsCommand());
        // get finished contests
        register("finishedcontests", new FinishedContestsCommand());
        // return standing of a contest for a user
        register("standing", new StandingCommand());
    }

    public static void register(String name, Command command) {
        commands.put(name, command);
    }

    public static Command getCommand(String name) {
        return commands.get(name);
    }

    public static void registerCommands(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("userinfo", "Get user information")
                        .addOption(STRING, "username", "Codeforces username", true),
                Commands.slash("upcomingcontests", "Get upcoming contests"),
                Commands.slash("finishedcontests", "Get finished contests"),
                Commands.slash("standing", "Get standing of a contest for a user")
                        .addOption(STRING, "username", "Codeforces username", true)
                        .addOption(STRING, "contest_id", "Contest ID", true)
        ).queue();
    }
}