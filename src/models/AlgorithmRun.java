package models;

/**
 * AlgorithmRun entity model.
 * Represents one benchmark execution recorded in the algorithm_runs table.
 */
public class AlgorithmRun {

    private int runId;
    private String algorithmName;
    private int inputSize;
    private long timeNs;
    private long memoryKb;
    private String dateRun;

    // Empty constructor
    public AlgorithmRun() {}

    // Fully parameterized constructor
    public AlgorithmRun(int runId, String algorithmName, int inputSize,
                        long timeNs, long memoryKb, String dateRun) {
        this.runId = runId;
        this.algorithmName = algorithmName;
        this.inputSize = inputSize;
        this.timeNs = timeNs;
        this.memoryKb = memoryKb;
        this.dateRun = dateRun;
    }

    // Getters
    public int getRunId() {
        return runId;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getInputSize() {
        return inputSize;
    }

    public long getTimeNs() {
        return timeNs;
    }

    public long getMemoryKb() {
        return memoryKb;
    }

    public String getDateRun() {
        return dateRun;
    }

    // Setters
    public void setRunId(int runId) {
        this.runId = runId;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public void setTimeNs(long timeNs) {
        this.timeNs = timeNs;
    }

    public void setMemoryKb(long memoryKb) {
        this.memoryKb = memoryKb;
    }

    public void setDateRun(String dateRun) {
        this.dateRun = dateRun;
    }

    @Override
    public String toString() {
        return "AlgorithmRun{" +
                "runId=" + runId +
                ", algorithmName='" + algorithmName + '\'' +
                ", inputSize=" + inputSize +
                ", timeNs=" + timeNs +
                ", memoryKb=" + memoryKb +
                ", dateRun='" + dateRun + '\'' +
                '}';
    }
}