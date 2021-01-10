package pl.edu.agh.mcc.tasks;

import java.util.Random;

import static pl.edu.agh.mcc.MatrixOperations.invert;

public class MatrixTaskLocal implements Runnable {
    private final int size;
    private final double[][] array;
    private final double coeff;

    public MatrixTaskLocal(int matrixSize) {
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

        invert(array);
    }
}
