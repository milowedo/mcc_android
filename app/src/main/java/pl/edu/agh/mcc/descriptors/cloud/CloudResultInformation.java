package pl.edu.agh.mcc.descriptors.cloud;

public class CloudResultInformation {
    public CloudMetrics cloudMetrics;
    public double duration;

    public CloudResultInformation(){
        cloudMetrics = new CloudMetrics();
    }
}
