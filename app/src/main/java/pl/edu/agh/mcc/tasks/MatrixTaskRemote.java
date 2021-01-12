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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import pl.edu.agh.mcc.InstrumentationData;
import pl.edu.agh.mcc.descriptors.cloud.CloudMetrics;
import pl.edu.agh.mcc.descriptors.cloud.CloudResultInformation;

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


    public void sendRequest() {
        URL url = null;
        try {
            url = new URL("http://66.66.666.666:8080/invert");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;

            http.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "put some here :)");

            Gson gson = new GsonBuilder().create();
            String jsonBody = "{\"matrix\": " + gson.toJson(array) + "}";
            System.out.println("mil");
            System.out.println(jsonBody);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            System.out.println("Response status: " + ((HttpURLConnection) con).getResponseMessage());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            System.out.println("Response from " + url.getPath() + ": " + content);

            Map<String, Object> map = gson.fromJson(
                    content.toString(), new TypeToken<Map<String, Object>>() {
                    }.getType()
            );

            this.instrumentationDataObject.cloudResultInformation = new CloudResultInformation();

            this.instrumentationDataObject.cloudResultInformation.duration = (double) map.get("duration");
            Map<String, String> metrics = convert(map.get("metrics").toString());

            this.instrumentationDataObject.cloudResultInformation.cloudMetrics = new CloudMetrics(
                    metrics.get("name"),
                    metrics.get("cpu"),
                    metrics.get("memory"),
                    metrics.get("window"),
                    metrics.get("timestamp"));

            in.close();
            ((HttpURLConnection) con).disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
