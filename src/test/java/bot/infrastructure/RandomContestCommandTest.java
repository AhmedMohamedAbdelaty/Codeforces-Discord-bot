package bot.infrastructure;

import bot.cache.RedisCache;
import bot.api.ApiCaller;
import bot.api.ApiResponse;
import bot.domain.contest.Contest;
import bot.domain.contest.Party;
import bot.domain.contest.Problem;
import bot.domain.user.Submission;
import bot.domain.user.Verdict;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RandomContestCommandTest {

    @Mock
    private ApiCaller apiCaller;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    private CodeforcesAPIImpl codeforcesAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeforcesAPI = new CodeforcesAPIImpl(apiCaller);

        // Mock RedisCache
        RedisCache.setPool(jedisPool);
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    void testGetRandomContestValidInput() throws IOException {
        // Mock the contest list response
        List<Contest> contests = Arrays.asList(
                new Contest(2018, "Codeforces Round 975 (Div. 1)", "CF", "FINISHED", false, 9000, 1727444100, 1293035),
                new Contest(2019, "Codeforces Round 975 (Div. 2)", "CF", "FINISHED", false, 9000, 1727444100, 1293035)
        );
        ApiResponse<List<Contest>> contestApiResponse = new ApiResponse<>("OK", contests);
        String contestJsonResponse = new Gson().toJson(contestApiResponse);
        when(apiCaller.makeApiCall(anyString())).thenReturn(contestJsonResponse);

        // Mock the user status response
        Problem problem1 = new Problem();
        Party author = new Party(2018, List.of(new Party.Member("_AhmedMohamed_")), "CONTESTANT", false, 1692887700);
        Submission.Testset testset = Submission.Testset.TESTS;

        List<Submission> submissions = Arrays.asList(
                new Submission(220287033, 342, 1692895752, 8052, problem1, author, "C++20 (GCC 11-64)", Verdict.OK, testset, 17, 15, 0, null),
                new Submission(220263495, 54, 1692893677, 5977, problem1, author, "C++20 (GCC 11-64)", Verdict.OK, testset, 19, 77, 20889600, null)
        );
        ApiResponse<List<Submission>> submissionApiResponse = new ApiResponse<>("OK", submissions);
        String submissionJsonResponse = new Gson().toJson(submissionApiResponse);
        when(apiCaller.makeApiCall(contains("user.status"))).thenReturn(submissionJsonResponse);

        // Verify the response
        ZonedDateTime fixedTime = ZonedDateTime.parse("2024-10-12T16:01:00+03:00[EET]"); // Fixed time for testing
        try (MockedStatic<ZonedDateTime> mockedTime = Mockito.mockStatic(ZonedDateTime.class)) {
            mockedTime.when(ZonedDateTime::now).thenReturn(fixedTime);

            // Call the method
            List<String> usernames = Arrays.asList("user1", "user2");
            ZonedDateTime startTime = ZonedDateTime.now();
            ZonedDateTime userTime = ZonedDateTime.now();
            EmbedBuilder contest = codeforcesAPI.getRandomContest(event, usernames, "div2", startTime, userTime);

            // Verify the response
            assertNotNull(contest);
            assertEquals("[Codeforces Round 975 (Div. 2)](https://codeforces.com/contest/2019)", contest.getFields().get(0).getValue());
            assertEquals("2019", contest.getFields().get(1).getValue());
            assertEquals("CF", contest.getFields().get(2).getValue());
            assertEquals("October 12, 2024, 4:01 PM EEST", contest.getFields().get(3).getValue());
            assertEquals("2 hours 30 minutes", contest.getFields().get(4).getValue());
        }
    }

}