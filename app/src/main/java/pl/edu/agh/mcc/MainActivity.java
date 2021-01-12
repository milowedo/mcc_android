package pl.edu.agh.mcc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pl.edu.agh.mcc.tasks.MatrixTaskLocal;
import pl.edu.agh.mcc.tasks.MatrixTaskRemote;

public class MainActivity extends AppCompatActivity {
    private TextView resultText;
    private EditText sizeInputField;
    private Intent batteryStatus;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        this.resultText = findViewById(R.id.resultText);

        this.sizeInputField = findViewById(R.id.sizeInputField);

        Button startLocalButton = findViewById(R.id.startLocalTestButton);
        startLocalButton.setText("Run local");
        startLocalButton.setOnClickListener(v -> {
            int taskSize = 200;
            try {
                taskSize = Integer.parseInt(sizeInputField.getText().toString());
            } catch (Exception e) {
                System.out.println("Wrong input size, defaulting to: " + taskSize);
                this.sizeInputField.setText(String.valueOf(taskSize));
            }
            InstrumentationData results = executeTaskAndGatherMetrics(taskSize, InstrumentationData.EXECUTION.LOCAL);

            this.resultText.setText(gson.toJson(results));
        });

        Button startRemoteButton = findViewById(R.id.startRemoteTestButton);
        startRemoteButton.setText("Run cloud");
        startRemoteButton.setOnClickListener(v -> {
            int taskSize = 200;
            try {
                taskSize = Integer.parseInt(sizeInputField.getText().toString());
            } catch (Exception e) {
                System.out.println("Wrong input size, defaulting to: " + taskSize);
                this.sizeInputField.setText(String.valueOf(taskSize));
            }
            InstrumentationData results = executeTaskAndGatherMetrics(taskSize, InstrumentationData.EXECUTION.CLOUD);

            this.resultText.setText(gson.toJson(results));
        });
    }

    private InstrumentationData executeTaskAndGatherMetrics(int matrixSize, InstrumentationData.EXECUTION execution) {
        InstrumentationData instrumentedEventData = new InstrumentationData(this);
        Thread task;
        if (execution == InstrumentationData.EXECUTION.CLOUD) {
            task = new Thread(new MatrixTaskRemote(matrixSize, instrumentedEventData));
        } else {
            task = new Thread(new MatrixTaskLocal(matrixSize, instrumentedEventData));
        }
        instrumentedEventData.taskInformation.taskName = "MatrixTask";
        instrumentedEventData.taskInformation.taskSize = matrixSize;

        instrumentedEventData.batteryInformation.startValueUpdate(batteryStatus, this);
        instrumentedEventData.timeMeasurements.startTime = System.currentTimeMillis();

        try {
            task.start();
            task.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instrumentedEventData.timeMeasurements.endTime = System.currentTimeMillis();
        instrumentedEventData.timeMeasurements.updateTotalTime();
        instrumentedEventData.batteryInformation.endValueUpdate(batteryStatus, this);

        return instrumentedEventData;
    }

}