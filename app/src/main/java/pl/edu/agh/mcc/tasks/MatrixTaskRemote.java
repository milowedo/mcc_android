package pl.edu.agh.mcc.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class MatrixTaskRemote implements Runnable {
    private final int size;
    private final double[][] array;
    private final double coeff;

    public MatrixTaskRemote(int matrixSize) {
        this.size = matrixSize;
        this.array = new double[size][size];
        this.coeff = new Random().nextDouble();
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

            try(OutputStream os = con.getOutputStream()) {
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
            in.close();
            ((HttpURLConnection) con).disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
