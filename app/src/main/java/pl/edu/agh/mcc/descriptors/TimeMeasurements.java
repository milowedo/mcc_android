package pl.edu.agh.mcc.descriptors;

public class TimeMeasurements {
    public long startTime;
    public long endTime;
    public long totalTime;

    public void updateTotalTime() {
        this.totalTime = endTime-startTime;
    }

}
