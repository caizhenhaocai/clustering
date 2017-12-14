/*
 * Created by benoit.audigier on 6/21/2017 10:15 AM.
 */

package NeurTools;

import Settings.Settings;
import DataTool.ClusLogger;
import javafx.util.Pair;

import java.io.*;
import java.util.*;


public class Nodes implements Serializable {
    private Vector<Node> nodes;
    private int numberOfClusters;
    private PossibleAlgorithms algorithm;
    private Pair<double[], int[]> regressionInformation;


    Nodes(PossibleAlgorithms algorithm) {
        this.nodes = new Vector<>(128, 64);
        this.numberOfClusters = 0;
        this.algorithm = algorithm;
    }

    // Constructor used for the merger, by gathering clusters as described is newNodes
    Nodes(Nodes oldNodes, HashMap<Integer, Vector<Integer>> newNodes) {

        nodes = new Vector<>();
        numberOfClusters = newNodes.size();
        algorithm = oldNodes.getAlgorithm();
        regressionInformation = oldNodes.getRegressionInformation();

        int counter = 0;
        for (Map.Entry<Integer, Vector<Integer>> newCluster : newNodes.entrySet()) {
            Vector<double[]> weights = new Vector<>();
            Node n;
            if (oldNodes.getAlgorithm() == PossibleAlgorithms.FUZZYART) n = new FuzzyArtNode(counter);
            else if (oldNodes.getAlgorithm() == PossibleAlgorithms.ART1) n = new Art1Node(counter);
            else {
                ClusLogger.getInstance().error("Merger is not possible for KMEANS algorithm");
                n = new KMeansNode(counter, false); // to avoid error n might not have been initialized
            }

            for (Integer i : newCluster.getValue()) {
                // adding the inputs of each nodes present in the list to the new node
                for (Input input : oldNodes.getNodes().get(i).getElements()) {
                    n.add(input);

                }
                weights.add(oldNodes.getNodes().get(i).getWeights()[0]);
            }

            n.setWeights(updateWeightsForMerger(weights));

            nodes.add(n);

            counter++;
        }
    }


    // Average of all the merged clusters into the same bigger cluster
    private double[] updateWeightsForMerger(Vector<double[]> weights) {
        double[] res = new double[weights.firstElement().length];
        for (double[] w : weights) {
            res = Node.sum(res, w);
        }
        res = Node.mult(1 / weights.size(), res);
        return res;
    }

    void addNode(Input input, double vigilanceRate) {
        Node newNode;
        if (algorithm == PossibleAlgorithms.FUZZYART) { // NOTE : add here another case if new algorithm
            newNode = new FuzzyArtNode(numberOfClusters, input, vigilanceRate, false);
        } else if (algorithm == PossibleAlgorithms.ART1) {
            newNode = new Art1Node(numberOfClusters, input, vigilanceRate, false);
        } else {
            ClusLogger.getInstance().error("Algorithm not detected in new node creation");
            newNode = null;
        }
        nodes.add(numberOfClusters, newNode);
        Node oldNode = input.getCluster();
        if (oldNode != null) oldNode.remove(input);
        input.setCluster(newNode);
        if (Main.LOGALL)
            ClusLogger.getInstance().writeInLog("Creating new node " + newNode.getId() + "which contains " + newNode.toString());
        numberOfClusters++;
    	}
    void addNode2(Node node, double vigilanceRate) {
        Node newNode;
        if (algorithm == PossibleAlgorithms.FUZZYART) { // NOTE : add here another case if new algorithm
            newNode = new FuzzyArtNode(numberOfClusters, node, vigilanceRate, false);
        }
        else {
        ClusLogger.getInstance().error("Algorithm not detected in new node creation");
        newNode = null;
        }
        nodes.add(numberOfClusters, newNode);
        numberOfClusters++;
    }

    void addNode(Node node) {
        nodes.add(numberOfClusters, node);
        numberOfClusters++;
    }

    public void printClustersInLog() {
        ClusLogger.getInstance().writeInLog("");
        ClusLogger.getInstance().writeInLog("Number of clusters : " + numberOfClusters + ".");

        int i = 1;
        for (Node item :
                nodes) {
            ClusLogger.getInstance().writeInLog("Cluster number " + i);
            item.printNodeInLog();
            i++;
        }
    }

