package bot.api;


import bot.domain.user.Rating;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;
import java.util.List;

public interface CodeforcesAPI {
    // Get user information
    EmbedBuilder getUserInfo(String handle) throws IOException;

    // Get upcoming contests
    EmbedBuilder getUpcomingContests() throws IOException;

    // Get finished contests
    EmbedBuilder getFinishedContests() throws IOException;

    // Get standing of a contest for a user
    EmbedBuilder getStandingContestForUser(String handle, int contestId) throws IOException;

    // Get rating history of a user as a graph
    List<Rating> getRatingHistory(String handle) throws IOException;

    // Get a random problem
    EmbedBuilder getRandomProblem(List<String> tagsList, int rateStart, int rateEnd) throws IOException;
}
