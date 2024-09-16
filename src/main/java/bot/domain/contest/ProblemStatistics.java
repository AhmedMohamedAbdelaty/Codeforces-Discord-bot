package bot.domain.contest;

import java.io.Serializable;

public class ProblemStatistics implements Serializable{
    private static final long serialVersionUID = 4L;
    private int contestId;
    private String index;
    private int solvedCount;

    // Getters and setters
    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }
}
