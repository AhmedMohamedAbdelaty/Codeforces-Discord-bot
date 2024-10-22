package bot.domain.contest;

import java.io.Serializable;
import java.util.List;

public class ProblemSetResult implements Serializable{
    private static final long serialVersionUID = 3L;
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
