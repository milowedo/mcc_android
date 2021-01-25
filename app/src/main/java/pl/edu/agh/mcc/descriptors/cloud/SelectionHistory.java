package pl.edu.agh.mcc.descriptors.cloud;

import pl.edu.agh.mcc.InstrumentationData;

public class SelectionHistory {

    public SelectionHistory(String timestamp, String networkType, int taskSize, InstrumentationData.EXECUTION executionLocation) {
        this.timestamp = timestamp;
        this.networkType = networkType;
        this.taskSize = taskSize;
        this.executionLocation = executionLocation;
    }

    public String timestamp;
    public String networkType;
    public int taskSize;
    public InstrumentationData.EXECUTION executionLocation;


}
