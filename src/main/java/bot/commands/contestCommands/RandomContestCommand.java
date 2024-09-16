package bot.commands.contestCommands;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RandomContestCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public RandomContestCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {

        // Log the user who used the command
        Logger logger = LoggerFactory.getLogger(RandomContestCommand.class);
        logger.info("Command used by: {}\nMessage: {}", event.getUser().getName(), event.getOptions());

        // Defer the reply
        event.deferReply(true).queue();

        String usernames = Objects.requireNonNull(event.getOption("usernames")).getAsString();
        String contestType = Objects.requireNonNull(event.getOption("contest_type")).getAsString();
        String startTime = Objects.requireNonNull(event.getOption("start_time")).getAsString();

        // Convert usernames to list
        List<String> usernamesList = new ArrayList<>(List.of(usernames.split(",")));
        usernamesList.replaceAll(String::trim);

        InteractionHook hook = event.getHook();

        ZonedDateTime startTimeZoned;
        ZonedDateTime userTime;
        try {
            startTimeZoned = (ZonedDateTime) convertTime(startTime)[1];
            userTime = (ZonedDateTime) convertTime(startTime)[0];
        } catch (IllegalArgumentException e) {
            hook.sendMessage("Error: " + e.getMessage()).queue();
            return;
        }

        Button confirmButton = Button.success("confirm_contest", "Confirm");
        Button cancelButton = Button.danger("cancel_contest", "Cancel");

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getRandomContest(event, usernamesList, contestType, startTimeZoned, userTime);

            } catch (IOException e) {
                hook.sendMessage("Error: " + e.getMessage()).queue();
            }
            return null;
        }).thenAccept(embedBuilder ->
                        hook.sendMessageEmbeds(embedBuilder.build())
                                .setActionRow(confirmButton, cancelButton)
                                .setEphemeral(true)
                                .queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }


    @NotNull
    @Contract(pure = true)
    private Object[] convertTime(String contestStartTime) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")  // Date and time pattern
                .optionalStart()
                .appendLiteral(' ')                    // Space between date-time and timezone
                .optionalEnd()
                .optionalStart()                       // Start optional block for 'GMT', 'UTC', or other timezones
                .appendZoneText(TextStyle.SHORT)       // Short timezone name (e.g., 'GMT', 'UTC')
                .optionalEnd()
                .optionalStart()
                .appendPattern("XXX")                  // Handle offsets like +03:00, -05:00, etc.
                .optionalEnd()
                .optionalStart()
                .appendPattern("Z")                    // Handle simple offsets like +0300, -0500, etc.
                .optionalEnd()
                .toFormatter();

        Logger logger = LoggerFactory.getLogger(RandomContestCommand.class);

        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(contestStartTime, formatter);
            ZonedDateTime localTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()); // Convert to system default timezone
            logger.info("Input time: {}\nConverted time: {}", contestStartTime, localTime);
            return new Object[]{zonedDateTime, localTime};
        } catch (DateTimeParseException e) {
            logger.error("Failed to parse date-time: {}", contestStartTime, e);
            throw new IllegalArgumentException("Invalid date-time format. Please use the format: yyyy-MM-dd HH:mm:ss z or an appropriate offset.");
        }
    }
}
