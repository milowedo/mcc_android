package pl.edu.agh.mcc.ML;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pl.edu.agh.mcc.InstrumentationData;
import pl.edu.agh.mcc.tasks.MatrixTaskLocal;
import pl.edu.agh.mcc.tasks.MatrixTaskRemote;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LocationExecutionDecider {
    private List connectionTypeValues;
    private List executionLocationValues;
    private Intent batteryStatus;
    private Context context;
    private Instances timePredictionSet;
    private Instances batteryPredictionSet;
    private Classifier timeClassifier;
    private Classifier batteryClassifier;

    public LocationExecutionDecider(Intent batteryStatus, Context context) {
        this.batteryStatus = batteryStatus;
        this.context = context;
        executionLocationValues = new ArrayList(2);
        executionLocationValues.add(InstrumentationData.EXECUTION.CLOUD.name());
        executionLocationValues.add(InstrumentationData.EXECUTION.LOCAL.name());

        connectionTypeValues = new ArrayList(7);
        connectionTypeValues.add("WIFI");
        connectionTypeValues.add("MOBILE");
        connectionTypeValues.add("NOT_CONNECTED");
        connectionTypeValues.add("BLUETOOTH");
        connectionTypeValues.add("ETHERNET");
        connectionTypeValues.add("VPN");
        connectionTypeValues.add("WIMAX");

        timeClassifier = new MultilayerPerceptron();
        batteryClassifier = new MultilayerPerceptron();
    }

    private InstrumentationData executeTaskAndGatherMetrics(int matrixSize, InstrumentationData.EXECUTION execution) {
        InstrumentationData instrumentedEventData = new InstrumentationData(context);
        Thread task;
        if (execution == InstrumentationData.EXECUTION.CLOUD) {
            task = new Thread(new MatrixTaskRemote(matrixSize, instrumentedEventData));
        } else {
            task = new Thread(new MatrixTaskLocal(matrixSize, instrumentedEventData));
        }
        instrumentedEventData.taskInformation.taskName = "MatrixTask";
        instrumentedEventData.taskInformation.taskSize = matrixSize;

        instrumentedEventData.batteryInformation.startValueUpdate(batteryStatus, context);
        instrumentedEventData.timeMeasurements.startTime = System.currentTimeMillis();

        try {
            task.start();
            task.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instrumentedEventData.timeMeasurements.endTime = System.currentTimeMillis();
        instrumentedEventData.timeMeasurements.updateTotalTime();
        instrumentedEventData.batteryInformation.endValueUpdate(batteryStatus, context);

        return instrumentedEventData;
    }

    private List<InstrumentationData> generateTrainData() {
        List<InstrumentationData> trainData = new ArrayList<>();
        Random rand = new Random();
        //10 init iterations for random data
        for (int i=0; i<10; i++) {
            int randMatrixSize = rand.nextInt(1000); //size??
            int pick = rand.nextInt(InstrumentationData.EXECUTION.values().length);
            InstrumentationData.EXECUTION randExecutionLocation = InstrumentationData.EXECUTION.values()[pick];
            InstrumentationData instrumentationData = executeTaskAndGatherMetrics(randMatrixSize, randExecutionLocation);
            trainData.add(instrumentationData);
        }
        return trainData;
    }

    //dataset for time prediction
    private Instances getDatasetForTimePrediction(List<InstrumentationData> data) {

        // 1. set up attributes
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("taskSize"));
        attributes.add(new Attribute("executionLocation", executionLocationValues));
        attributes.add(new Attribute("connectionType", connectionTypeValues));
        attributes.add(new Attribute("totalTime"));
        // 2. create Instances object
        Instances dataSet = new Instances("execution time prediction", attributes, 0);
        for (InstrumentationData example : data){
            double[] values = new double[dataSet.numAttributes()];
            values[0] = example.taskInformation.taskSize;
            values[1] = executionLocationValues.indexOf(example.executionLocation.name());
            values[2] = connectionTypeValues.indexOf(example.networkInformation.connectionType);
            values[3] = example.timeMeasurements.totalTime;
            dataSet.add(new DenseInstance(1.0, values));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }
    //dataset for time prediction
    private Instances getDatasetForBatteryPrediction(List<InstrumentationData> data) {

        // 1. set up attributes
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("taskSize"));
        attributes.add(new Attribute("executionLocation", executionLocationValues));
        attributes.add(new Attribute("connectionType", connectionTypeValues));
        attributes.add(new Attribute("totalBatteryConsumption"));
        // 2. create Instances object
        Instances dataSet = new Instances("execution time prediction", attributes, 0);
        for (InstrumentationData example : data){
            double[] values = new double[dataSet.numAttributes()];
            values[0] = example.taskInformation.taskSize;
            values[1] = executionLocationValues.indexOf(example.executionLocation.name());
            values[2] = connectionTypeValues.indexOf(example.networkInformation.connectionType);
            values[3] = example.batteryInformation.batteryCapacityDelta;
            dataSet.add(new DenseInstance(1.0, values));
        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        return dataSet;
    }

    public InstrumentationData chooseExecutionLocation(int matrixSize) throws Exception {

        if (timePredictionSet == null) {
            //wygenerowac pliki arff, jeden an pdostawie baterii drugi na podstawie pomiarow
            List<InstrumentationData> instrumentationDataSet = generateTrainData();
            timePredictionSet = getDatasetForTimePrediction(instrumentationDataSet); //trzymac to zamiast examples
            batteryPredictionSet= getDatasetForBatteryPrediction(instrumentationDataSet); //trzymac to zamiast examples
            //System.out.println(trainData);
            // wytrenowac dwa modele na tym klasyfikatorze
            timeClassifier.buildClassifier(timePredictionSet);
            batteryClassifier.buildClassifier(batteryPredictionSet);

        }
        InstrumentationData instrumentedEventData = new InstrumentationData(context);

        double[] values = new double[4];
        values[0] = 200;
        values[1] = executionLocationValues.indexOf(InstrumentationData.EXECUTION.LOCAL.name());
        values[2] = connectionTypeValues.indexOf(instrumentedEventData.networkInformation.connectionType);
        Instance i = new DenseInstance(1.0, values);
        i.setDataset(timePredictionSet);
        //spr przewidywanie dla jednej lokalizacji, dla drugiej
        //tam gdzie min funkcja wybierz wykonanie; jesli dwa modele to bierzemy srednia wazona z ich wynikow
        double timeForLocal=timeClassifier.classifyInstance(i);
        values[1] = executionLocationValues.indexOf(InstrumentationData.EXECUTION.CLOUD.name());
        double timeForCloud=timeClassifier.classifyInstance(i);

        i.setDataset(batteryPredictionSet);
        double batteryForCloud=batteryClassifier.classifyInstance(i);
        values[1] = executionLocationValues.indexOf(InstrumentationData.EXECUTION.LOCAL.name());
        double batteryForLocal= batteryClassifier.classifyInstance(i);
        double predictedCostForCloud = (timeForCloud*0.5 + batteryForCloud*0.5)/2;
        double predictedCostForLocal = (timeForLocal*0.5 + batteryForLocal*0.5)/2;
        InstrumentationData instrumentationData;
        if (timeForLocal > predictedCostForCloud) {
            instrumentationData = executeTaskAndGatherMetrics(matrixSize, InstrumentationData.EXECUTION.CLOUD);
            //instrumentationData = executeTaskAndGatherMetrics(100, InstrumentationData.EXECUTION.LOCAL);
            values[3] = timeForCloud;
            timePredictionSet.add(i);
            values[3] = batteryForCloud;
            batteryPredictionSet.add(i);
        }
        else {
            values[1] = executionLocationValues.indexOf(InstrumentationData.EXECUTION.LOCAL.name());
            instrumentationData = executeTaskAndGatherMetrics(matrixSize, InstrumentationData.EXECUTION.LOCAL);
            values[3] = timeForLocal;
            timePredictionSet.add(i);
            values[3] = batteryForLocal;
            batteryPredictionSet.add(i);
        }
        timeClassifier.buildClassifier(timePredictionSet);
        batteryClassifier.buildClassifier(batteryPredictionSet);
        return instrumentationData;
    }



}
