package bot.infrastructure;

import bot.api.ApiCaller;
import bot.api.ApiResponse;
import bot.api.CodeforcesAPI;
import bot.cache.RedisCache;
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

import static bot.util.EmbedBuilderUtil.buildStandingEmbed;


public class CodeforcesAPIImpl implements CodeforcesAPI {
    private static final String BASE_URL = "https://codeforces.com/api/";
    private static final Gson gson = new Gson();
    private final ApiCaller apiCaller;
    private static final int CACHE_EXPIRATION_SECONDS_PROBLEMS = 60 * 60 * 24 * 7; // 1 week
    private static final int CACHE_EXPIRATION_SECONDS_USER_INFO = 60 * 60 * 24; // 1 day
    private static final int CACHE_EXPIRATION_SECONDS_CONTESTS = 60 * 60 * 24; // 1 day

    public CodeforcesAPIImpl(ApiCaller apiCaller) {
        this.apiCaller = apiCaller;
    }

    @Override
    public EmbedBuilder getUserInfo(String handle) throws IOException {
        String cacheKey = "user_info_" + handle;
        String cachedResponse = RedisCache.get(cacheKey);

        if (cachedResponse != null) {
            Type responseType = new TypeToken<ApiResponse<List<UserInfo>>>() {
            }.getType();
            ApiResponse<List<UserInfo>> apiResponse = gson.fromJson(cachedResponse, responseType);
            return buildUserInfoEmbed(apiResponse.getResult().getFirst());
        }

        String url = BASE_URL + "user.info" + "?handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        RedisCache.setWithExpiration(cacheKey, jsonResponse, CACHE_EXPIRATION_SECONDS_USER_INFO);

        Type responseType = new TypeToken<ApiResponse<List<UserInfo>>>() {
        }.getType();
        ApiResponse<List<UserInfo>> apiResponse = gson.fromJson(jsonResponse, responseType);
        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null && !apiResponse.getResult().isEmpty()) {
            return buildUserInfoEmbed(apiResponse.getResult().getFirst());
        } else {
            throw new IOException("Failed to retrieve user info");
        }
    }

    private List<Contest> getContests(String url) throws IOException {
        String cacheKey = "contests_" + url.hashCode();
        String cachedResponse = RedisCache.get(cacheKey);

        if (cachedResponse != null) {
            Type responseType = new TypeToken<ApiResponse<List<Contest>>>() {
            }.getType();
            ApiResponse<List<Contest>> apiResponse = gson.fromJson(cachedResponse, responseType);
            return apiResponse.getResult();
        }

        String jsonResponse = apiCaller.makeApiCall(url);
        RedisCache.setWithExpiration(cacheKey, jsonResponse, CACHE_EXPIRATION_SECONDS_CONTESTS);

        Type responseType = new TypeToken<ApiResponse<List<Contest>>>() {
        }.getType();
        ApiResponse<List<Contest>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return apiResponse.getResult();
        } else {
            throw new IOException("Failed to retrieve contests");
        }
    }

    @Override
    public EmbedBuilder getUpcomingContests() throws IOException {
        String url = BASE_URL + "contest.list" + "?gym=false";
        List<Contest> contests = getContests(url);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Upcoming Contests");
        embed.setColor(Color.ORANGE);

        int count = 0;
        for (Contest contest : contests) {
            if (contest.getPhase().equals("BEFORE")) {
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
                // add the id after the name
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
        String url = BASE_URL + "contest.list" + "?gym=false";
        List<Contest> contests = getContests(url);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Finished Contests");
        embed.setColor(Color.GREEN);

        int count = 0;
        for (Contest contest : contests) {
            if (contest.getPhase().equals("FINISHED")) {
                String name = contest.getName();
                String contestId = String.valueOf(contest.getId());
                String startTime = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(contest.getStartTimeSeconds() * 1000));
                String duration = String.valueOf(contest.getDurationSeconds() / 3600) + " hours";

                long relativeTimeSeconds = contest.getRelativeTimeSeconds();
                String relativeTime;

                // Relative time should be like : "Finished 2 days ago" or "Finished 2 hours ago" or "Finished 2 minutes ago"
                long absTimeSeconds = Math.abs(relativeTimeSeconds);
                long days = absTimeSeconds / (24 * 3600);
                long hours = (absTimeSeconds % (24 * 3600)) / 3600;
                long minutes = (absTimeSeconds % 3600) / 60;
                relativeTime = String.format("Finished %d days %d hours %d minutes ago", days, hours, minutes);

                // add the id after the name
                embed.addField(name + " (ID: " + contestId + ")", "Start Time: " + startTime + "\nDuration: " + duration + "\n" + relativeTime, false);
                count++;

                if (count == 5) {
                    break;
                }
            }
        }
        return embed;
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
        String cacheKey = "standing_" + handle + "_" + contestId;
        String cachedResponse = RedisCache.get(cacheKey);

        if (cachedResponse != null) {
            Type responseType = new TypeToken<ApiResponse<StandingsResponse>>() {
            }.getType();
            ApiResponse<StandingsResponse> apiResponse = gson.fromJson(cachedResponse, responseType);
            return buildStandingEmbed(apiResponse.getResult(), handle, contestId);
        }

        String url = BASE_URL + "contest.standings?contestId=" + contestId + "&asManager=false&handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        RedisCache.setWithExpiration(cacheKey, jsonResponse, CACHE_EXPIRATION_SECONDS_CONTESTS);

        Type responseType = new TypeToken<ApiResponse<StandingsResponse>>() {
        }.getType();
        ApiResponse<StandingsResponse> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null && !apiResponse.getResult().getRows().isEmpty()) {
            return buildStandingEmbed(apiResponse.getResult(), handle, contestId);
        } else {
            throw new IOException("Failed to retrieve user standings");
        }
    }

    @Override
    public List<Rating> getRatingHistory(String handle) throws IOException {
        String cacheKey = "rating_history_" + handle;
        String cachedResponse = RedisCache.get(cacheKey);

        if (cachedResponse != null) {
            Type responseType = new TypeToken<ApiResponse<List<Rating>>>() {}.getType();
            ApiResponse<List<Rating>> apiResponse = gson.fromJson(cachedResponse, responseType);
            return apiResponse.getResult();
        }

        String url = BASE_URL + "user.rating" + "?handle=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        RedisCache.setWithExpiration(cacheKey, jsonResponse, CACHE_EXPIRATION_SECONDS_USER_INFO);

        Type responseType = new TypeToken<ApiResponse<List<Rating>>>() {}.getType();
        ApiResponse<List<Rating>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return apiResponse.getResult();
        } else {
            throw new IOException("Failed to retrieve rating history");
        }
    }

    @Override
    public EmbedBuilder getRandomProblem(@NotNull List<String> tagsList, int rateStart, int rateEnd) throws IOException {
        String cacheKey = "problem_set_" + String.join("_", tagsList) + "_" + rateStart + "_" + rateEnd;
        String cachedResponse = RedisCache.get(cacheKey);

        if (cachedResponse != null) {
            Type responseType = new TypeToken<ApiResponse<ProblemSetResult>>() {}.getType();
            ApiResponse<ProblemSetResult> apiResponse = gson.fromJson(cachedResponse, responseType);
            return getRandomProblemFromResult(apiResponse.getResult(), rateStart, rateEnd);
        }

        String url = BASE_URL + "problemset.problems";
        if (!tagsList.isEmpty()) {
            url += "?tags=" + String.join(";", tagsList);
        }
        String jsonResponse = apiCaller.makeApiCall(url);
        RedisCache.setWithExpiration(cacheKey, jsonResponse, CACHE_EXPIRATION_SECONDS_PROBLEMS);

        Type responseType = new TypeToken<ApiResponse<ProblemSetResult>>() {}.getType();
        ApiResponse<ProblemSetResult> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return getRandomProblemFromResult(apiResponse.getResult(), rateStart, rateEnd);
        } else {
            throw new IOException("Failed to retrieve random problem");
        }
    }

    private EmbedBuilder getRandomProblemFromResult(ProblemSetResult result, int rateStart, int rateEnd) throws IOException {
        List<Problem> problems = result.getProblems();
        List<Problem> filteredProblems = problems.stream()
                .filter(problem -> problem.getRating() >= rateStart && problem.getRating() <= rateEnd)
                .toList();
        if (filteredProblems.isEmpty()) {
            throw new IOException("No problem found in the given rating range");
        }
        Random random = new Random();
        Problem randomProblem = filteredProblems.get(random.nextInt(filteredProblems.size()));
        return EmbedBuilderUtil.buildRandomProblemEmbed(randomProblem);
    }
}