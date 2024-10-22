package bot.domain.contest;

import java.util.List;
import java.util.Objects;

public class Problem {
    private int contestId;
    private String index;
    private String name;
    private String type;
    private int rating;
    private List<String> tags;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Problem problem = (Problem) obj;
        return contestId == problem.contestId &&
                rating == problem.rating &&
                index.equals(problem.index) &&
                name.equals(problem.name) &&
                type.equals(problem.type) &&
                tags.equals(problem.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contestId, index, name, type, rating, tags);
    }
}