package pl.edu.agh.mcc.descriptors.cloud;

public class CloudExecutionRatio {
    public CloudExecutionRatio(String networkType, int taskSizeRangeMin, int taskSizeRangeMax, double cloudExecutionRatio) {
        this.networkType = networkType;
        this.taskSizeRangeMin = taskSizeRangeMin;
        this.taskSizeRangeMax = taskSizeRangeMax;
        this.cloudExecutionRatio = cloudExecutionRatio;
    }

    public String networkType;
    public int taskSizeRangeMin;
    public int taskSizeRangeMax;
    public double cloudExecutionRatio;
}
