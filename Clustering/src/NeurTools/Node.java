/*
 * Created by benoit.audigier on 6/20/2017 5:12 PM.
 */
package NeurTools;


import java.io.Serializable;
import java.util.Vector;

import DataTool.ClusLogger;

public abstract class Node implements Serializable {
    private int id;
    protected Vector<Input> elements;
    int size;
    private Node cluster;
    private final double vigilanceRate;
    double[] centroid;
    private boolean frozen;


    Node(int id) {
        this.vigilanceRate = 0;
        this.id = id;
        elements = new Vector<>();
        size = 0;
        frozen = false; // This is for training; if a node is created after training, then it is correct; its weights won't change, hence the frozen.
    }

    Node(int id, double vigilanceRate, boolean frozen) {
        this.id = id;
        this.size = 0;
        this.elements = new Vector<>(200);
        this.vigilanceRate = vigilanceRate;
        this.frozen = frozen;
    }

    // ALL THE ABSTRACT FUNCTION DIFFERS IN THEIR IMPLEMENTATION FOR EACH ALGORITHM


    // Calculates the affinity of the node for an input
    protected abstract double calcAffinity(double[] x);

    // For the retroaction
    protected abstract double calcQuality(Input t) throws NullPointerException;
    // for the retroaction for node as input
    protected abstract double calcQualitynode(Node t) throws NullPointerException;
    // Operator AND ^
    protected abstract double[] AND(double[] a, double[] b);

    abstract void setWeights(double[] w);

    abstract void updateWeights(double[] x);

    // The most important is getWeights()[0] whatever the algorithm (Art1 has several weights)
    public abstract double[][] getWeights();
    //test if the second layer nodes can be added into the third layer nodes
    boolean tryAdding(Node t) throws NullPointerException { // returns false if the vigilance criterion is not verified.
        if (t == null) {
            ClusLogger.getInstance().warning("Attempt to insert null trade into node " + id + ".");
            return false;
        } else {
            if (calcVerification(t)) { // If the input fits enough
                for(Input input:t.getElements()) {
                	input.setCluster(this);
                	elements.add(input);
                }
                if (Main.LOGALL) ClusLogger.getInstance().writeInLog(t.getId() + " added to " + id);
                update(t.getWeights()[0]);
                size=size+t.getElements().size();
                return true;
            } else return false;
        }
    }
    // Important function, try to add an input and rejects it if the vigilance criterion is not validated.
    boolean tryAdding(Input t) throws NullPointerException { // returns false if the vigilance criterion is not verified.
        if (t == null) {
            ClusLogger.getInstance().warning("Attempt to insert null trade into node " + id + ".");
            return false;
        } else {
            if (onlyZeros(t)) {
                ClusLogger.getInstance().warning("Attempt to add a 0-trade (number " + t.getId() + " in node " + id + ".");
                return true;
            }
            if (calcVerification(t)) { // If the input fits enough
                Node oldNode = t.getCluster();
                if (oldNode != null) {
                    if (oldNode.equals(this)) {
                        t.setJustChanged(false);
                        return true;
                    } else {
                        oldNode.remove(t);
                    }
                }
                t.setJustChanged(true);
                t.setCluster(this);
                elements.add(t);
                if (Main.LOGALL) ClusLogger.getInstance().writeInLog(t.getId() + " added to " + id);
                update(t.getValues());
                size++;
                return true;
            } else return false;
        }
    }

    // Update the node after an input is added
    void update(double[] x) {
        if (!frozen) updateWeights(x);
    }

    // To force add
    void add(Input t) {
        boolean isIn = false;
        for (Input input :
                elements) {
            if (input.getId() == t.getId())
                isIn = true;
        }
        if (!isIn) {
            elements.add(t);
            size++;
        }
    }

    static double norm(double[] a) {
        double res = 0;
        for (double val :
                a) {
            res += val;
        }
        return res;
    }

