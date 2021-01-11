package pl.edu.agh.mcc.descriptors;

public class DeviceInformation {
    public String deviceName;
    public String deviceId;

    //Cannot be sure the number returned is the actual number of all the processors on the device.
    public final int numAvailableProcessors = Runtime.getRuntime().availableProcessors();

    //Maximum amount the JVM will try to use, if not specified - Long.MAX_VALUE
    public final long maxMemoryBytes = Runtime.getRuntime().maxMemory();

    //Amount of memory in the JVM.
    public final long totalMemoryBytes = Runtime.getRuntime().totalMemory();
}
