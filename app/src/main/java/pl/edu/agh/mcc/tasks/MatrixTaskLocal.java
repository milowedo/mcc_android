package pl.edu.agh.mcc.tasks;

import java.util.Random;

import pl.edu.agh.mcc.InstrumentationData;

import static pl.edu.agh.mcc.MatrixOperations.invert;

public class MatrixTaskLocal implements Runnable {
    private final int size;
    private final double[][] array;
    private final double coeff;
    private final InstrumentationData instrumentationDataObject;

    public MatrixTaskLocal(int matrixSize, InstrumentationData instrumentedEventData) {
        this.size = matrixSize;
        this.array = new double[size][size];
        this.coeff = new Random().nextDouble();
        this.instrumentationDataObject = instrumentedEventData;
        this.instrumentationDataObject.executionLocation = InstrumentationData.EXECUTION.LOCAL;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                array[i][j] = random.nextDouble() * coeff;

        invert(array);
    }
}
