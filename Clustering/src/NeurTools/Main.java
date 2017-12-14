/*
 * Created by benoit.audigier on 6/20/2017 5:18 PM.
 */

package NeurTools;

import DataTool.*;
import ExportTools.Exporter;
import Settings.Settings;

import java.io.*;

public class Main {

    public final static boolean PRINTINCONSOLE = true;
    public final static boolean LOGALL = false;

    public static void main(Settings settings) {

        // See the documentation for the storyline of the algorithm.

        // Initiating the Logger.getInstance() file, the folders, the readme
        initiateEnvironment();
        

        // Retrieving settings
//        Settings settings = new Settings();

        // Retrieving and processing data
        Inputs inputs = Inputs.getInputs(settings);
        

        // Treating the data with the algorithm
        Nodes ns = Algorithm.runAlgorithm(inputs, settings);

        // Exporting the data
        (new Exporter(inputs, ns, settings)).export();

        // Ending program
        ClusLogger.getInstance().endProg("No error encountered.");

    }

    // To create the folders deleted or for the first time the program is launched
    private static void createFolders() {
        File directory = new File("logs\\");
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating logs/ folder");
        directory = new File("results_cluster\\");
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating results_cluster/ folder");
        directory = new File("data_mismatch\\");
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating data_mismatch/ folder");
        directory = new File("data_cluster\\");
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating data_cluster/ folder");
        directory = new File("data_cluster\\saved_trades\\");
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating data_cluster/saved_trades folder");
//        directory = new File("results\\");
//        if (directory.mkdir()) Logger.getInstance().writeInLog("Creating results/ folder");
//        directory = new File("data\\");
//        if (directory.mkdir()) Logger.getInstance().writeInLog("Creating data/ folder");
//        directory = new File("data\\saved_trades\\");
//        if (directory.mkdir()) Logger.getInstance().writeInLog("Creating data/saved_trades folder");
    }

    // To create the readme
    private static void createReadMe() {
        try {
            PrintWriter readme = new PrintWriter(new BufferedWriter(new FileWriter("README.txt", false))); //false to init the file

            readme.println("- IMPORTANT : Every time you launch the algorithm, please check the file logs.log to be sure no errors or warnings have been raised.");
            readme.println("");
            readme.println("- A message box appears at the end of the program with information about the program ending.");
            readme.println("");
            readme.println("- Before launching the program, please read the first part of the documentation, \"How to use the algorithm\".");
            readme.println("");
            readme.println("- Do not forget to check the settings before you launch the algorithm, to be sure the parameters are correct.");
            readme.println("");
            readme.println("");
            readme.println("");

            readme.println("If you notice any bug or you have any ideas to improve the program, please contact the person in charge of the project.");

            readme.close();
        } catch (IOException e) {
            ClusLogger.getInstance().warning("Impossible to generate README.txt : " + e.getMessage());
        }
    }

    // To initiate everything
    private static void initiateEnvironment() {
        ClusLogger.getInstance().initLogFile();

        createFolders();
        createReadMe();
    }

}