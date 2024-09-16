package bot.domain.contest;

import java.util.List;

public class Party {
    private Integer contestId; // Can be absent
    private List<Member> members;
    private String participantType;
    private boolean ghost;
    private long startTimeSeconds;

    // Inner class for Member
    public static class Member {
        private String handle;

        public Member(String handle) {
            this.handle = handle;
        }

        public String getHandle() {
            return handle;
        }

        public void setHandle(String handle) {
            this.handle = handle;
        }
    }

    // Constructor
    public Party(Integer contestId, List<Member> members, String participantType, boolean ghost, long startTimeSeconds) {
        this.contestId = contestId;
        this.members = members;
        this.participantType = participantType;
        this.ghost = ghost;
        this.startTimeSeconds = startTimeSeconds;
    }

    // Getters and Setters
    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
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