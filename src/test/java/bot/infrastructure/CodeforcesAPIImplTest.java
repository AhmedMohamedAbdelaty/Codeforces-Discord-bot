package bot.infrastructure;

import bot.domain.user.Submission;
import com.google.gson.JsonSyntaxException;
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
}
