package bot.infrastructure;

import bot.api.ApiCaller;
import bot.domain.user.Rating;
import bot.util.EmbedBuilderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CodeforcesAPIImplTest {

    @Mock
    private ApiCaller apiCaller;

    private CodeforcesAPIImpl codeforcesAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeforcesAPI = new CodeforcesAPIImpl(apiCaller);
    }

    @Test
    void getUserInfo() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":[{\"handle\":\"tourist\",\"rating\":3779,\"maxRating\":3779,\"rank\":\"legendary grandmaster\",\"maxRank\":\"legendary grandmaster\"}]}";
        int rating = 3779;
        int maxRating = 3779;
        String rank = "legendary grandmaster";
        String maxRank = "legendary grandmaster";
        int contribution = 0;

        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder embed = codeforcesAPI.getUserInfo("tourist");

        assertNotNull(embed);
        assertEquals("`tourist`'s Codeforces Info", embed.build().getTitle());
        assertEquals(String.valueOf(rating), Objects.requireNonNull(embed.build().getFields().getFirst()).getValue());
        assertEquals(String.valueOf(maxRating), Objects.requireNonNull(embed.build().getFields().get(1)).getValue());
        assertEquals(rank, Objects.requireNonNull(embed.build().getFields().get(2)).getValue());
        assertEquals(maxRank, Objects.requireNonNull(embed.build().getFields().get(3)).getValue());
        assertEquals(String.valueOf(contribution), Objects.requireNonNull(embed.build().getFields().get(4)).getValue());
    }

    @Test
    void getUpcomingContests() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":[{\"id\":1234,\"name\":\"Codeforces Round #123\",\"type\":\"CF\",\"phase\":\"BEFORE\",\"frozen\":false,\"durationSeconds\":7200,\"startTimeSeconds\":1609459200,\"relativeTimeSeconds\":-86400}]}";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder embed = codeforcesAPI.getUpcomingContests();

        assertNotNull(embed);
        assertEquals("Upcoming Contests", embed.build().getTitle());
        assertEquals(Color.ORANGE, embed.build().getColor());
        assertFalse(embed.build().getFields().isEmpty());
        assertEquals("Codeforces Round #123 (ID: 1234)", embed.build().getFields().getFirst().getName());

        System.out.println(embed.build().getFields().getFirst().getValue());
        String value = Objects.requireNonNull(embed.build().getFields().getFirst().getValue());
        List<String> lines = Arrays.asList(value.split("\n"));
        assertEquals("Start Time: 01/01/2021 02:00:00", lines.getFirst());
        assertEquals("Duration: 2 hours", lines.get(1));
        assertEquals("Before start 1 days 0 hours 0 minutes", lines.get(2));
    }

    @Test
    void getFinishedContests() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":[{\"id\":1234,\"name\":\"Codeforces Round #123\",\"type\":\"CF\",\"phase\":\"FINISHED\",\"frozen\":false,\"durationSeconds\":7200,\"startTimeSeconds\":1609459200,\"relativeTimeSeconds\":86400}]}";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder embed = codeforcesAPI.getFinishedContests();

        assertNotNull(embed);
        assertEquals("Finished Contests", embed.build().getTitle());
        assertEquals(Color.GREEN, embed.build().getColor());
        assertFalse(embed.build().getFields().isEmpty());
        assertEquals("Codeforces Round #123 (ID: 1234)", embed.build().getFields().getFirst().getName());

        String value = Objects.requireNonNull(embed.build().getFields().getFirst().getValue());
        List<String> lines = Arrays.asList(value.split("\n"));
        assertEquals("Start Time: 01/01/2021 02:00:00", lines.getFirst());
        assertEquals("Duration: 2 hours", lines.get(1));
        assertEquals("Finished 1 days 0 hours 0 minutes ago", lines.get(2));
    }

    @Test
    void getStandingContestForUser() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":{\"contest\":{\"id\":1234,\"name\":\"Codeforces Round #123\",\"type\":\"CF\",\"phase\":\"FINISHED\",\"frozen\":false,\"durationSeconds\":7200,\"startTimeSeconds\":1609459200,\"relativeTimeSeconds\":86400},\"problems\":[{\"contestId\":1234,\"index\":\"A\",\"name\":\"Problem A\",\"type\":\"PROGRAMMING\",\"points\":500,\"rating\":800}],\"rows\":[{\"party\":{\"contestId\":1234,\"members\":[{\"handle\":\"tourist\"}]},\"rank\":1,\"points\":3000,\"penalty\":0,\"successfulHackCount\":0,\"unsuccessfulHackCount\":0,\"problemResults\":[{\"points\":500,\"rejectedAttemptCount\":0,\"type\":\"FINAL\",\"bestSubmissionTimeSeconds\":3600}]}]}}";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder embed = codeforcesAPI.getStandingContestForUser("tourist", 1234);

        assertNotNull(embed);
        assertTrue(Objects.requireNonNull(embed.build().getTitle()).contains("tourist"));
        assertEquals("`tourist`'s Standing in Contest", embed.build().getTitle());
        assertEquals(Color.cyan, embed.build().getColor());
        assertFalse(embed.build().getFields().isEmpty());
        assertEquals("Contest", embed.build().getFields().getFirst().getName());
        assertTrue(Objects.requireNonNull(embed.build().getFields().getFirst().getValue()).contains("Codeforces Round #123"));
        assertEquals("Start Time", embed.build().getFields().get(1).getName());
        assertEquals("01/01/2021 02:00:00", embed.build().getFields().get(1).getValue());
        assertEquals("Rank", embed.build().getFields().get(2).getName());
        assertEquals("1", embed.build().getFields().get(2).getValue());
        assertEquals("Solved", embed.build().getFields().get(3).getName());
        assertEquals("1/1", embed.build().getFields().get(3).getValue());
        assertEquals("Problem", embed.build().getFields().get(4).getName());
        assertTrue(Objects.requireNonNull(embed.build().getFields().get(4).getValue()).contains("Problem A"));
    }

    @Test
    void getRatingHistory() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":[{\"contestId\":1234,\"contestName\":\"Codeforces Round #123\",\"handle\":\"tourist\",\"rank\":1,\"ratingUpdateTimeSeconds\":1609459200,\"oldRating\":3779,\"newRating\":3780}]}";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        List<Rating> ratingHistory = codeforcesAPI.getRatingHistory("tourist");

        assertNotNull(ratingHistory);
        assertFalse(ratingHistory.isEmpty());
        assertEquals(1234, ratingHistory.getFirst().getContestId());
        assertEquals("tourist", ratingHistory.getFirst().getHandle());
        assertEquals(1, ratingHistory.getFirst().getRank());
        assertEquals(1609459200, ratingHistory.getFirst().getRatingUpdateTimeSeconds());
        assertEquals(3779, ratingHistory.getFirst().getOldRating());
        assertEquals(3780, ratingHistory.getFirst().getNewRating());

        // Test for image generation
        EmbedBuilder embed = EmbedBuilderUtil.buildRatingHistoryEmbed(ratingHistory, "tourist");
        assertNotNull(embed);
        assertTrue(Objects.requireNonNull(embed.build().getDescription()).contains("rating history graph"));
        assertTrue(embed.build().getFields().stream().anyMatch(field -> Objects.equals(field.getName(), "Current Rating")));
        assertTrue(embed.build().getFields().stream().anyMatch(field -> Objects.equals(field.getName(), "Max Rating")));
        String imageUrl = Objects.requireNonNull(embed.build().getImage()).getUrl();
        assertNotNull(imageUrl);
        assertTrue(imageUrl.contains("attachment://rating_history_tourist.png"));

        // Test for image deletion
        String filePath = imageUrl.replace("attachment://", "");
        File file = new File(filePath);
        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());
    }

    @Test
    void getRandomProblem() throws IOException {
        String mockResponse = "{\"status\":\"OK\",\"result\":{\"problems\":[{\"contestId\":1234,\"index\":\"A\",\"name\":\"Problem A\",\"type\":\"PROGRAMMING\",\"points\":500,\"rating\":800,\"tags\":[\"implementation\"]}]}}";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder embed = codeforcesAPI.getRandomProblem(Collections.singletonList("implementation"), 800, 1000);

        assertNotNull(embed);
        assertEquals("A. Problem A", embed.build().getTitle());
        assertEquals("https://codeforces.com/contest/1234/problem/A", embed.build().getUrl());
        assertEquals(Color.GREEN, embed.build().getColor());
        assertFalse(embed.build().getFields().isEmpty());
        assertEquals("Rating", embed.build().getFields().getFirst().getName());
        assertEquals("800", embed.build().getFields().getFirst().getValue());
        assertEquals("Tags", embed.build().getFields().get(1).getName());
        assertEquals("implementation", embed.build().getFields().get(1).getValue());
    }
}