package pl.edu.agh.mcc.descriptors;

import android.content.Intent;
import android.os.BatteryManager;

public class BatteryInformation {
    public double batteryPercentStart;
    public double batteryPercentEnd;


    public void startValueUpdate(Intent batteryStatus) {
        this.batteryPercentStart = getBatteryPercent(batteryStatus);
    }

    public void endValueUpdate(Intent batteryStatus) {
        this.batteryPercentEnd = getBatteryPercent(batteryStatus);
    }

    private int getBatteryPercent(Intent batteryStatus) {
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

}
