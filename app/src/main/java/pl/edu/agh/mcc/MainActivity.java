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

        Button startButton = findViewById(R.id.startTestButton);
        startButton.setText("Run task");
        startButton.setOnClickListener(v -> {
            int taskSize = 500;
            try {
                taskSize = Integer.parseInt(sizeInputField.getText().toString());
            } catch (Exception e) {
                System.out.println("Wrong input size, defaulting to: " + taskSize);
                this.sizeInputField.setText(String.valueOf(taskSize));
            }
            InstrumentationData results = executeTaskAndGatherMetrics(taskSize);

            this.resultText.setText(gson.toJson(results));
        });
    }

    private InstrumentationData executeTaskAndGatherMetrics(int matrixSize) {
        InstrumentationData instrumentedEventData = new InstrumentationData(this);

        Thread task = new Thread(new MatrixTask(matrixSize));
        instrumentedEventData.taskInformation.taskName = "MatrixTask";
        instrumentedEventData.taskInformation.taskSize = matrixSize;

        instrumentedEventData.batteryInformation.startValueUpdate(batteryStatus);
        instrumentedEventData.executionLocation = InstrumentationData.EXECUTION.LOCAL;
        instrumentedEventData.timeMeasurements.startTime = System.currentTimeMillis();

        try {
            task.start();
            task.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instrumentedEventData.timeMeasurements.endTime = System.currentTimeMillis();
        instrumentedEventData.batteryInformation.endValueUpdate(batteryStatus);

        return instrumentedEventData;
    }

}