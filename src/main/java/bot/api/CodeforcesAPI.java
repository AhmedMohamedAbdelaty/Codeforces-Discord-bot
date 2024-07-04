package bot.api;


import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;

public interface CodeforcesAPI {
    EmbedBuilder getUserInfo(String handle) throws IOException;

    EmbedBuilder getUpcomingContests() throws IOException;

    EmbedBuilder getFinishedContests() throws IOException;

    EmbedBuilder getStandingContestForUser(String handle, int contestId) throws IOException;
}
