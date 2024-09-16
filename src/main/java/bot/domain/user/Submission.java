package bot.domain.user;

import bot.domain.contest.Problem;

public class Submission {
    private int id;
    private int contestId;
    private Problem problem;
    private String verdict;

    public Problem getProblem() {
        return problem;
    }

    public String getVerdict() {
        return verdict;
    }
}
