package bot.listener;

import bot.api.ApiCaller;
import bot.api.ApiResponse;
import bot.domain.contest.StandingsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContestTimerHandler extends ListenerAdapter {
    private final Map<String, ScheduledExecutorService> activeTimers = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ContestTimerHandler.class);
    private static final String BASE_URL = "https://codeforces.com/api/";
    private static final Gson gson = new Gson();
    private final ApiCaller apiCaller;

    public ContestTimerHandler(ApiCaller apiCaller) {
        this.apiCaller = apiCaller;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.equals("confirm_contest")) {
            logger.info("Confirm contest button clicked");
            handleConfirmContest(event);
        } else if (componentId.equals("cancel_contest")) {
            logger.info("Cancel contest button clicked");
            handleCancelContest(event);
        }
    }

    private void handleConfirmContest(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        EmbedBuilder embed = new EmbedBuilder(message.getEmbeds().getFirst());

        // Get user time from the embed field
        String startTimeStr = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Virtual Start Time"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();

        // Parse the start time
        ZonedDateTime startTime = parseDateTime(startTimeStr);
        if (startTime == null) {
            event.reply("Error parsing the start time. Please check the format.").setEphemeral(true).queue();
            return;
        }

        // Disable buttons
        List<Button> updatedButtons = message.getButtons().stream()
                .map(Button::asDisabled)
                .toList();

        // Edit the message to disable the buttons
        message.editMessageComponents(
                Collections.singletonList(ActionRow.of(updatedButtons))
        ).queue();


        // Example: "2 hours 25 minutes"
        String duration = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Duration"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();

        // Parse the duration
        String[] durationParts = duration.split(" ");
        long hours = Long.parseLong(durationParts[0]);
        long minutes = Long.parseLong(durationParts[2]);
        Duration originalDuration = Duration.ofHours(hours).plusMinutes(minutes);

        ZonedDateTime now = ZonedDateTime.now(startTime.getZone());
        Duration timeUntilStart = Duration.between(now, startTime);
        Duration totalDuration = timeUntilStart.plus(originalDuration);

        long totalDurationSeconds = totalDuration.getSeconds();


        String participantsStr = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Participants"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();
        logger.info("Participants: {}", participantsStr);

        // Extract usernames using regex
        List<String> usernames = new ArrayList<>();
        Pattern pattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = pattern.matcher(participantsStr);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        logger.info("Usernames: {}", usernames);

        String contestId = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Contest ID"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();

        String contestTitle = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Contest Name"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();

        contestTitle = contestTitle.substring(1, contestTitle.indexOf("]"));

        // Schedule the timer
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        String finalContestTitle = contestTitle;
        scheduler.schedule(() -> displayResults(event.getChannel(), contestId, finalContestTitle, usernames), totalDurationSeconds, TimeUnit.SECONDS);

        // Store the scheduler for potential cancellation
        activeTimers.put(contestId, scheduler);

        // After confirmation, send the same embed visible to all
        event.getChannel().sendMessageEmbeds(embed.build()).queue(); // Send to all users

        event.reply("Contest confirmed! Results will be displayed after the contest ends.").setEphemeral(true).queue();
    }

    private ZonedDateTime parseDateTime(String dateTimeStr) {
        try {
            // Parse the date and time part
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a", Locale.ENGLISH);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr.substring(0, dateTimeStr.lastIndexOf(" ")), formatter);

            // Parse the timezone offset
            String offsetStr = dateTimeStr.substring(dateTimeStr.lastIndexOf(" ") + 1);
            ZoneOffset offset = ZoneOffset.of(offsetStr);

            // Combine into ZonedDateTime
            return ZonedDateTime.of(localDateTime, offset);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date time: {}", dateTimeStr, e);
            return null;
        }
    }


    private void handleCancelContest(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        EmbedBuilder embed = new EmbedBuilder(message.getEmbeds().getFirst());

        String contestId = embed.getFields().stream()
                .filter(field -> Objects.requireNonNull(field.getName()).contains("Contest ID"))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();


        // Cancel the timer
        ScheduledExecutorService scheduler = activeTimers.get(contestId);
        if (scheduler != null) {
            try (scheduler) {
                scheduler.shutdownNow();
                activeTimers.remove(contestId);
            }
        } else {
            logger.error("No active timer found for contest ID: {}", contestId);
        }

        event.reply("Contest cancelled!").setEphemeral(true).queue();

        // Delete the message
        message.delete().queue();
    }


    public void displayResults(MessageChannel channel, String contestId, String contestName, List<String> participants) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return contestTournamentResult(participants, contestId, contestName);
            } catch (IOException e) {
                logger.error("Error getting contest results", e);
                return null;
            }
        }).thenAccept(embedBuilder -> {
            if (embedBuilder != null) {
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        });
    }


    public EmbedBuilder contestTournamentResult(List<String> participants, String contestId, String contestName) throws IOException {
        // Build the URL for the API call
        StringBuilder url = new StringBuilder(BASE_URL + "contest.standings?contestId=" + contestId + "&asManager=false&handles=");
        for (String username : participants) {
            url.append(username).append(";");
        }
        url.deleteCharAt(url.length() - 1);
        url.append("&showUnofficial=true");

        // Make the API call
        String jsonResponse = apiCaller.makeApiCall(url.toString());

        // Parse the JSON response
        Type responseType = new TypeToken<ApiResponse<StandingsResponse>>() {
        }.getType();
        ApiResponse<StandingsResponse> apiResponse = gson.fromJson(jsonResponse, responseType);

        // Check if the response is OK
        if (!"OK".equals(apiResponse.getStatus()) || apiResponse.getResult() == null) {
            throw new IOException("Failed to retrieve contest standings");
        }

        // Extract the required information
        StandingsResponse standings = apiResponse.getResult();
        List<StandingsResponse.StandingsRow> rows = standings.getRows();

        // Sort rows by rank
        rows.sort(Comparator.comparingInt(StandingsResponse.StandingsRow::getRank));

        return buildResult(contestId, contestName, rows);
    }

    @NotNull
    private static EmbedBuilder buildResult(String contestId, String contestName, List<StandingsResponse.StandingsRow> rows) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Contest Results: " + contestName, "https://codeforces.com/contest/" + contestId);
        embedBuilder.setDescription("Contest ID: " + contestId);
        embedBuilder.setColor(Color.BLUE);

        int index = 0;
        for (StandingsResponse.StandingsRow row : rows) {
            String username = row.getParty().getMembers().getFirst().getHandle();
            String usernameLink = "[`" + username + "`](https://codeforces.com/profile/" + username + ")";
            int rank = row.getRank();
            int problemsSolved = 0;
            int wrongAnswers = 0;

            for (StandingsResponse.ProblemResult problemResult : row.getProblemResults()) {
                if (problemResult.getPoints() > 0) {
                    problemsSolved++;
                }
                wrongAnswers += problemResult.getRejectedAttemptCount();
            }

            String funnyWords = "";
            index++;
            if (index == 1) {
                funnyWords = ":trophy: **Champion!**";
            } else if (index <= 3) {
                funnyWords = ":medal: **Top 3, impressive!**";
            } else {
                funnyWords = ":muscle: **Keep trying!**";
            }
            embedBuilder.addField("Username", usernameLink + "\t" + funnyWords, true);
            embedBuilder.addField("Rank", String.valueOf(rank), false);
            embedBuilder.addField("Problems Solved", String.valueOf(problemsSolved), false);
            embedBuilder.addField("Wrong Answers", String.valueOf(wrongAnswers), false);
        }
        return embedBuilder;
    }
}