package bot.infrastructure;

import bot.api.ApiCaller;
import bot.api.ApiResponse;
import bot.api.CodeforcesAPI;
import bot.cache.RedisUtil;
import bot.domain.contest.Contest;
import bot.domain.contest.Problem;
import bot.domain.contest.ProblemSetResult;
import bot.domain.contest.StandingsResponse;
import bot.domain.user.Rating;
import bot.domain.user.UserInfo;
import bot.util.EmbedBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CodeforcesAPIImpl implements CodeforcesAPI {
    private static final String BASE_URL = "https://codeforces.com/api/";
    private static final Gson gson = new Gson();
    private final ApiCaller apiCaller;
    RedisUtil redisUtil = new RedisUtil();

    public CodeforcesAPIImpl(ApiCaller apiCaller) {
        this.apiCaller = apiCaller;
    }

    @Override
    public EmbedBuilder getUserInfo(String handle) throws IOException {
        String cacheKey = "user_info:" + handle; // Unique cache key

        // Check Redis Cache
        UserInfo cachedUserInfo = redisUtil.getObjectFromRedis(cacheKey, UserInfo.class);
        if (cachedUserInfo != null) {
            return buildUserInfoEmbed(cachedUserInfo);
        }

        // If not cached, fetch from the API
        String url = BASE_URL + "user.info" + "?handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        Type responseType = new TypeToken<ApiResponse<List<UserInfo>>>() {}.getType();
        ApiResponse<List<UserInfo>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null && !apiResponse.getResult().isEmpty()) {
            UserInfo userInfo = apiResponse.getResult().get(0);

            // Cache the result
            redisUtil.storeObjectInRedis(cacheKey, userInfo);

            return buildUserInfoEmbed(userInfo);
        } else {
            throw new IOException("Failed to retrieve user info");
        }
    }

    private List<Contest> getContests(String url) throws IOException {
        String jsonResponse = apiCaller.makeApiCall(url);
        Type responseType = new TypeToken<ApiResponse<List<Contest>>>() {}.getType();
        ApiResponse<List<Contest>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return apiResponse.getResult();
        } else {
            throw new IOException("Failed to retrieve contests");
        }
    }

    @Override
    public EmbedBuilder getUpcomingContests() throws IOException {
        String cacheKey = "upcoming_contests";

        // Check Redis Cache
        List<Contest> cachedContests = redisUtil.getObjectFromRedis(cacheKey, List.class);
        if (cachedContests != null) {
            return buildContestsEmbed(cachedContests, "Upcoming Contests", Color.ORANGE, "BEFORE");
        }

        // Fetch from the API if not cached
        String url = BASE_URL + "contest.list" + "?gym=false";
        List<Contest> contests = getContests(url);

        // Cache the result
        redisUtil.storeObjectInRedis(cacheKey, contests);

        return buildContestsEmbed(contests, "Upcoming Contests", Color.ORANGE, "BEFORE");
    }

    // Helper method to build EmbedBuilder for contests
    private EmbedBuilder buildContestsEmbed(List<Contest> contests, String title, Color color, String phaseFilter) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setColor(color);

        int count = 0;
        for (Contest contest : contests) {
            if (contest.getPhase().equals(phaseFilter)) {
                String name = contest.getName();
                String contestId = String.valueOf(contest.getId());
                String startTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(contest.getStartTimeSeconds() * 1000));
                String duration = String.valueOf(contest.getDurationSeconds() / 3600) + " hours";

                long relativeTimeSeconds = contest.getRelativeTimeSeconds();
                String relativeTime;

                if (relativeTimeSeconds < 0) {
                    long absTimeSeconds = Math.abs(relativeTimeSeconds);
                    long days = absTimeSeconds / (24 * 3600);
                    long hours = (absTimeSeconds % (24 * 3600)) / 3600;
                    long minutes = (absTimeSeconds % 3600) / 60;
                    relativeTime = String.format("Before start %d days %d hours %d minutes", days, hours, minutes);
                } else {
                    relativeTime = String.valueOf(relativeTimeSeconds / 3600) + " hours";
                }

                embed.addField(name + " (ID: " + contestId + ")", "Start Time: " + startTime + "\nDuration: " + duration + "\n" + relativeTime, false);
                count++;

                if (count == 5) {
                    break;
                }
            } else {
                break;
            }
        }
        return embed;
    }

    // Return last 5 finished contests
    @Override
    public EmbedBuilder getFinishedContests() throws IOException {
        String cacheKey = "finished_contests";

        // Check Redis Cache
        List<Contest> cachedContests = redisUtil.getObjectFromRedis(cacheKey, List.class);
        if (cachedContests != null) {
            return buildContestsEmbed(cachedContests, "Finished Contests", Color.GREEN, "FINISHED");
        }

        // Fetch from the API if not cached
        String url = BASE_URL + "contest.list" + "?gym=false";
        List<Contest> contests = getContests(url);

        // Cache the result
        redisUtil.storeObjectInRedis(cacheKey, contests);

        return buildContestsEmbed(contests, "Finished Contests", Color.GREEN, "FINISHED");
    }

    private EmbedBuilder buildUserInfoEmbed(UserInfo userInfo) {
        EmbedBuilder embed = new EmbedBuilder();
        String handle = "`" + userInfo.handle + "`";
        embed.setTitle(handle + "'s Codeforces Info");
        embed.setColor(Color.BLUE);

        if (userInfo.avatar != null && !userInfo.avatar.isEmpty()) {
            embed.setThumbnail(userInfo.avatar);
        }

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Name", userInfo.firstName != null && userInfo.lastName != null ? userInfo.firstName + " " + userInfo.lastName : null);
        fields.put("Country", userInfo.country);
        fields.put("City", userInfo.city);
        fields.put("Organization", userInfo.organization);
        fields.put("Rating", String.valueOf(userInfo.rating));
        fields.put("Max Rating", String.valueOf(userInfo.maxRating));
        fields.put("Rank", userInfo.rank);
        fields.put("Max Rank", userInfo.maxRank);
        fields.put("Contribution", String.valueOf(userInfo.contribution));

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                embed.addField(entry.getKey(), entry.getValue(), false);
            }
        }
        return embed;
    }

    @Override
    public EmbedBuilder getStandingContestForUser(String handle, int contestId) throws IOException {
        String url = BASE_URL + "contest.standings?contestId=" + contestId + "&asManager=false&handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);

        Type responseType = new TypeToken<ApiResponse<StandingsResponse>>() {}.getType();
        ApiResponse<StandingsResponse> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null && !apiResponse.getResult().getRows().isEmpty()) {
            StandingsResponse.StandingsRow userStanding = apiResponse.getResult().getRows().getFirst();

            String contestName = apiResponse.getResult().getContest().getName();
            String contestStartTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .format(new java.util.Date(apiResponse.getResult().getContest().getStartTimeSeconds() * 1000));

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

            List<Problem> problems = apiResponse.getResult().getProblems();
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
        } else {
            throw new IOException("Failed to retrieve user standings");
        }
    }

    @Override
    public List<Rating> getRatingHistory(String handle) throws IOException {
        String cacheKey = "rating_history:" + handle;

        // Check Redis Cache
        List<Rating> cachedRatings = redisUtil.getObjectFromRedis(cacheKey, List.class);
        if (cachedRatings != null) {
            return cachedRatings;
        }

        // Fetch from the API if not cached
        String url = BASE_URL + "user.rating" + "?handle=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        Type responseType = new TypeToken<ApiResponse<List<Rating>>>() {}.getType();
        ApiResponse<List<Rating>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            List<Rating> ratings = apiResponse.getResult();

            // Cache the result
            redisUtil.storeObjectInRedis(cacheKey, ratings);

            return ratings;
        } else {
            throw new IOException("Failed to retrieve rating history");
        }
    }

    @Override
    public EmbedBuilder getRandomProblem(@NotNull List<String> tagsList, int rateStart, int rateEnd) throws IOException {
        String url = BASE_URL + "problemset.problems";
        if (!tagsList.isEmpty()) {
            url += "?tags=" + String.join(";", tagsList);
        }
        String jsonResponse = apiCaller.makeApiCall(url);

        Type responseType = new TypeToken<ApiResponse<ProblemSetResult>>() {}.getType();
        ApiResponse<ProblemSetResult> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            List<Problem> problems = apiResponse.getResult().getProblems();
            List<Problem> filteredProblems = problems.stream()
                    .filter(problem -> problem.getRating() >= rateStart && problem.getRating() <= rateEnd)
                    .toList();
            if (filteredProblems.isEmpty()) {
                throw new IOException("No problem found in the given rating range");
            }
            Random random = new Random();
            Problem randomProblem = filteredProblems.get(random.nextInt(filteredProblems.size()));
            return EmbedBuilderUtil.buildRandomProblemEmbed(randomProblem);
        } else {
            throw new IOException("Failed to retrieve random problem");
        }
    }
}
