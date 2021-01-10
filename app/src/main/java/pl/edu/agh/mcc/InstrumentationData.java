package pl.edu.agh.mcc;

import android.content.Context;
import android.os.Build;

import java.util.UUID;

import pl.edu.agh.mcc.descriptors.BatteryInformation;
import pl.edu.agh.mcc.descriptors.DeviceInformation;
import pl.edu.agh.mcc.descriptors.NetworkInformation;
import pl.edu.agh.mcc.descriptors.TaskInformation;
import pl.edu.agh.mcc.descriptors.TimeMeasurements;

// Should be stored in a local and remote database
public class InstrumentationData {
    public DeviceInformation deviceInformation;
    public BatteryInformation batteryInformation;
    public EXECUTION executionLocation;
    public NetworkInformation networkInformation;
    public TimeMeasurements timeMeasurements;
    public TaskInformation taskInformation;

    public InstrumentationData(Context context) {
        this.deviceInformation = new DeviceInformation();
        this.batteryInformation = new BatteryInformation();
        this.networkInformation = new NetworkInformation();
        this.timeMeasurements = new TimeMeasurements();
        this.taskInformation = new TaskInformation();

        this.networkInformation.updateInformation(context);

        // This should be stored somewhere on the device so we could get it every time the same
        this.deviceInformation.deviceId = UUID.randomUUID().toString();
        this.deviceInformation.deviceName = Build.MODEL;
    }

    public enum EXECUTION {
        LOCAL,
        CLOUD
    }
}
