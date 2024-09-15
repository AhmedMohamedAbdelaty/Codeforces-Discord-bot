package bot.util;

import bot.domain.contest.Contest;
import bot.domain.contest.Problem;
import bot.domain.user.Rating;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EmbedBuilderUtil {

    @NotNull
    public static EmbedBuilder createDefaultEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE); // Set a default color for all embeds
        // Add more default configurations here
        return embed;
    }

    @NotNull
    public static EmbedBuilder createErrorEmbed(String errorMessage) {
        EmbedBuilder embed = createDefaultEmbed();
        embed.setColor(Color.RED); // Error messages can be red
        embed.setDescription(errorMessage);
        return embed;
    }

    @Nullable
    public static String generateGraphImage(@NotNull List<Rating> ratingHistory, String handle) {
        TimeSeries series = new TimeSeries("Rating");

        for (Rating change : ratingHistory) {
            Date date = new Date(change.getRatingUpdateTimeSeconds() * 1000);
            series.addOrUpdate(new Day(date), change.getNewRating());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                handle + "'s Rating History", // Title
                "Time", // X-Axis label
                "Rating", // Y-Axis label
                dataset, // Dataset
                true, // Legend
                true, // Tooltips
                false // URLs
        );

        chart.setBackgroundPaint(Color.white);

        String filePath = "rating_history_" + handle + ".png";
        File chartFile = new File(filePath);
        try {
            ChartUtils.saveChartAsPNG(chartFile, chart, 1200, 800);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return chartFile.getAbsolutePath();
    }

    @NotNull
    public static EmbedBuilder buildRatingHistoryEmbed(@NotNull List<Rating> ratingHistory, String handle) {
        EmbedBuilder embed = new EmbedBuilder();
        String profileUrl = "https://codeforces.com/profile/" + handle;
        embed.setTitle("`" + handle + "`" + "'s Rating History");
        embed.setColor(Color.MAGENTA);
        embed.setDescription("Here is the rating history graph for [" + "`" + handle + "`](" + profileUrl + ")");

        // Calculate current and max rating
        int currentRating = ratingHistory.getLast().getNewRating();
        int maxRating = ratingHistory.stream().max(Comparator.comparingInt(Rating::getNewRating)).get().getNewRating();

        // Add fields for current and max rating
        embed.addField("Current Rating", String.valueOf(currentRating), true);
        embed.addField("Max Rating", String.valueOf(maxRating), true);

        String filePath = generateGraphImage(ratingHistory, handle);
        if (filePath != null) {
            embed.setImage("attachment://" + new File(filePath).getName());
        }

        return embed;
    }

    @NotNull
    public static EmbedBuilder buildRandomProblemEmbed(@NotNull Problem randomProblem) {
        EmbedBuilder embed = new EmbedBuilder();

        String problemNameWithIndex = randomProblem.getIndex() + ". " + randomProblem.getName();
        String problemLink = "https://codeforces.com/contest/" + randomProblem.getContestId() + "/problem/" + randomProblem.getIndex();
        int problemRating = randomProblem.getRating();
        List<String> tags = randomProblem.getTags();

        embed.setTitle(problemNameWithIndex, problemLink);
        embed.setColor(Color.GREEN);
        embed.addField("Rating", String.valueOf(problemRating), true);
        embed.addField("Tags", String.join(", ", tags), false);

        return embed;
    }

    @NotNull
    public static EmbedBuilder buildRandomContestEmbed(@NotNull Contest selectedContest, List<String> usernames, ZonedDateTime userTime) {
        EmbedBuilder embed = new EmbedBuilder();
        String contestUrl = "https://codeforces.com/contest/" + selectedContest.getId();
        embed.setTitle("🎉 Contest: " + selectedContest.getName(), contestUrl);
        embed.setColor(Color.GREEN);
        embed.setDescription("Join this contest as a fun tournament!");

        // Add contest details
        embed.addField("Type", selectedContest.getType(), true);

        // Custom start time (user input)
        DateTimeFormatter friendlyFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a z");
        String formattedCustomStartTime = userTime.format(friendlyFormatter);
        embed.addField("Virtual Start Time", formattedCustomStartTime, true);

        // Format duration
        long hours = selectedContest.getDurationSeconds() / 3600;
        long minutes = (selectedContest.getDurationSeconds() % 3600) / 60;
        String duration = String.format("%d hours %d minutes", hours, minutes);
        embed.addField("Duration", duration, false);


        // Add participating users
        List<String> participantsList = usernames.stream()
                .map(username -> "[`" + username + "`](https://codeforces.com/profile/" + username + ")")
                .collect(Collectors.toList());
        String participants = String.join(", ", participantsList);
        embed.addField("Participants", participants, false);

        embed.setFooter("Let's see who will be rank one!");

        return embed;
    }
}
