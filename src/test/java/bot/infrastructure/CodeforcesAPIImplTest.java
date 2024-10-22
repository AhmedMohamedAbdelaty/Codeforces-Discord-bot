package bot.infrastructure;

import bot.domain.user.Rating;
import bot.domain.user.Submission;
import com.google.gson.JsonSyntaxException;
import net.dv8tion.jda.api.EmbedBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import bot.api.ApiCaller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CodeforcesAPIImplTest {

    @Mock
    private ApiCaller apiCaller;

    private CodeforcesAPIImpl codeforcesAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeforcesAPI = new CodeforcesAPIImpl(apiCaller);
    }

    @Test
    void testGetUserSubmissions() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": [
                    {
                      "id": 1,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "A",
                        "name": "Test",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    }
                  ]
                }
                """;

        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);
        List<Submission> result = codeforcesAPI.getUserSubmissions("username");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OK", result.getFirst().getVerdict());
        assertEquals(800, result.getFirst().getProblem().getRating());
    }

    @Test
    void testFetchProblemRatings() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": [
                    {
                      "id": 1,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "A",
                        "name": "Test1",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    },
                    {
                      "id": 2,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "B",
                        "name": "Test2",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    },
                    {
                      "id": 3,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "C",
                        "name": "Test3",
                        "type": "PROGRAMMING",
                        "rating": 1000,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    }
                  ]
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        Map<Integer, Long> result = codeforcesAPI.fetchProblemRatings("username");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(800));
        assertEquals(1, result.get(1000));
    }

    @Test
    void testGetUserSubmissionsApiError() throws IOException {
        String mockResponse = """
                {
                  "status": "FAILED",
                  "comment": "Handle not found"
                }
                """;

        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);
        assertThrows(IOException.class, () -> codeforcesAPI.getUserSubmissions("non existent user"));
    }

    @Test
    void testCompareProblemRatings() throws IOException {
        CodeforcesAPIImpl spyCodeforcesAPI = spy(codeforcesAPI);
        Map<Integer, Long> mockRatings1 = Map.of(800, 2L, 1000, 1L);
        Map<Integer, Long> mockRatings2 = Map.of(800, 1L, 1000, 2L);
        doReturn(mockRatings1).doReturn(mockRatings2).when(spyCodeforcesAPI).fetchProblemRatings(anyString());

        File result = spyCodeforcesAPI.compareProblemRatings("user1", "user2");

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertTrue(result.length() > 0);
        assertEquals("problem_ratings.png", result.getName());
    }

    @Test
    void testGetUserSubmissionsEmptyList() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": []
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        List<Submission> result = codeforcesAPI.getUserSubmissions("new_user");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchProblemRatingsNoAcceptedProblems() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": [
                    {
                      "id": 1,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "A",
                        "name": "Test",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      },
                      "verdict": "WRONG_ANSWER"
                    }
                  ]
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        Map<Integer, Long> result = codeforcesAPI.fetchProblemRatings("username");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchProblemRatingsUnratedProblems() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": [
                    {
                      "id": 1,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "A",
                        "name": "Test",
                        "type": "PROGRAMMING",
                        "rating": 0,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    }
                  ]
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        Map<Integer, Long> result = codeforcesAPI.fetchProblemRatings("username");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchProblemRatingsMultipleRatings() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": [
                    {
                      "id": 1,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "A",
                        "name": "Test1",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      },
                      "verdict": "OK"
                    },
                    {
                      "id": 2,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "B",
                        "name": "Test2",
                        "type": "PROGRAMMING",
                        "rating": 1000,
                        "tags": ["math"]
                      },
                      "verdict": "OK"
                    },
                    {
                      "id": 3,
                      "contestId": 1,
                      "problem": {
                        "contestId": 1,
                        "index": "C",
                        "name": "Test3",
                        "type": "PROGRAMMING",
                        "rating": 1200,
                        "tags": ["dp"]
                      },
                      "verdict": "OK"
                    }
                  ]
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        Map<Integer, Long> result = codeforcesAPI.fetchProblemRatings("username");

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(800));
        assertEquals(1, result.get(1000));
        assertEquals(1, result.get(1200));
    }

    @Test
    void testInvalidJsonResponse() throws IOException {
        String invalidJsonResponse = "This is not a valid JSON";
        when(apiCaller.makeApiCall(anyString())).thenReturn(invalidJsonResponse);

        assertThrows(JsonSyntaxException.class, () -> codeforcesAPI.getUserSubmissions("username"));
    }

    @Test
    void testGetUserInfo() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": [
                    {
                        "lastName": "Korotkevich",
                        "country": "Belarus",
                        "lastOnlineTimeSeconds": 1728818245,
                        "city": "Gomel",
                        "rating": 4009,
                        "friendOfCount": 73322,
                        "handle": "tourist",
                        "firstName": "Gennady",
                        "contribution": 135,
                        "organization": "ITMO University",
                        "rank": "tourist",
                        "maxRating": 4009,
                        "registrationTimeSeconds": 1265987288,
                        "maxRank": "tourist"
                    }
                ]
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder result = codeforcesAPI.getUserInfo("tourist");

        assertNotNull(result);
        assertEquals("`tourist`'s Codeforces Info", result.build().getTitle());
        assertEquals("Belarus", result.getFields().stream().filter(field -> "Country".equals(field.getName())).findFirst().get().getValue());
        assertEquals("Gennady Korotkevich", result.getFields().stream().filter(field -> "Name".equals(field.getName())).findFirst().get().getValue());
        assertEquals("4009", result.getFields().stream().filter(field -> "Rating".equals(field.getName())).findFirst().get().getValue());
    }

    @Test
    void testGetUpcomingContests() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": [
                    {
                        "id": 2031,
                        "name": "Codeforces Round (Div. 2)",
                        "type": "CF",
                        "phase": "BEFORE",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1731674100,
                        "relativeTimeSeconds": -2793299
                    },
                    {
                        "id": 2028,
                        "name": "Codeforces Round (Div. 2)",
                        "type": "CF",
                        "phase": "BEFORE",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1731252900,
                        "relativeTimeSeconds": -2372099
                    },
                    {
                        "id": 2022,
                        "name": "Codeforces Round 978 (Div. 2)",
                        "type": "CF",
                        "phase": "FINISHED",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1728848100,
                        "relativeTimeSeconds": 32700
                    }
                ]
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder result = codeforcesAPI.getUpcomingContests();

        assertNotNull(result);
        assertEquals("Upcoming Contests", result.build().getTitle());
        assertEquals(2, result.getFields().size());
        assertEquals("Codeforces Round (Div. 2) (ID: 2031)", result.getFields().getFirst().getName());
        assertEquals("Codeforces Round (Div. 2) (ID: 2028)", result.getFields().getLast().getName());
    }

    @Test
    void testGetFinishedContests() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": [
                    {
                        "id": 2031,
                        "name": "Codeforces Round (Div. 2)",
                        "type": "CF",
                        "phase": "BEFORE",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1731674100,
                        "relativeTimeSeconds": -2793299
                    },
                    {
                        "id": 2028,
                        "name": "Codeforces Round (Div. 2)",
                        "type": "CF",
                        "phase": "BEFORE",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1731252900,
                        "relativeTimeSeconds": -2372099
                    },
                    {
                        "id": 2022,
                        "name": "Codeforces Round 978 (Div. 2)",
                        "type": "CF",
                        "phase": "FINISHED",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1728848100,
                        "relativeTimeSeconds": 32700
                    }
                ]
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder result = codeforcesAPI.getFinishedContests();

        assertNotNull(result);
        assertEquals("Finished Contests", result.build().getTitle());
        assertEquals(1, result.getFields().size());
        assertEquals("Codeforces Round 978 (Div. 2) (ID: 2022)", result.getFields().getFirst().getName());
    }

    @Test
    void testGetStandingContestForUserWithoutParticipation() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": {
                    "contest": {
                        "id": 2022,
                        "name": "Codeforces Round 978 (Div. 2)",
                        "type": "CF",
                        "phase": "FINISHED",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1728848100,
                        "relativeTimeSeconds": 33018
                    },
                    "problems": [
                        {
                            "contestId": 2022,
                            "index": "A",
                            "name": "Bus to Pénjamo",
                            "type": "PROGRAMMING",
                            "points": 750.0,
                            "tags": [
                                "greedy",
                                "implementation",
                                "math"
                            ]
                        }
                    ],
                    "rows": []
                }
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        // throw exception if the user not participated in the contest
        assertThrows(IOException.class, () -> codeforcesAPI.getStandingContestForUser("username", 1));
    }

    @Test
    void testGetStandingContestForUser() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": {
                    "contest": {
                        "id": 2022,
                        "name": "Codeforces Round 978 (Div. 2)",
                        "type": "CF",
                        "phase": "FINISHED",
                        "frozen": false,
                        "durationSeconds": 7200,
                        "startTimeSeconds": 1728848100,
                        "relativeTimeSeconds": 33465
                    },
                    "problems": [
                        {
                            "contestId": 2022,
                            "index": "A",
                            "name": "Bus to Pénjamo",
                            "type": "PROGRAMMING",
                            "points": 750.0,
                            "tags": [
                                "greedy",
                                "implementation",
                                "math"
                            ]
                        }
                    ],
                    "rows": [
                        {
                            "rank": 6444,
                            "points": 480.0,
                            "penalty": 0,
                            "successfulHackCount": 0,
                            "unsuccessfulHackCount": 0,
                            "problemResults": [
                                {
                                    "points": 480.0,
                                    "rejectedAttemptCount": 0,
                                    "type": "FINAL",
                                    "bestSubmissionTimeSeconds": 5439
                                }
                            ]
                        }
                    ]
                }
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);
        EmbedBuilder result = codeforcesAPI.getStandingContestForUser("username", 2022);

        assertNotNull(result);
        assertEquals("`username`'s Standing in Contest", result.build().getTitle());
        assertEquals("6444", result.getFields().stream().filter(field -> "Rank".equals(field.getName())).findFirst().get().getValue());
        assertEquals("1/1", result.getFields().stream().filter(field -> "Solved".equals(field.getName())).findFirst().get().getValue());
    }
    @Test
    void testGetRatingHistory() throws IOException {
        String mockResponse = """
            {
                "status": "OK",
                "result": [
                    {
                        "contestId": 1676,
                        "contestName": "Codeforces Round 790 (Div. 4)",
                        "handle": "Loay_Ghreeb",
                        "rank": 12995,
                        "ratingUpdateTimeSeconds": 1652201100,
                        "oldRating": 0,
                        "newRating": 377
                    },
                    {
                        "contestId": 1680,
                        "contestName": "Educational Codeforces Round 128 (Rated for Div. 2)",
                        "handle": "Loay_Ghreeb",
                        "rank": 10674,
                        "ratingUpdateTimeSeconds": 1652459700,
                        "oldRating": 377,
                        "newRating": 614
                    }
                ]
            }""";
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        List<Rating> result = codeforcesAPI.getRatingHistory("username");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(377, result.getFirst().getNewRating());
        assertEquals(614, result.get(1).getNewRating());
    }

    @Test
    void testGetRandomProblem() throws IOException {
        String mockResponse = """
                {
                  "status": "OK",
                  "result": {
                    "problems": [
                      {
                        "contestId": 1,
                        "index": "A",
                        "name": "Problem A",
                        "type": "PROGRAMMING",
                        "rating": 800,
                        "tags": ["implementation"]
                      }
                    ],
                    "problemStatistics": []
                  }
                }
                """;
        when(apiCaller.makeApiCall(anyString())).thenReturn(mockResponse);

        EmbedBuilder result = codeforcesAPI.getRandomProblem(List.of("implementation"), 800, 1000);

        assertNotNull(result);
        assertEquals("A. Problem A", result.build().getTitle());
        assertEquals("800", result.getFields().stream().filter(field -> "Rating".equals(field.getName())).findFirst().get().getValue());
    }
}
