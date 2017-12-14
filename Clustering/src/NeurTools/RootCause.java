/*
 * Created by benoit.audigier on 8/2/2017 5:09 PM.
 */
package NeurTools;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import DataTool.ClusLogger;

import static java.lang.Integer.parseInt;

public abstract class RootCause {

    // For the debug, to compare the clusters obtained to truly labeled data.
    public static Nodes getRootCauses(String fileName, boolean separateClusters) { // separatedClusters is to treat the case where a trade is in several cluster. If true, the trades in several clusters are joined together in another cluster.
        String separated;
        if (separateClusters) separated = "separated";
        else separated = "not separated";
        ClusLogger.getInstance().writeInLog("Retrieving " + separated + " root causes inputs ...");
        String[] rootCauses = getRootCausesNames(fileName);
        if (rootCauses == null) {
            ClusLogger.getInstance().warning("Error while trying to retrieve the root causes");
            return null;
        }

        // To retrieve the root cause name and associate an ID
//        HashMap<String, Integer> rcIds = new HashMap<>(20, 1);


        // A trade may have several root causes
        HashMap<Integer, HashMap<String, String>> tradesWithRC = new HashMap<>(2000);
        try {
        	/**
        	 * author: Thanh Nguyen
        	 * edit file name input
        	 */
//            BufferedReader br = new BufferedReader(new FileReader("data/" + fileName));
        	BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            br.readLine();
            int lineCounter = 1;
            while ((line = br.readLine()) != null) {
                String[] val = line.split(",");
                int t;
                try {
                    t = parseInt(val[0]);
                    if (tradesWithRC.containsKey(t)) {
                        HashMap<String, String> rcAssociated = tradesWithRC.get(t);
                        rcAssociated.put(val[1], val[1]);
                        tradesWithRC.put(t, rcAssociated);
                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(val[1], val[1]);
                        tradesWithRC.put(t, map);
                    }
                } catch (NumberFormatException e) {
                    ClusLogger.getInstance().warning("Data corrupted line " + lineCounter + ", " + val[0] + " cannot be parsed as Int.");
                }
                lineCounter++;
            }
        } catch (Exception e) {
            ClusLogger.getInstance().warning("Encountered problem while reading Root Cause File : " + e.getMessage());
        }

        if (separateClusters) {
            HashMap<String, String> newRCs = new HashMap<>(30);
            for (Map.Entry<Integer, HashMap<String, String>> trade : tradesWithRC.entrySet()) {
                HashMap<String, String> val = trade.getValue();
                StringBuilder rcAssociated = new StringBuilder();
                for (Map.Entry<String, String> rc : val.entrySet()) {
                    if (rcAssociated.toString().equals("")) rcAssociated = new StringBuilder(rc.getValue());
                    else rcAssociated.append("-").append(rc.getValue());
                }
                newRCs.put(rcAssociated.toString(), rcAssociated.toString());
                HashMap<String, String> newRCMap = new HashMap<>(1);
                newRCMap.put(rcAssociated.toString(), rcAssociated.toString());
                tradesWithRC.put(trade.getKey(), newRCMap);
            }
            rootCauses = new String[newRCs.size()];
            int i = 0;
            for (Map.Entry<String, String> newRC : newRCs.entrySet()) {
                rootCauses[i] = newRC.getValue();
                i++;
            }
        }


        RootCauseNodes nodes = new RootCauseNodes(rootCauses);

        // Creating artificially a node for each root cause
        for (int i = 0; i < nodes.getRootCauses().length; i++) {
            Node n = new KMeansNode(i, false); // because node is abstract
            nodes.addNode(n);
        }

        HashMap<String, Integer> rcIds = new HashMap<>(20);
          for (int i = 0; i < rootCauses.length; i++) {
            rcIds.put(rootCauses[i], i);
        }


        for (Map.Entry<Integer, HashMap<String, String>> trade : tradesWithRC.entrySet()) {
            for (Map.Entry<String, String> categories : trade.getValue().entrySet()) {
                nodes.getNodes().get(rcIds.get(categories.getValue())).add(new Input(trade.getKey()));
            }
        }

        ClusLogger.getInstance().writeInLog("Done.");


        return nodes;
    }

    private static String[] getRootCausesNames(String fileName) {
        try {
            String[] res;
            /**
             * author: Thanh Nguyen
             * fix file input problem
             */
//            BufferedReader br = new BufferedReader(new FileReader("data/" + fileName));
            HashMap<String, String> rootCauses = new HashMap<>(40);
            String previousRC = "";
            int lineCounter = 1;
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            br.readLine(); // first line is irrelevant
            while ((line = br.readLine()) != null) {
                String[] val = line.split(",");
                if (val.length != 2)
                    throw new Exception("Root Cause file corrupted line " + lineCounter + " : length of the line is " + val.length + " instead of 2 (id, root cause).");
                if (!previousRC.equals(val[1])) {
                    rootCauses.put(val[1], val[1]);
                    previousRC = val[1];
                }
                lineCounter++;
            }

            res = new String[rootCauses.size()];
            int i = 0;
            for (Map.Entry<String, String> rootCause : rootCauses.entrySet()) {
                res[i] = rootCause.getKey();
                i++;
            }

            return res;
        } catch (Exception e) {
            ClusLogger.getInstance().warning("Encountered problem while reading the root cause file to search for Root Cause names : " + e.getMessage());
            return null;
        }
    }

}
