package bot.util;

import bot.domain.contest.Problem;
import bot.domain.contest.StandingsResponse;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    public static EmbedBuilder buildStandingEmbed(StandingsResponse standingsResponse, String handle, int contestId) {
        StandingsResponse.StandingsRow userStanding = standingsResponse.getRows().getFirst();

        String contestName = standingsResponse.getContest().getName();
        String contestStartTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new java.util.Date(standingsResponse.getContest().getStartTimeSeconds() * 1000));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("`" + handle + "`" + "'s Standing in Contest");
        embed.setColor(Color.cyan);

        String contestUrl = "https://codeforces.com/contest/" + contestId;
        String contestFieldTitle = "Contest";
        String contestFieldValue = String.format("[%s](%s) (ID: %s)", contestName, contestUrl, contestId);
        embed.addField(contestFieldTitle, contestFieldValue, false);

        embed.addField("Start Time", contestStartTime, false);
        embed.addField("Rank", String.valueOf(userStanding.getRank()), false);
        embed.addField("Solved", userStanding.getProblemResults().stream().filter(pr -> pr.getPoints() > 0).count() + "/" + userStanding.getProblemResults().size(), false);

        List<Problem> problems = standingsResponse.getProblems();
        int problemCount = 0;

        for (StandingsResponse.ProblemResult problemResult : userStanding.getProblemResults()) {
            Problem problem = problems.get(problemCount++);

            String problemName = problem.getName();
            String problemLink = "https://codeforces.com/contest/" + contestId + "/problem/" + problem.getIndex();
            String problemRating = problem.getRating() > 0 ? "Rating: " + problem.getRating() : "Unrated";
            String problemStatus = problemResult.getPoints() > 0 ? "Solved" : "Unsolved";
            String problemDetails = problemStatus + " (" + problemResult.getRejectedAttemptCount() + " wrong attempts)";
            if (problemResult.getPoints() > 0) {
                problemDetails += "\nSolved after: " + problemResult.getBestSubmissionTimeSeconds() / 60 + " minutes";
            }

            String fieldValue = String.format("[%s](%s) (%s)\n%s\n%s", problemName, problemLink, problem.getIndex(), problemRating, problemDetails);
            embed.addField("Problem", fieldValue, false);
        }

        return embed;
    }
}
