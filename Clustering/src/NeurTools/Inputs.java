/*
 * Created by benoit.audigier on 6/20/2017 6:01 PM.
 */
package NeurTools;

import DataTool.Data;
import DataTool.ClusLogger;
import Settings.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Integer.parseInt;


public class Inputs implements Serializable {

    private Data data;
    private int size;
    private Input[] inputs;
    private int dataSize;

    // Constructs from data
    private Inputs(Data data) {
        this.data = data;
        this.dataSize = data.getNumberOfCategories();
        this.size = data.getNumberOfTrade() / 2;
        this.inputs = new Input[data.getTrades().length];
        data.randomize();
        //TODO TO IMPROVE
        for (int i = 0; i < inputs.length; i++) {
            try {
               inputs[i] = new Input(parseInt(data.getTrades()[i].getVersion1()[0]), data.getTrades()[i].getTradeNumber(), data.getTrades()[i].getValues());
//                inputs[i] = new Input(data.getTrades()[i].getVersion1()[0], data.getTrades()[i].getTradeNumber(), data.getTrades()[i].getValues());
            } catch (NumberFormatException e) {
                ClusLogger.getInstance().error("Impossible to parse to int trade number for input " + i + ". Please make sure the trade numbers are the first column.");
            }
        }
    }

    // Used for tests
    Inputs(double[][] weights) {
        inputs = new Input[weights.length];
        size = weights.length;
        for (int i = 0; i < weights.length; i++) {
            if (i == 0) dataSize = weights[i].length;
            Input input = new Input(i, i, weights[i]);
            inputs[i] = input;
        }

    }


    // Construct the inputs, trying to retrieve them from before if specified in the settings
    public static Inputs getInputs(Settings settings) {
        // If mentioned in settings, the program will try to retrieve the last processed inputs. if no/error, it starts from scratch (relaunches the function but changing settings).

/*        File directory = new File("results_cluster\\"); // to be sure the directory is created
        if (directory.mkdir()) { // If the folder is created
            Logger.getInstance().error("The folder results_cluster/ did not exists. It should now, please add the data file here and fill the dataFileName in the settings.");
        }*/

        Inputs inputs;
/*        String path = "results_cluster/" + settings.getDataFileName();*/
        String path = settings.getDataAbsolutePath();

        if (settings.isUseLastDataFile()) {
            try {
                inputs = retrieveInput(settings.getDataName());
                ClusLogger.getInstance().writeInLog("Success.");
                ClusLogger.getInstance().writeInLog("");
                ClusLogger.getInstance().writeInLog("");
            } catch (Exception e) {
                ClusLogger.getInstance().warning("Impossible to retrieve last treated data : " + e.getMessage());
                ClusLogger.getInstance().writeInLog("Starting from scratch.");
                settings.setUseLastDataFileToFalse();
                inputs = Inputs.getInputs(settings);
            }
        } else {
            inputs = new Inputs(new Data(path, settings.getAlgorithm(), settings.getIrrelevantColumns(), settings.getCsvSplitBy()));
            saveInputs(inputs, settings.getDataName()); // If starting from scratch, the inputs processed are stored for next time.
        }
        return inputs;
    }

    // Saving inputs allows to spare time the next times the algorithm is run on the same set of data
    private static void saveInputs(Inputs inputs, String fileName) {
        File directory = new File("results_cluster\\saved_trades\\"); // to be sure the directory is created
        if (directory.mkdir()) ClusLogger.getInstance().writeInLog("Creating folder results_cluster/saved_trades/.");

        Path path = Paths.get("results_cluster/saved_trades/" + fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            ClusLogger.getInstance().warning("Impossible to delete old saved processed data : " + e.getMessage());
        }
        try {
            ClusLogger.getInstance().writeInLog("Saving processed inputs ...");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("results_cluster/saved_trades/" + fileName + "saved"));
            out.writeObject(inputs);
            out.close();
            ClusLogger.getInstance().writeInLog("Done.");
            ClusLogger.getInstance().writeInLog("");
            ClusLogger.getInstance().writeInLog("");
        } catch (Exception e) {
            ClusLogger.getInstance().warning("Impossible to save treated data :" + e.getMessage());
        }
    }

    private static Inputs retrieveInput(String fileName) throws Exception {
        ClusLogger.getInstance().writeInLog("Trying to retrieve processed data ...");

        File directory = new File("results_cluster\\saved_trades\\"); // to be sure the directory is created
        if (directory.mkdir()) throw new Exception("The folder results_cluster/saved_trades/ did not exist.");

        // create an ObjectInputStream for the file we created before
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("results_cluster/saved_trades/" + fileName + "saved"));

        // read and print what we wrote before
        return (Inputs) ois.readObject();
    }


    // Randomizes the inputs
    void randomize() {
        Collections.shuffle(Arrays.asList(inputs));
    }


    // Getters and Setters

    public Data getData() {
        return data;
    }

    Input[] getInputs() {
        return inputs;
    }

    int getDataSize() {
        return dataSize;
    }

    int getSize() {
        return size;
    }


    @Override
    public String toString() {
        return "Inputs{" +
                "size=" + size +
                ", inputs=" + Arrays.toString(inputs) +
                '}';
    }


}
