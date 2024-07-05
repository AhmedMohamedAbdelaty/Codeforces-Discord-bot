package bot.domain.contest;

import java.util.List;

public class ProblemSetResult {
    private List<Problem> problems;
    private List<ProblemStatistics> problemStatistics;

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public List<ProblemStatistics> getProblemStatistics() {
        return problemStatistics;
    }

    public void setProblemStatistics(List<ProblemStatistics> problemStatistics) {
        this.problemStatistics = problemStatistics;
    }
}
