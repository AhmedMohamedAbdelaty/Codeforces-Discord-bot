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
import bot.util.EmbedBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

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
            StandingsResponse.StandingsRow userStanding = apiResponse.getResult().getRows().getFirst(); // Use get(0) instead of getFirst()

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


    @Override
    public EmbedBuilder getRandomContest(SlashCommandInteractionEvent event, List<String> usernames, String contestType, ZonedDateTime startTime, ZonedDateTime userTime) throws IOException {
        String contestsURL = BASE_URL + "contest.list" + "?gym=false";
        List<Contest> contests = getContests(contestsURL);

        switch (contestType.toLowerCase()) {
            case "div1":
                contestType = "Div. 1";
                break;
            case "div2":
                contestType = "Div. 2";
                break;
            case "div3":
                contestType = "Div. 3";
                break;
            case "div4":
                contestType = "Div. 4";
                break;
            default:
        }

        String finalContestType = contestType;
        contests = contests.stream()
                .filter(contest -> contest.getName().contains(finalContestType))
                .toList();


        // Set to store the contests that the users have participated in
        Set<Integer> participatedContests = new HashSet<>();

        for (String username : usernames) {
            String userURL = BASE_URL + "user.status?handle=" + username;
            String jsonResponse = apiCaller.makeApiCall(userURL);
            Type responseType = new TypeToken<ApiResponse<List<Submission>>>() {
            }.getType();
            ApiResponse<List<Submission>> apiResponse = gson.fromJson(jsonResponse, responseType);


            if ("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null) {
                for (Submission submission : apiResponse.getResult()) {
                    if (submission.getVerdict() == Submission.Verdict.OK) {
                        participatedContests.add(submission.getContestId());
                    }
                }
            }
        }

        // Filter out contests that users have participated in
        List<Contest> availableContests = contests.stream()
                .filter(contest -> !participatedContests.contains(contest.getId()))
                .toList();

        if (availableContests.isEmpty()) {
            throw new IOException("No suitable contests found.");
        }

        // Select a random contest
        Random random = new Random();
        Contest selectedContest = availableContests.get(random.nextInt(availableContests.size()));

        return EmbedBuilderUtil.buildRandomContestEmbed(selectedContest, usernames, userTime, event);
    }
}