    static double[] sum(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i] + b[i];
        }
        return res;
    }

    // absSous(a, b) = |a-b|
    static double[] absSous(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = Math.abs(a[i] - b[i]);
        }
        return res;
    }

    static double[] mult(double k, double[] a) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i] * k;
        }
        return res;
    }

    static double[] scalarProduct(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = a[i] * b[i];
        }
        return res;
    }

    private boolean onlyZeros(Input t) {
        boolean allZeros = true;
        int i = 0;
        while (allZeros && i < t.getValues().length) {
            if (t.getValues()[i] != 0) allZeros = false;
            i++;
        }
        return allZeros;
    }

    void remove(Input input) {
        elements.remove(input);
        size--;
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog(input.getId() + " removed from " + id);
    }

    void printNodeInLog() {
        ClusLogger.getInstance().writeInLog(size + " trades.");
        StringBuilder line = new StringBuilder();
        for (Input t :
                elements) {
            line.append(t.getId()).append(", ");
        }
        ClusLogger.getInstance().writeInLog(line.toString());
        ClusLogger.getInstance().writeInLog("");
    }

    // To compare with other nodes
    public double similarityPercentage(Node otherNode) {

        double res = 0;
        for (Input input : elements) {
            for (Input input2 : otherNode.getElements()) {
                if (input.getId() == input2.getId()) {
                    res++;
                }
            }
        }


        if (size == 0) {
            ClusLogger.getInstance().warning("node number " + id + " is empty");
            return 0;
        } else return (100 * res / size);
    }
    //to set up a nodes to nodes vigilance criterion check function
    private boolean calcVerification(Node t) throws NullPointerException {
        double quality = calcQualitynode(t);
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Vigilance criterion check : " + quality);
        return (vigilanceRate < quality);
    }

    void updateCentroid() {
        centroid = this.centroid();
    }

    // To check if an input fits enough regarding vigilance criterion
    private boolean calcVerification(Input t) throws NullPointerException {
        double quality = calcQuality(t);
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Vigilance criterion check : " + quality);
        return (vigilanceRate < quality);
    }



    public double[] centroid() {
        double[] centroid = new double[getWeights()[0].length];
        for (Input input :
                elements) {
            for (int j = 0; j < centroid.length; j++) {
                centroid[j] += input.getValues()[j];
            }
        }
        for (int j = 0; j < centroid.length; j++) {
            centroid[j] = centroid[j] / size;
        }
        return centroid;
    }

    // Distance on binary vectors, worth mentioning but not relevant on anything else than binary data (hence no possibility to use centroids)
    static double jaccardDistance(double[] a, double[] b) {
        double M01_10 = 0;
        double M11 = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == b[i]) {
                if (a[i] > 0) M11++;
            } else if (a[i] == 0 || b[i] == 0) {
                M01_10++;
            } else M11 += 1 - Math.abs(a[i] - b[i]);
        }
        return (M01_10 / (M01_10 + M11));
    }

    private static double euclideanDistance(double[] a, double[] b) {
        double res = 0;
        for (int i = 0; i < a.length; i++) {
            res += Math.pow(Math.abs(b[i] - a[i]), 2);
        }
        return Math.pow(res, .5);
    }

    public static double distance(double[] a, double[] b) {
//        return jaccardDistance(a, b);
        return euclideanDistance(a, b);
    }

    public double variance() {
        double res = 0;
        double[] centroid = centroid();
        for (Input i :
                elements) {
            res += Math.pow(distance(centroid, i.getValues()), 2);
        }
        res = res / size;
        return res;
    }

    private double dissimilarTo(Input input) {
        double res = 10000000;
        for (Input i :
                elements) {
            res = Math.min(res, distance(input.getValues(), i.getValues()));
        }
        return res;
    }

    private double averageDissimilarity(Input input) {
        double res = 0;
        for (Input i :
                elements) {
            if (!i.equals(input)) res += distance(input.getValues(), i.getValues());
        }
        return res;
    }


    // Getters and Setters

    public int getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public Vector<Input> getElements() {
        return elements;
    }

    public void setId(int id) {
        this.id = id;
    }
    public Node getCluster() {
    	return cluster;
    }
    public void setCluster(Node cluster) {
    	this.cluster=cluster;
    }
}
