package bot.domain.user;

import bot.domain.contest.Party;
import bot.domain.contest.Problem;


public class Submission {
    private long id;
    private Integer contestId; // Can be absent
    private long creationTimeSeconds;
    private int relativeTimeSeconds;
    private Problem problem;
    private Party author;
    private String programmingLanguage;
    private Verdict verdict; // Can be absent
    private Testset testset;
    private int passedTestCount;
    private int timeConsumedMillis;
    private long memoryConsumedBytes;
    private Float points; // Can be absent

    @Override
    public String toString() {
        return "Submission{" +
               "id=" + id +
               ", contestId=" + contestId +
               ", creationTimeSeconds=" + creationTimeSeconds +
               ", relativeTimeSeconds=" + relativeTimeSeconds +
               ", problem=" + problem +
               ", author=" + author +
               ", programmingLanguage='" + programmingLanguage + '\'' +
               ", verdict=" + verdict +
               ", testset=" + testset +
               ", passedTestCount=" + passedTestCount +
               ", timeConsumedMillis=" + timeConsumedMillis +
               ", memoryConsumedBytes=" + memoryConsumedBytes +
               ", points=" + points +
               '}';
    }

    // Enum for Verdict
    public enum Verdict {
        FAILED, OK, PARTIAL, COMPILATION_ERROR, RUNTIME_ERROR, WRONG_ANSWER, PRESENTATION_ERROR,
        TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED, IDLENESS_LIMIT_EXCEEDED, SECURITY_VIOLATED,
        CRASHED, INPUT_PREPARATION_CRASHED, CHALLENGED, SKIPPED, TESTING, REJECTED
    }

    // Enum for Testset
    public enum Testset {
        SAMPLES, PRETESTS, TESTS, CHALLENGES, TESTS1, TESTS2, TESTS3, TESTS4, TESTS5,
        TESTS6, TESTS7, TESTS8, TESTS9, TESTS10
    }

    // Constructor
    public Submission(long id, Integer contestId, long creationTimeSeconds, int relativeTimeSeconds,
                      Problem problem, Party author, String programmingLanguage, Verdict verdict,
                      Testset testset, int passedTestCount, int timeConsumedMillis,
                      long memoryConsumedBytes, Float points) {
        this.id = id;
        this.contestId = contestId;
        this.creationTimeSeconds = creationTimeSeconds;
        this.relativeTimeSeconds = relativeTimeSeconds;
        this.problem = problem;
        this.author = author;
        this.programmingLanguage = programmingLanguage;
        this.verdict = verdict;
        this.testset = testset;
        this.passedTestCount = passedTestCount;
        this.timeConsumedMillis = timeConsumedMillis;
        this.memoryConsumedBytes = memoryConsumedBytes;
        this.points = points;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public void setCreationTimeSeconds(long creationTimeSeconds) {
        this.creationTimeSeconds = creationTimeSeconds;
    }

    public int getRelativeTimeSeconds() {
        return relativeTimeSeconds;
    }

    public void setRelativeTimeSeconds(int relativeTimeSeconds) {
        this.relativeTimeSeconds = relativeTimeSeconds;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Party getAuthor() {
        return author;
    }

    public void setAuthor(Party author) {
        this.author = author;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public Testset getTestset() {
        return testset;
    }

    public void setTestset(Testset testset) {
        this.testset = testset;
    }

    public int getPassedTestCount() {
        return passedTestCount;
    }

    public void setPassedTestCount(int passedTestCount) {
        this.passedTestCount = passedTestCount;
    }

    public int getTimeConsumedMillis() {
        return timeConsumedMillis;
    }

    public void setTimeConsumedMillis(int timeConsumedMillis) {
        this.timeConsumedMillis = timeConsumedMillis;
    }

    public long getMemoryConsumedBytes() {
        return memoryConsumedBytes;
    }

    public void setMemoryConsumedBytes(long memoryConsumedBytes) {
        this.memoryConsumedBytes = memoryConsumedBytes;
    }

    public Float getPoints() {
        return points;
    }

    public void setPoints(Float points) {
        this.points = points;
    }


}