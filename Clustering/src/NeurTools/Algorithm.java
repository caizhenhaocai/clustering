/*
 * Created by benoit.audigier on 6/30/2017 12:21 PM.
 */
package NeurTools;

import ExportTools.Exporter;
import LogisticRegression.Logistic;
import Settings.Settings;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

import DataTool.ClusLogger;


public abstract class Algorithm {

    // Sorts by value the indexes; for example, [.5, .3, .7] will come out as [2,0,1]
    public static <T extends Comparable<T>> int[] argSort(T[] tab) {
        if (tab == null) {
            ClusLogger.getInstance().warning("Attempt to sort a null array.");
            return null;
        } else if (tab.length == 0) {
            ClusLogger.getInstance().warning("Attempt to sort an empty int array.");
            return new int[]{};
        } else if (tab.length == 1) {
            return new int[]{0};
        } else {
            T[] copy = tab.clone();
            int[] indexes = new int[copy.length];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = i;
            }
            for (int i = copy.length - 1; i >= 0; i--) {
                T tmp = copy[i];
                int tmpIndex = indexes[i];
                for (int j = i + 1; j <= copy.length - 1 && tmp.compareTo(copy[j]) < 0; j++) {
                    copy[j - 1] = copy[j];
                    copy[j] = tmp;
                    indexes[j - 1] = indexes[j];
                    indexes[j] = tmpIndex;
                }
            }
            return indexes;
        }
    }

    // Returns the order the nodes are classified by, regarding the affinity of the nodes for the input considered
    private static int[] argmaxAffinity(Input x, Nodes nodes) {
        if (nodes.getNumberOfClusters() == 0) {
            return new int[]{};
        }
        Double[] tab = new Double[nodes.getNumberOfClusters()];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = nodes.getNodes().elementAt(i).calcAffinity(x.getValues());
        }
        return argSort(tab);
    }
    //Returns the order of the nodes(used in the third layer)
    private static int[] argmaxAffinity(Node x, Nodes nodes) {
        if (nodes.getNumberOfClusters() == 0) {
            return new int[]{};
        }
        Double[] tab = new Double[nodes.getNumberOfClusters()];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = nodes.getNodes().elementAt(i).calcAffinity(x.getWeights()[0]);
        }
        return argSort(tab);
    }
    
    //to deal with the nodes from second layer and check if they can fit in the 
    private static void treatnode(Node node, Nodes nodes2, PossibleAlgorithms algorithm, double vigilanceRate) {
        int[] affinityOrder = argmaxAffinity(node, nodes2);
        boolean accepted = false;
        for (int i = 0; i < affinityOrder.length && !accepted; i++) {
            try {
                accepted = nodes2.getNodes().elementAt(affinityOrder[i]).tryAdding(node);
                if (accepted && Main.LOGALL) ClusLogger.getInstance().writeInLog("Accepted in node " + affinityOrder[i]);
            } catch (NullPointerException e) {
                //ClusLogger.getInstance().warning("Empty trade, number " + input.getId() + ".");
                accepted = true;
            }
        }
        if (!accepted) {
            if (algorithm != PossibleAlgorithms.KMEANS) {
                if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Not accepted; creating new node.");
                nodes2.addNode2(node, vigilanceRate);
                //input.setJustChanged(true);
            } else if (Main.LOGALL) ClusLogger.getInstance().warning("trying to add a new node in k-means algorithm");
        }
    }
    // Treat the input considered: trying each node classified by affinity, and creating new node if no match
    private static void treatInput(Input input, Nodes nodes, PossibleAlgorithms algorithm, double vigilanceRate) {
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog("");
        if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Treating trade number " + input.getId());
        int[] affinityOrder = argmaxAffinity(input, nodes);
        boolean accepted = false;
        for (int i = 0; i < affinityOrder.length && !accepted; i++) {
            if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Trying node " + affinityOrder[i]);
            try {
                accepted = nodes.getNodes().elementAt(affinityOrder[i]).tryAdding(input);
                if (accepted && Main.LOGALL) ClusLogger.getInstance().writeInLog("Accepted in node " + affinityOrder[i]);
            } catch (NullPointerException e) {
                ClusLogger.getInstance().warning("Empty trade, number " + input.getId() + ".");
                accepted = true;
            }
        }
        if (!accepted) {
            if (algorithm != PossibleAlgorithms.KMEANS) {
                if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Not accepted; creating new node.");
                nodes.addNode(input, vigilanceRate);
                input.setJustChanged(true);
            } else if (Main.LOGALL) ClusLogger.getInstance().warning("trying to add a new node in k-means algorithm");
        }
    }

    // Initialization, changes following the algorithm and the training (optional)
    private static Nodes initializeNode(Inputs inputs, Settings settings) {
        PossibleAlgorithms algorithm = settings.getAlgorithm();
        if (algorithm == PossibleAlgorithms.FUZZYART || algorithm == PossibleAlgorithms.ART1) { // NOTE : here is to be changed if there is another algorithm added
            Nodes nodes = new Nodes(algorithm);
            if (settings.isTrain()) {
                boolean correctTraining = nodes.train(inputs, settings);
                if (!correctTraining) {
                    settings.setTrainToFalse();
                    return initializeNode(inputs, settings);
                }
            } else {
                Node n;
                if (algorithm == PossibleAlgorithms.FUZZYART)
                    n = new FuzzyArtNode(0, inputs.getDataSize(), settings.getVigilanceCriterion(), false);
                else n = new Art1Node(0, inputs.getDataSize(), settings.getVigilanceCriterion(), false);
                nodes.addNode(n);
            }
            return nodes;
        } else if (algorithm == PossibleAlgorithms.KMEANS) return new Nodes(algorithm);
        return null;
    }
    private static Nodes initializeNode1(Inputs inputs, Settings settings) {
        PossibleAlgorithms algorithm = settings.getAlgorithm();
        if (algorithm == PossibleAlgorithms.FUZZYART || algorithm == PossibleAlgorithms.ART1) { // NOTE : here is to be changed if there is another algorithm added
            Nodes nodes = new Nodes(algorithm);
            if (settings.isTrain()) {
                boolean correctTraining = nodes.train(inputs, settings);
                if (!correctTraining) {
                    settings.setTrainToFalse();
                    return initializeNode(inputs, settings);
                }
            } else {
                Node n;
                if (algorithm == PossibleAlgorithms.FUZZYART)
                    n = new FuzzyArtNode(0, inputs.getInputs()[0], settings.getVigilanceCriterion(), false);
                else n = new Art1Node(0, inputs.getDataSize(), settings.getVigilanceCriterion(), false);
                nodes.addNode(n);
            }
            return nodes;
        } else if (algorithm == PossibleAlgorithms.KMEANS) return new Nodes(algorithm);
        return null;
    }
    private static Nodes initializeNode2(Node node, Settings settings) {
        PossibleAlgorithms algorithm = settings.getAlgorithm();
        Nodes nodes = new Nodes(algorithm);
        Node n;
        n = new FuzzyArtNode(0,node , settings.getVigilanceCriterion(), false);
        nodes.addNode(n);
        return nodes;
    }
    // Reinitialize the value justChanged and cluster in nodes, in case another algorithm is run on the same set of input later.
    private static void reinitializeInputs(Inputs inputs) {
        for (Input input :
                inputs.getInputs()) {
            input.setJustChanged(true);
            input.setCluster(null);
        }
    }

    // Main function;
    public static Nodes runAlgorithm(Inputs inputs, Settings settings) {
        // The idea is to treat all the input and check if there has been any change; if so, we do it again, until stability is reached.

        long start = System.currentTimeMillis();

        int numberOfSecurityIteration = 4;

        ClusLogger.getInstance().writeInLog("Beginning data treatment with " + settings.getAlgorithm() + ((int) (settings.getVigilanceCriterion() * 10)) + " ...");

        // NOTE : here needs to be changed if another algorithm is added
        Nodes nodes = initializeNode1(inputs, settings); // One empty cluster with weights initialized according to algorithm, or several frozen nodes if trained.
        if (nodes == null) {
            ClusLogger.getInstance().error("Algorithm not detected, nodes not initialized.");
        }
        boolean stabilized = false;


        int iterations = 0;
        int stabilization = 0;

        //Beginning of the iterations
        //while (!stabilized || stabilization < numberOfSecurityIteration) {
            //stabilized = true;
            //ClusLogger.getInstance().flashNodes(nodes, iterations); // for the logs, to follow the evolution of the clusters (mostly for debug)
            
//            if ((iterations - numberOfSecurityIteration) == 1) nodes.printClustersInLog();
            
            //if (Main.LOGALL) ClusLogger.getInstance().writeInLog("Iteration number " + (iterations + 1));

            // Treat all the inputs
            //for (Input input : inputs.getInputs()) {
               // treatInput(input, nodes, settings.getAlgorithm(), settings.getVigilanceCriterion());
               // if (input.isJustChanged()) stabilized = false;
            //}
            //if (settings.getAlgorithm() == PossibleAlgorithms.KMEANS) {
                //for (Node n : nodes.getNodes()) {
                   // n.updateCentroid();
                //}
            //}
            //inputs.randomize(); // Randomization for more difficulty to reach stability


            // Check of stability
            //if (stabilized) {
                //stabilization++;
            //} else {
                //stabilization = 0;
            //}
            //iterations++;
           // FuzzyArtNode.setlearningrate(0.6);

       //}
        //first layer, treat the input
        for (Input input : inputs.getInputs()) {
            if(input!=inputs.getInputs()[0]) {
        	treatInput(input, nodes, settings.getAlgorithm(), settings.getVigilanceCriterion());
        }
        }
        //second layer, get the cluster result nodes
        nodes.removeEmptyClusters(); // It happens that cluster are created then emptied; no need to keep them.
        
        FuzzyArtNode.setlearningrate(0.8);//reset the learning rate
        //third layer
        Nodes nodes2=initializeNode2(nodes.getNodes().elementAt(0), settings);
        for(Node node2: nodes.getNodes()) {
        	if (node2!=nodes.getNodes().elementAt(0)) {
        	treatnode(node2,nodes2,settings.getAlgorithm(),0.9);
        }
        }
        //fourth layer
        FuzzyArtNode.setlearningrate(0.8);//reset the learning rate
        Nodes nodes3=initializeNode2(nodes2.getNodes().elementAt(0), settings);
        for(Node node3: nodes2.getNodes()) {
        	if (node3!=nodes2.getNodes().elementAt(0)) {
        	treatnode(node3,nodes3,settings.getAlgorithm(),0.9);
        }
        }
        //fifth layer
        FuzzyArtNode.setlearningrate(0.8);//reset the learning rate
        Nodes nodes4=initializeNode2(nodes3.getNodes().elementAt(0), settings);
        for(Node node4: nodes3.getNodes()) {
        	if (node4!=nodes3.getNodes().elementAt(0)) {
        	treatnode(node4,nodes4,settings.getAlgorithm(),0.8);
        }
        }
//        Nodes nodes5=initializeNode2(nodes4.getNodes().elementAt(0), settings);
//        for(Node node5: nodes4.getNodes()) {
//        	if (node5!=nodes4.getNodes().elementAt(0)) {
//        	treatnode(node5,nodes5,settings.getAlgorithm(),0.8);
//        }
//        }
//        Nodes nodes6=initializeNode2(nodes5.getNodes().elementAt(0), settings);
//        for(Node node6: nodes5.getNodes()) {
//        	if (node6!=nodes5.getNodes().elementAt(0)) {
//        	treatnode(node6,nodes6,settings.getAlgorithm(),0.9);
//        }
//        }
//        Nodes nodes7=initializeNode2(nodes6.getNodes().elementAt(0), settings);
//        for(Node node7: nodes6.getNodes()) {
//        	if (node7!=nodes6.getNodes().elementAt(0)) {
//        	treatnode(node7,nodes7,settings.getAlgorithm(),0.8);
//        }
//        }
        if (Main.LOGALL) nodes.printClustersInLog();

        if (settings.isMerger())
            nodes = mergeClusters(inputs, nodes, settings.getMergerRate()); // If merger in settings

        ClusLogger.getInstance().writeInLog("Finished.");
        ClusLogger.getInstance().writeInLog("Number of effective iterations : " + (iterations - numberOfSecurityIteration));
        ClusLogger.getInstance().writeInLog("Time of execution : " + ClusLogger.getInstance().toTime(System.currentTimeMillis() - start));
        ClusLogger.getInstance().writeInLog("");
        ClusLogger.getInstance().writeInLog("");

        reinitializeInputs(inputs); // In case another algorithm is run on the same set of inputs.

        if (Main.LOGALL) nodes.printClustersInLog();       
        
        //Write the most correct evolution
        ClusLogger.getInstance().flashNodes(nodes, iterations+1);
        //nodes.printClustersInLog();
        
        return nodes4 ;
    }


	// Function to merge the clusters
    private static Nodes mergeClusters(Inputs inputs, Nodes nodes, double threshold) {
        int numberOfCluster = nodes.getNumberOfClusters();


        // Retrieving LR weights
        ClusLogger.getInstance().writeInLog("Calculating regression ...");
        Pair<double[], int[]> LR = getLogisticRegression(inputs, nodes);
        double[] weights;
        if (LR == null) {
            ClusLogger.getInstance().warning("Logical regression weights null. Merging aborted.");
            return nodes;
        } else weights = LR.getKey();
        nodes.setRegressionInformation(LR);


        //Building closeness data
        ClusLogger.getInstance().writeInLog("Merging ...");
        double[] closeness = new double[numberOfCluster * numberOfCluster];
        double closenessMax = 0;
        for (int i = 0; i < closeness.length; i++) {
            int cluster1 = i / numberOfCluster;
            int cluster2 = i % numberOfCluster;
            if (cluster1 < cluster2) {
                closeness[i] = Node.norm(Node.scalarProduct(Node.absSous(nodes.getNodes().get(cluster1).getWeights()[0], nodes.getNodes().get(cluster2).getWeights()[0]), weights)) / nodes.getNodes().get(cluster1).getWeights()[0].length;
            	//closeness[i] = calcAffinity(nodes.getNodes().get(cluster1).getWeights()[0], nodes.getNodes().get(cluster2).getWeights()[0],weights);
                if (closeness[i] > closenessMax) closenessMax = closeness[i];
            } else closeness[i] = 100000;
        }


        // Normalizing
        for (int i = 0; i < closeness.length; i++) {
            if (closeness[i] != 100000) closeness[i] = closeness[i] / closenessMax;
        }


        // Generating file of closeness, mostly for debug, not useful here.

//        ArrayList<StringBuilder> content = new ArrayList<>(400);
//        StringBuilder lineToWrite = new StringBuilder(",Size");
//        for (int i = 0; i < numberOfCluster; i++) {
//            lineToWrite.append(",").append(nodes.getNodes().get(i).getSize());
//        }
//        content.add(lineToWrite);
//        lineToWrite = new StringBuilder("Size,Cluster ID");
//        for (int i = 0; i < numberOfCluster; i++) {
//            lineToWrite.append(",").append(i);
//        }
//
//
//        for (int i = 0; i < closeness.length; i++) {
//            int line = i / numberOfCluster;
//            int column = i % numberOfCluster;
//            if (column == 0) {
//                content.add(lineToWrite);
//                lineToWrite = new StringBuilder(nodes.getNodes().get(line).getSize() + "," + line + "");
//            }
//            lineToWrite.append(",").append(closeness[i]);
//        }
//        try {
//            File directory = new File("results\\"); // to be sure the directory is created
//            if (directory.mkdir()) Logger.getInstance().writeInLog("Folder results/ created");
//            Exporter.generateFileInResults("results/Closeness RL.csv", content);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        HashMap<Integer, Vector<Integer>> newClusters = new HashMap<>(50);
        // Constructing the new clusters first of all with the ids
        for (int i = 0; i < closeness.length; i++) {
        	if (closeness[i] < threshold) {
            //if (closeness[i] > threshold) {
                int line = i / numberOfCluster;
                int column = i % numberOfCluster;
                updateClusters(newClusters, Math.min(line, column), Math.max(line, column));
            }
        }
        finalUpdateCluster(newClusters, numberOfCluster);

        return new Nodes(nodes, newClusters);// The construction of the final clusters are in this constructor
    }

    // Used during the merger of clusters
    private static void updateClusters(HashMap<Integer, Vector<Integer>> newCluster, Integer cluster1, Integer cluster2) {
        // The idea is to update the list the list of the new cluster in a clever way to be sure not to miss any case and not ending up with in an incorrect set of clusters :
        // 0:0,1,4 ; 2:2,3,4 is not a correct set, it should be 0:1,2,3,4.

        // we assume here that line < column
        // the key is the cluster with the lowest index
        if (newCluster.containsKey(cluster1)) { // if the cluster 1 is already a key
            if (!newCluster.get(cluster1).contains(cluster2)) {
                newCluster.get(cluster1).add(cluster2);
            }
        } else if (newCluster.containsKey(cluster2)) { // the same for cluster 2
            Vector<Integer> clusterList = newCluster.get(cluster2);
            if (!clusterList.contains(cluster1)) {
                clusterList.add(cluster1);
            }
            newCluster.put(cluster1, clusterList);
        } else { // checking that they are not a value
            boolean added = false;
            for (Map.Entry<Integer, Vector<Integer>> cluster : newCluster.entrySet()) {
                if (cluster.getValue().contains(cluster1)) {
                    if (!cluster.getValue().contains(cluster2)) {
                        cluster.getValue().add(cluster2);
                    }
                    added = true;
                } else if (cluster.getValue().contains(cluster2)) {
                    if (!cluster.getValue().contains(cluster1)) {
                        cluster.getValue().add(cluster1);
                    }
                    added = true;
                }
            }
            if (!added) {
                Vector<Integer> newList = new Vector<>();
                newList.add(cluster1);
                newList.add(cluster2);
                newCluster.put(cluster1, newList);
            }
        }
        // still a case not covered : 1 : 1, 5 and 2 : 2, 3 and then appears 3-5 : we will have 1 : 1,3,5 and 2 : 2,3 instead of 1 : 1,2,3,5.
        // This is why the finalUpdate function is mandatory
    }

    // Used during the merger of clusters
    private static void finalUpdateCluster(HashMap<Integer, Vector<Integer>> newCluster, int numberOfCluster) {
        // taking care of the case not covered by the regular update : 1 : 1, 5 and 2 : 2, 3 and then appears 3-5 : we will have 1 : 1,3,5 and 2 : 2,3 instead of 1 : 1,2,3,5.
        // basically merging clusters of clusters that have ids in common, reiterating until a stable state is reached.
        int toMerge1 = -1;
        int toMerge2 = -1;
        boolean merged = false; // This is not to modify the lists inside the 'for' to avoid concurrence issues, the merged is done afterwards and the function called again if a merged has been detected.
        for (Map.Entry<Integer, Vector<Integer>> cluster1 : newCluster.entrySet()) {
            Vector<Integer> list1 = newCluster.get(cluster1.getKey());
            for (Map.Entry<Integer, Vector<Integer>> cluster2 : newCluster.entrySet()) {

                if (cluster1.equals(cluster2)) {
                    continue;
                }
                Vector<Integer> list2 = newCluster.get(cluster2.getKey());
                for (int i = 0; i < list2.size() && !merged; i++) {
                    Integer considered = list2.elementAt(i);
                    if (list1.contains(considered)) {
                        merged = true;
                        toMerge1 = cluster1.getKey();
                        toMerge2 = cluster2.getKey();
                        break; // an outerloop could be used
                    }
                }
                if (merged) break;
            }
            if (merged) break;
        }

        if (merged) {
            mergeTwoClusters(newCluster, toMerge1, toMerge2);
            finalUpdateCluster(newCluster, numberOfCluster);
        } else {
            // Adding the clusters that have not been merged at all
            for (int i = 0; i < numberOfCluster; i++) {
                if (!isMentioned(newCluster, i)) {
                    Vector<Integer> newList = new Vector<>();
                    newList.add(i);
                    newCluster.put(i, newList);
                }
            }
        }
    }

    private static boolean isMentioned(HashMap<Integer, Vector<Integer>> newCluster, Integer i) {
        for (Vector<Integer> list : newCluster.values()) {
            for (Integer j : list) {
                if (j.equals(i)) return true;
            }
        }
        return false;
    }

    // Used during the merger of clusters
    private static void mergeTwoClusters(HashMap<Integer, Vector<Integer>> newCluster, Integer c1, Integer c2) {
        Vector<Integer> list1 = newCluster.remove(c1);
        Vector<Integer> list2 = newCluster.remove(c2);
        for (Integer i : list2) if (!list1.contains(i)) list1.add(i);
        newCluster.put(Math.min(c1, c2), list1);
    }

    // Used for debug in mergeCLuster
    private static void printClusters(HashMap<Integer, Vector<Integer>> newCluster) {
        for (Map.Entry<Integer, Vector<Integer>> entry : newCluster.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
    
    static double[] AND(double[] a, double[] b){
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            if(a[i]<b[i]) res[i] = a[i];
            else res[i] = b[i];
        }
		return res;
    }

	static double norm(double[] a) {
        double res = 0;
        for (double val :
                a) {
            res += val;
        }
        return res;
    }
    static double calcAffinity(double[] x, double[] y, double[] weight){
        return norm(Node.scalarProduct(AND(x, y),weight)) / (norm(Node.scalarProduct(weight,x)));
    }


    // Calculates the logistic regression and the number of 1 per column (easy to retrieve this data here)
    public static Pair<double[], int[]> getLogisticRegression(Inputs inputs, Nodes ns) {
        ArrayList<StringBuilder> treatedTradesInNodes = new ArrayList<>(5000);

        // Generating file to be read by the Logistic Regression
        // Can be improved by keeping this data locally
        int[] sum = new int[ns.getNodes().firstElement().getElements().firstElement().getValues().length];
        StringBuilder line;
        for (Node n : ns.getNodes()) {
            for (Input t : n.getElements()) {
                line = new StringBuilder();
                line.append(t.getId());
                int i = 0;
                for (double d : t.getValues()) {
                    sum[i] += (int) d;
                    line.append(",").append(d);
                    i++;
                }
                line.append(",").append(n.getId());
                treatedTradesInNodes.add(line);
            }
        }
        try {
            Exporter.generateFileInResults("results_cluster/saved_trades/tmp.csv", treatedTradesInNodes);
        } catch (Exception e) {
            ClusLogger.getInstance().warning("Encountered problem while exporting temporary file : " + e.getMessage());
            return null;
        }

        List<Logistic.Instance> instances;
        try {
            instances = Logistic.readDataSet("results_cluster/saved_trades/tmp.csv");
        } catch (FileNotFoundException e) {
            ClusLogger.getInstance().warning("Encountered problem while reading temporary file : " + e.getMessage());
            return null;
        }

        Logistic logistic = new Logistic(inputs.getDataSize());
        double[] weights = logistic.train(instances, "Weights of regression");
        if (weights == null) return null;

        return new Pair<>(weights, sum);
    }

}
