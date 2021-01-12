package pl.edu.agh.mcc.descriptors;

import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DeviceInformation {
    public String deviceName;
    public String deviceId;

    //Cannot be sure the number returned is the actual number of all the processors on the device.
    private String numAvailableProcessors;

    //Maximum amount the JVM will try to use, if not specified - Long.MAX_VALUE
    public final long maxMemoryMBytes = (long) (Runtime.getRuntime().maxMemory() * 1e-6);

    //Amount of memory in the JVM.
    public final long totalMemoryMBytes = (long) (Runtime.getRuntime().totalMemory() * 1e-6);

    public String platform_chipset;
    public String platform_os;

    //TODO: NOT IMPLEMENTED ~ 600mhz ARM 11?
    private String platform_cpu;

    //TODO: https://stackoverflow.com/questions/15804365/is-there-any-way-to-get-gpu-information ~ Adreno 200
    private String platform_gpu;

    public DeviceInformation() {
        Map<String, String> cpuInfo = new HashMap<>();
        try {
            cpuInfo = this.getCPUInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.platform_chipset = cpuInfo.get("Hardware");
        this.numAvailableProcessors = cpuInfo.get("CPU_architecture");
        this.platform_os = this.getOsInfo();

    }

    private Map<String, String> getCPUInfo() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
        String str;
        Map<String, String> output = new HashMap<>();
        while ((str = br.readLine()) != null) {
            String[] data = str.split(":");
            if (data.length > 1) {
                String key = data[0].trim().replace(" ", "_");
                if (key.equals("model_name")) key = "cpu_model";
                output.put(key, data[1].trim());
            }
        }
        br.close();
        return output;
    }

    private String getOsInfo() {
        Field[] fields = Build.VERSION_CODES.class.getFields();
        StringBuilder codeName = new StringBuilder();
        for (Field field : fields) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    codeName.append("Android ").append(field.getName());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return codeName.toString();
    }
}