    // After an algorithm is run, there may be some empty clusters
    void removeEmptyClusters() {
        int[] toRemove = new int[numberOfClusters];
        Arrays.fill(toRemove, -1);
        int i = 0;
        for (Node n : nodes) {
            if (n.getSize() == 0) {
                toRemove[i] = n.getId();
                i++;
            }
        }
        ClusLogger.getInstance().writeInLog("Removing empty clusters ...");
        if (toRemove[0] == -1) ClusLogger.getInstance().writeInLog("No empty clusters.");

        for (int j = numberOfClusters - 1; j >= 0; j--) {
            if (toRemove[j] != -1) {
                if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Removing " + toRemove[j]);
                nodes.remove(toRemove[j]);
                for (int k = toRemove[j]; k < numberOfClusters - 1; k++) {
                    int id = nodes.get(k).getId();
                    nodes.get(k).setId(id - 1);
                }
                numberOfClusters--;
            }
        }
    }

    // Train with labelised data
    boolean train(Inputs inputs, Settings settings) {
        ClusLogger.getInstance().writeInLog("Training ...");
        if (settings.getAlgorithm() == PossibleAlgorithms.KMEANS) {
            ClusLogger.getInstance().warning("Trying to train a K-Means");
            return false;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("data/" + settings.getTrainingFileName()));
            br.readLine(); // headers
            String line;
            HashMap<Integer, String> labels = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                int tnb = Integer.parseInt(split[0]);
                String label = split[1];
                labels.put(tnb, label);
            }
            train(inputs, labels, settings.getAlgorithm(), settings.getVigilanceCriterion());
        } catch (Exception e) {
            ClusLogger.getInstance().warning("While training : " + e.getMessage());
            return false;
        }
        ClusLogger.getInstance().writeInLog("Done.");
        return true;
    }

    private void train(Inputs inputs, HashMap<Integer, String> labels, PossibleAlgorithms algorithm, double vigilanceRate) {
        nodes = new Vector<>(10);
        ArrayList<String> seenLabels = new ArrayList<>(15);
        HashMap<String, Integer> nodeAssociatedToLabel = new HashMap<>();
        for (Input input : inputs.getInputs()) {
            if (labels.get(input.getId()) != null) {
                if (!seenLabels.contains(labels.get(input.getId()))) {
                    seenLabels.add(labels.get(input.getId()));
                    Node newNode;
                    if (algorithm == PossibleAlgorithms.FUZZYART)
                        newNode = new FuzzyArtNode(numberOfClusters, input, vigilanceRate, true);
                    else if (algorithm == PossibleAlgorithms.ART1)
                        newNode = new Art1Node(numberOfClusters, input, vigilanceRate, true);
                    else {
                        ClusLogger.getInstance().warning("Algorithm not detected for training");
                        continue;
                    }
                    nodeAssociatedToLabel.put(labels.get(input.getId()), numberOfClusters);
                    nodes.add(newNode);
                    numberOfClusters++;
                } else {
                    nodes.get(nodeAssociatedToLabel.get(labels.get(input.getId()))).add(input);
                    nodes.get(nodeAssociatedToLabel.get(labels.get(input.getId()))).update(input.getValues());
                }
                input.setCluster(nodes.get(nodeAssociatedToLabel.get(labels.get(input.getId()))));
            }
        }
    }


    // Getters and setters

    public Vector<Node> getNodes() {
        return nodes;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public Pair<double[], int[]> getRegressionInformation() {
        return regressionInformation;
    }

    void setRegressionInformation(Pair<double[], int[]> regressionInformation) {
        this.regressionInformation = regressionInformation;
    }

    public void updateNumberOfCluster() {
        numberOfClusters = nodes.size();
    }

    public PossibleAlgorithms getAlgorithm() {
        return algorithm;
    }

    @Override
    public String toString() {
        return "Nodes{" +
                "nodes=" + nodes +
                ", numberOfClusters=" + numberOfClusters +
                '}';
    }
}
