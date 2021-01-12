package pl.edu.agh.mcc.descriptors.cloud;

public class CloudMetrics {
    public String name;
    public String cpu;
    public String memory;
    public String window;
    public String timestamp;

    public CloudMetrics(String name, String cpu, String memory, String timestamp, String window) {
        this.name = name;
        this.cpu = cpu;
        this.memory = memory;
        this.window = window;
        this.timestamp = timestamp;
    }

    public CloudMetrics() {
    }
}
