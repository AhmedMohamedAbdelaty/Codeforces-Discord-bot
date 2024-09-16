package bot.domain.contest;

import java.io.Serializable;
import java.util.List;

public class StandingsResponse implements Serializable{
    private static final long serialVersionUID = 5L;
    private Contest contest;
    private List<Problem> problems;
    private List<StandingsRow> rows;

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public List<StandingsRow> getRows() {
        return rows;
    }

    public void setRows(List<StandingsRow> rows) {
        this.rows = rows;
    }

    public static class ProblemResult {
        private String index;
        private int points;
        private int rejectedAttemptCount;
        private String type;
        private long bestSubmissionTimeSeconds;

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getRejectedAttemptCount() {
            return rejectedAttemptCount;
        }

        public void setRejectedAttemptCount(int rejectedAttemptCount) {
            this.rejectedAttemptCount = rejectedAttemptCount;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getBestSubmissionTimeSeconds() {
            return bestSubmissionTimeSeconds;
        }

        public void setBestSubmissionTimeSeconds(long bestSubmissionTimeSeconds) {
            this.bestSubmissionTimeSeconds = bestSubmissionTimeSeconds;
        }
    }

    public static class StandingsRow {
        private Party party;
        private int rank;
        private int points;
        private int penalty;
        private List<ProblemResult> problemResults;

        public Party getParty() {
            return party;
        }

        public void setParty(Party party) {
            this.party = party;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getPenalty() {
            return penalty;
        }

        public void setPenalty(int penalty) {
            this.penalty = penalty;
        }

        public List<ProblemResult> getProblemResults() {
            return problemResults;
        }

        public void setProblemResults(List<ProblemResult> problemResults) {
            this.problemResults = problemResults;
        }
    }

    public static class Party {
        private int contestId;
        private List<Member> members;
        private String participantType;
        private boolean ghost;
        private long startTimeSeconds;

        public int getContestId() {
            return contestId;
        }

        public void setContestId(int contestId) {
            this.contestId = contestId;
        }

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        public String getParticipantType() {
            return participantType;
        }

        public void setParticipantType(String participantType) {
            this.participantType = participantType;
        }

        public boolean isGhost() {
            return ghost;
        }

        public void setGhost(boolean ghost) {
            this.ghost = ghost;
        }

        public long getStartTimeSeconds() {
            return startTimeSeconds;
        }

        public void setStartTimeSeconds(long startTimeSeconds) {
            this.startTimeSeconds = startTimeSeconds;
        }
    }

    public static class Member {
        private String handle;

        public String getHandle() {
            return handle;
        }

        public void setHandle(String handle) {
            this.handle = handle;
        }
    }

}
