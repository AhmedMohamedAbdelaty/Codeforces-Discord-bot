package bot.domain.user;

import bot.domain.contest.Problem;

public class Submission {
    private int id;
    private int contestId;
    private Problem problem;
    private Verdict verdict;

    public Problem getProblem() {
        return problem;
    }

    public Verdict getVerdict() {
        return verdict;
    }
}
