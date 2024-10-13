package bot.infrastructure;

import bot.api.ApiCaller;
import bot.api.ApiResponse;
import bot.api.CodeforcesAPI;
import bot.domain.contest.Contest;
import bot.domain.contest.Problem;
import bot.domain.contest.ProblemSetResult;
import bot.domain.contest.StandingsResponse;
import bot.domain.user.Rating;
import bot.domain.user.Submission;
import bot.domain.user.UserInfo;
import bot.domain.user.Verdict;
import bot.util.EmbedBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeforcesAPIImpl implements CodeforcesAPI {
    private static final String BASE_URL = "https://codeforces.com/api/";
    private static final Gson gson = new Gson();
    private final ApiCaller apiCaller;

    public CodeforcesAPIImpl(ApiCaller apiCaller) {
        this.apiCaller = apiCaller;
    }

    @Override
    public EmbedBuilder getUserInfo(String handle) throws IOException {
        String url = BASE_URL + "user.info" + "?handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
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
        String jsonResponse = apiCaller.makeApiCall(url);
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
        String url = BASE_URL + "contest.standings?contestId=" + contestId + "&asManager=false&handles=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);

        Type responseType = new TypeToken<ApiResponse<StandingsResponse>>() {
        }.getType();
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
//            embed.addField("Penalty", String.valueOf(userStanding.getPenalty()), false);
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
        String url = BASE_URL + "user.rating" + "?handle=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        Type responseType = new TypeToken<ApiResponse<List<Rating>>>() {
        }.getType();
        ApiResponse<List<Rating>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return apiResponse.getResult();
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

        Type responseType = new TypeToken<ApiResponse<ProblemSetResult>>() {
        }.getType();
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

    public List<Submission> getUserSubmissions(String handle) throws IOException {
        String url = BASE_URL + "user.status" + "?handle=" + handle;
        String jsonResponse = apiCaller.makeApiCall(url);
        Type responseType = new TypeToken<ApiResponse<List<Submission>>>() {
        }.getType();
        ApiResponse<List<Submission>> apiResponse = gson.fromJson(jsonResponse, responseType);

        if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
            return apiResponse.getResult();
        } else {
            throw new IOException("Failed to retrieve user submissions");
        }
    }

    /**
     * @return a map of problem ratings and the number of problems solved by the user with that rating
     */
    public Map<Integer, Long> fetchProblemRatings(String handle) throws IOException {
        List<Submission> submissions = getUserSubmissions(handle);
        Set<Problem> acceptedProblems = submissions.stream()
                                                   .filter(submission -> Verdict.OK.toString().equals(submission.getVerdict()))
                                                   .map(Submission::getProblem)
                                                   .collect(Collectors.toSet());

        return acceptedProblems.stream()
                               .filter(problem -> problem.getRating() > 0)
                               .collect(Collectors.groupingBy(Problem::getRating, Collectors.counting()));
    }

    @Override
    public File getProblemRatings(String handle) throws IOException {
        Map<Integer, Long> problemRatings = fetchProblemRatings(handle);
        List<Integer> ratings = createRatingsList();
        List<Long> solved = createSolvedList(problemRatings, ratings);

        CategoryChart chart = createChart(handle + " Problem Ratings", handle, ratings, solved);

        return saveChart(chart);
    }

    @Override
    public File compareProblemRatings(String handle1, String handle2) throws IOException {
        Map<Integer, Long> problemRatings1 = fetchProblemRatings(handle1);
        Map<Integer, Long> problemRatings2 = fetchProblemRatings(handle2);

        List<Integer> ratings = createRatingsList();
        List<Long> solved1 = createSolvedList(problemRatings1, ratings);
        List<Long> solved2 = createSolvedList(problemRatings2, ratings);

        CategoryChart chart = createChart(handle1 + " vs " + handle2, handle1, ratings, solved1);
        chart.addSeries(handle2, ratings, solved2);

        return saveChart(chart);
    }

    private List<Integer> createRatingsList() {
        List<Integer> ratings = new ArrayList<>();
        for (int rate = 800; rate <= 2200; rate += 100) {
            ratings.add(rate);
        }
        return ratings;
    }

    private List<Long> createSolvedList(Map<Integer, Long> problemRatings, List<Integer> ratings) {
        List<Long> solved = new ArrayList<>();
        for (int rate : ratings) {
            solved.add(problemRatings.getOrDefault(rate, 0L));
        }
        return solved;
    }

    private CategoryChart createChart(String title, String seriesName, List<Integer> ratings, List<Long> solved) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(1920)
                .height(1080)
                .title(title)
                .xAxisTitle("Problem Rating")
                .yAxisTitle("Number of Problems Solved")
                .build();

        chart.addSeries(seriesName, ratings, solved);

        Font font = new Font("Arial", Font.PLAIN, 20);
        chart.getStyler().setBaseFont(font);
        chart.getStyler().setLegendFont(font);
        chart.getStyler().setAxisTickLabelsFont(font);
        chart.getStyler().setAxisTitleFont(font);
        chart.getStyler().setChartButtonFont(font);
        chart.getStyler().setChartTitleFont(font);

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setStacked(false);
        chart.getStyler().setLabelsVisible(true);
        chart.getStyler().setLabelsFont(new Font("Arial", Font.BOLD, 20));
        chart.getStyler().setLabelsFontColorAutomaticEnabled(false);
        return chart;
    }

    private File saveChart(CategoryChart chart) throws IOException {
        BitmapEncoder.saveBitmap(chart, "problem_ratings.png", BitmapEncoder.BitmapFormat.PNG);
        return new File("problem_ratings.png");
    }
}
