package bot.util;

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
}
