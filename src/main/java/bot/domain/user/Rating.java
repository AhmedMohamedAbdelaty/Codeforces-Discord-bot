package bot.domain.user;

import com.google.gson.annotations.SerializedName;

public class Rating {
    @SerializedName("contestId")
    private int contestId;

    @SerializedName("contestName")
    private String contestName;

    @SerializedName("handle")
    private String handle;

    @SerializedName("rank")
    private int rank;

    @SerializedName("ratingUpdateTimeSeconds")
    private long ratingUpdateTimeSeconds;

    @SerializedName("oldRating")
    private int oldRating;

    @SerializedName("newRating")
    private int newRating;

    // Getters and Setters
    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getRatingUpdateTimeSeconds() {
        return ratingUpdateTimeSeconds;
    }

    public void setRatingUpdateTimeSeconds(long ratingUpdateTimeSeconds) {
        this.ratingUpdateTimeSeconds = ratingUpdateTimeSeconds;
    }

    public int getOldRating() {
        return oldRating;
    }

    public void setOldRating(int oldRating) {
        this.oldRating = oldRating;
    }

    public int getNewRating() {
        return newRating;
    }

    public void setNewRating(int newRating) {
        this.newRating = newRating;
    }
}