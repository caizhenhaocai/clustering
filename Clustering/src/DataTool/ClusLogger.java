package DataTool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import NeurTools.Input;
import NeurTools.Node;
import NeurTools.Nodes;
import logger.impl.ClusterLogger;

public class ClusLogger extends ClusterLogger {

	private static ClusLogger instance;
	
	private ClusLogger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static ClusLogger getInstance()
	{
		if (instance == null)
			instance = new ClusLogger();
		return instance;
	}
	
    // Flash in the evolution file, which is the clusters at the iteration i
    public static void flashNodes(Nodes nodes, int iteration) {
        try {
            PrintWriter tmp = new PrintWriter(new BufferedWriter(new FileWriter(evolutionFileName, true)));
            tmp.println();
            tmp.println("Iteration " + iteration);


            StringBuilder s;
            for (Node n :
                    nodes.getNodes()) {
                s = new StringBuilder();
                s.append(n.getId()).append(",");
                for (Input i :
                        n.getElements()) {
                    s.append(i.getId()).append(",");
                }
                tmp.println(s);
            }

            tmp.println();

            tmp.close();


        } catch (IOException e) {
            ClusLogger.getInstance().writeInLog("Warning : " + e.getMessage());
        }

    }
}
