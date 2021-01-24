package pl.edu.agh.mcc.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pl.edu.agh.mcc.InstrumentationData;
import pl.edu.agh.mcc.descriptors.DeviceInformation;
import pl.edu.agh.mcc.descriptors.cloud.CloudExecutionRatio;
import pl.edu.agh.mcc.descriptors.cloud.CloudMetrics;
import pl.edu.agh.mcc.descriptors.cloud.CloudResultInformation;
import pl.edu.agh.mcc.descriptors.cloud.SelectionHistory;

public class MatrixTaskRemote implements Runnable {
    private final int size;
    private final double[][] array;
    private final double coeff;
    private final InstrumentationData instrumentationDataObject;

    public MatrixTaskRemote(int matrixSize, InstrumentationData instrumentedEventData) {
        this.size = matrixSize;
        this.array = new double[size][size];
        this.coeff = new Random().nextDouble();
        instrumentationDataObject = instrumentedEventData;
        this.instrumentationDataObject.executionLocation = InstrumentationData.EXECUTION.CLOUD;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                array[i][j] = random.nextDouble() * coeff;
        this.sendRequest();
    }

    private class RequestClass {
        public double[][] matrix;
        public DeviceInformation deviceStatus;
        public List<SelectionHistory> selectionHistoryList;

        public RequestClass(double[][] array, DeviceInformation deviceStatus, List<SelectionHistory> selectionHistoryList) {
            this.matrix = array;
            this.deviceStatus = deviceStatus;
            this.selectionHistoryList = selectionHistoryList;
        }
    }


    public void sendRequest() {
        URL url = null;
        try {
            url = new URL("");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;

            http.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "");

            Gson gson = new GsonBuilder().create();

            // Here is the mock of the selectionHistory, the list should be real from the device
            SelectionHistory selectionHistory = new SelectionHistory(123123, "LTE", 123, InstrumentationData.EXECUTION.LOCAL);
            List<SelectionHistory> selectionHistoryList = Arrays.asList(selectionHistory, selectionHistory);

            String jsonBody = gson.toJson(new RequestClass(array, instrumentationDataObject.deviceInformation, selectionHistoryList));

            // Sending the response
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int respCode = ((HttpURLConnection) con).getResponseCode();
            System.out.println("Response code was : " + respCode);

            if (respCode != 200) {
                return;
            }

            // Read the response to the content variable
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            updateTheResults(content.toString(), gson);

            in.close();
            ((HttpURLConnection) con).disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTheResults(String content, Gson gson) {
        Map<String, Object> map = gson.fromJson(
                content, new TypeToken<Map<String, Object>>() {
                }.getType()
        );

        this.instrumentationDataObject.cloudResultInformation = new CloudResultInformation();

        this.instrumentationDataObject.cloudResultInformation.duration = (double) map.get("duration");
        Object metrics = map.get("metrics");
        Map<String, String> metricsMap;
        if (metrics != null) {
            metricsMap = convert(map.get("metrics").toString());
            this.instrumentationDataObject.cloudResultInformation.cloudMetrics = new CloudMetrics(
                    metricsMap.get("name"),
                    metricsMap.get("cpu"),
                    metricsMap.get("memory"),
                    metricsMap.get("window"),
                    metricsMap.get("timestamp"));
        }

        List<CloudExecutionRatio> cloudExecutionRatioList = (List<CloudExecutionRatio>) map.get("cloudExecutionRatioList");
        System.out.println("Execution ratios returned count: " + cloudExecutionRatioList.size());
    }

    public static Map<String, String> convert(String str) {
        str = str.substring(1, str.length() - 1);
        String[] tokens = str.split(", ");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < tokens.length; i++) {
            String current_keymap = tokens[i].replace(",", "").trim();
            String[] splitted = current_keymap.split("=", 2);
            map.put(splitted[0], splitted[1]);
        }
        return map;
    }
}
