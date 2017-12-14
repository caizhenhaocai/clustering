package LogisticRegression;

import ExportTools.Exporter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Logistic {


    private double learningRate;
    private double[] weights;
    private int ITERATIONS = 3000;

    public Logistic(int n) {
        this.learningRate = 0.0001;
        weights = new double[n];
    }


    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public double[] train(List<Instance> instances, String exportFileName) {
        for (int n = 0; n < ITERATIONS; n++) {
            for (Instance instance : instances) {
                int[] x = instance.x;
                double predicted = classify(x);
                int label = instance.label;
                for (int j = 0; j < weights.length; j++) {
                    weights[j] = weights[j] + learningRate * (label - predicted) * x[j];
                }
            }
        }
        return weights;
    }

    private double classify(int[] x) {
        double logit = .0;
        for (int i = 0; i < weights.length; i++) {
            logit += weights[i] * x[i];
        }
        return sigmoid(logit);
    }

    public static class Instance {
        public int label;
        public int[] x;

        public Instance(int label, int[] x) {
            this.label = label;
            this.x = x;
        }
    }

    public static List<Instance> readDataSet(String file) throws FileNotFoundException {
        List<Instance> dataSet = new ArrayList<Instance>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] columns = line.split(",");

                int i = 1; // First column is trade number
                int[] data = new int[columns.length - 2];
                for (i = 1; i < columns.length - 1; i++) {
                    double tmp = Double.parseDouble(columns[i]);
                    data[i - 1] = (int) tmp;
                }
                int label = Integer.parseInt(columns[i]);
                Instance instance = new Instance(label, data);
                dataSet.add(instance);
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
        return dataSet;
    }


    public static void main(String... args) throws FileNotFoundException {

    }

}
