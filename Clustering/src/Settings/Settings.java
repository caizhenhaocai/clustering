/*
 * Created by benoit.audigier on 8/2/2017 5:43 PM.
 */
package Settings;

import NeurTools.PossibleAlgorithms;
import DataTool.ClusLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings {
    private String settingsFileName = "settings.txt";

    // data
    private String dataFileName;
    private String dataName;
    private String dataAbsolutePath;
    private boolean useLastDataFile = true;

    // algorithm
    private PossibleAlgorithms algorithm;
    private double vigilanceCriterion;
    private boolean merger = false;
    private double mergerRate = 0.1;
    private boolean train = false;
    private String trainingFileName;
    private String irrelevantColumns = "_ID|_REF|LineNumber|_NB|KEY|Key|SourceName|TIMESTAMP";
    private String csvSplitBy = ",";

    //export
    private boolean exportTrades = true;
    private boolean exportWeights = true;
    private boolean exportLRWeights = true;
    private boolean exportVariances = false;
    private boolean exportDistances = false;

    private boolean exportCompareToRC = false;
    private String RCFile;

    // local tools
    private String[] allSettings = new String[]{"dataFileName", "dataName", "useLastDataFile", "algorithm", "vigilanceCriterion", "merger", "mergerRate", "train", "trainingFileName", "irrelevantColumns", "exportCompareToRC", "RCFile", "exportTrades", "exportWeights", "exportLRWeights", "exportVariances", "exportDistances"};
    private ArrayList<String> notTreated;

    public Settings() {

        // For the first time the program is launched
        File f = new File(settingsFileName);
        if (!(f.exists() && !f.isDirectory())) reinitializeSettingsFile();

        // If you want to force the reinitialization of the settings, uncomment the next line
//        reinitializeSettingsFile();

        notTreated = new ArrayList<>();
        // The elements are removed from notTreated one by one. The remaining will be assigned their default value (a warning is written in the logs).
        notTreated.addAll(Arrays.asList(allSettings));

        readSettingsFile();

        if (dataFileName != null) {
            dataName = dataFileName.split(".csv")[0]; // the fileName contains at most one ".", for ".csv".
            notTreated.remove("dataName");
        }
        for (String setting : notTreated)
            ClusLogger.getInstance().warning("Value of " + setting + " not read in the setting file, using default value.");
        if (notTreated.size() != 0)
            ClusLogger.getInstance().writeInLog("You might want to regenerate the settings file (#regenerate:YES).");
    }

    public Settings(String dataAbsolutePath2, String dataFileName2, PossibleAlgorithms algorithm2, double vigilanceCriterion2, double mergerRate2,
			String trainingFileName2, String irrelevantColumns2, String rCFile2, boolean useLastDataFile2,
			boolean merger2, boolean train2, boolean exportCompareToRC2, boolean exportTrades2, boolean exportWeights2,
			boolean exportLRWeights2, boolean exportVariances2, boolean exportDistances2, String CSVSplitBy) {
    	
    	dataAbsolutePath = dataAbsolutePath2;
    	dataFileName = dataFileName2;
    	algorithm = algorithm2;
        vigilanceCriterion = vigilanceCriterion2;
        mergerRate = mergerRate2;
        trainingFileName = trainingFileName2;
        irrelevantColumns = irrelevantColumns2;
        RCFile = rCFile2;
//		useLastDataFile;	
        useLastDataFile = useLastDataFile2;
//		regenerateSettings
//		merger
        merger = merger2;
//		train
        train = train2;
//		exportCompareToRC
        exportCompareToRC = exportCompareToRC2;
//      exportTrades
        exportTrades = exportTrades2;
//      exportWeights
        exportWeights = exportWeights2;
//      exportLRWeights
        exportLRWeights = exportLRWeights2;
//      exportVariances
        exportVariances = exportVariances2;
//      exportDistances
        exportDistances = exportDistances2;     
        
        this.csvSplitBy = CSVSplitBy;
    	
        if (dataFileName.equals("dataFileNameToChange.csv")) {
            ClusLogger.getInstance().error("The data file name is not given, left to dataFileNameToChange.csv. Please enter the name of the data file.");
        } else if (dataFileName.equals("")) {
            ClusLogger.getInstance().error("Data file name empty in settings.");
        }

        if (merger && mergerRate == -1) {
            ClusLogger.getInstance().warning("Merge wanted but merger criterion incorrect. Merge set to false.");
            merger = false;
        }

        if (train && (trainingFileName.equals("404") || trainingFileName == null)) {
            ClusLogger.getInstance().warning("Training wanted but training file incorrect. Train set to false.");
            train = false;
        }

        if (exportCompareToRC && (RCFile.equals("404") || RCFile == null)) {
            ClusLogger.getInstance().warning("Comparison to Root Causes wanted but RC file incorrect. exportCompareToRC set to false.");
            exportCompareToRC = false;
        }
        
        if (dataFileName != null) {
            dataName = dataFileName.split(".csv")[0]; // the fileName contains at most one ".", for ".csv".
        }
	}

	// Reads the parameters from the settings file settings.txt.
    private void readSettingsFile() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(settingsFileName));

            // Sorry for bad joke, for more clarity you can rename tinder to pattern, delphine to line and elior to matcher.
            String delphine;
            Pattern tinder = Pattern.compile("#([a-zA-Z]+):(.+)?");
            while ((delphine = br.readLine()) != null) {
                Matcher elior = tinder.matcher(delphine);
                if (elior.matches()) {
                    switch (elior.group(1)) {
                        case "dataFileName":
                            notTreated.remove("dataFileName");
                            if (elior.group(2).matches("^[-a-zA-Z_0-9]+\\.csv$")) {
                                dataFileName = elior.group(2);
                            } else {
                                ClusLogger.getInstance().error("Impossible to read dataFileName, format not correct (example : file.csv).");
                            }
                            break;
                        case "useLastDataFile":
                            notTreated.remove("useLastDataFile");
                            if (elior.group(2).matches("YES|NO")) {
                                useLastDataFile = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read useLastDataFile. Guessing yes, default value.");
                                useLastDataFile = true;
                            }
                            break;
                        case "regenerateSettings":
                            notTreated.remove("regenerateSettings");
                            if (elior.group(2).matches("YES|NO")) {
                                if (elior.group(2).equals("YES")) reinitializeSettingsFile();
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read regenerateSettings. Guessing yes.");
                                reinitializeSettingsFile();
                            }
                            break;
                        case "algorithm":
                            notTreated.remove("algorithm");
                            if (elior.group(2).matches("FUZZYART")) algorithm = PossibleAlgorithms.FUZZYART;
                            else if (elior.group(2).matches("ART1")) algorithm = PossibleAlgorithms.ART1;
                            else {
                                ClusLogger.getInstance().warning("Impossible to read algorithm. Guessing FUZZYART, default value.");
                                algorithm = PossibleAlgorithms.FUZZYART;
                            }
                            break;
                        case "vigilanceCriterion":
                            notTreated.remove("vigilanceCriterion");
                            try {
                                if (elior.group(2) != null) {
                                    vigilanceCriterion = Double.parseDouble(elior.group(2));
                                    if (vigilanceCriterion < 0 || vigilanceCriterion > 1)
                                        throw new Exception("Value not between 0 and 1.");
                                } else throw new Exception("Empty value.");
                            } catch (Exception e) {
                                ClusLogger.getInstance().warning("Impossible to read vigilanceCriterion. Guessing .9, default value.");
                                vigilanceCriterion = .9;
                            }
                            break;
                        case "merger":
                            notTreated.remove("merger");
                            if (elior.group(2).matches("YES|NO")) {
                                merger = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read merger. Guessing yes.");
                                merger = true;
                            }
                            break;
                        case "mergerRate":
                            notTreated.remove("mergerRate");
                            try {
                                if (elior.group(2) != null) {
                                    mergerRate = Double.parseDouble(elior.group(2));
                                    if (mergerRate < 0 || mergerRate > 1)
                                        throw new Exception("Value not between 0 and 1.");
                                } else throw new Exception("Empty value.");
                            } catch (Exception e) {
                                ClusLogger.getInstance().warning("Impossible to read mergerRate : " + e.getMessage());
                                ClusLogger.getInstance().writeInLog("Guessing 0.1, default value.");
                                mergerRate = 0.1;
                            }
                            break;
                        case "train":
                            notTreated.remove("train");
                            if (elior.group(2).matches("YES|NO")) {
                                train = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read train. Guessing no, default value.");
                                train = false;
                            }
                            break;
                        case "trainingFileName":
                            notTreated.remove("trainingFileName");
                            if (elior.group(2).matches("^[-a-zA-Z_0-9]+\\.csv$")) {
                                trainingFileName = elior.group(2);
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read trainingFileName.");
                                trainingFileName = "404";
                            }
                            break;
                        case "irrelevantColumns":
                            notTreated.remove("irrelevantColumns");
                            if (elior.group(2).matches("^(([-a-zA-Z_0-9]+\\|)+)?[-a-zA-Z_0-9]+$")) {
                                irrelevantColumns = elior.group(2);
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read irrelevantColumns. Going for default value, _ID|_REF|LineNumber|_NB|KEY|Key|SourceName|TIMESTAMP.");
                                irrelevantColumns = "_ID|_REF|LineNumber|_NB|KEY|Key|SourceName|TIMESTAMP";
                            }
                            break;
                        case "exportCompareToRC":
                            notTreated.remove("exportCompareToRC");
                            if (elior.group(2).matches("YES|NO")) {
                                exportCompareToRC = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportCompareToRC. Guessing no, default value.");
                                exportCompareToRC = false;
                            }
                            break;
                        case "RCFile":
                            notTreated.remove("RCFile");
                            if (elior.group(2).matches("^[-a-zA-Z_0-9]+\\.csv$")) {
                                RCFile = elior.group(2);
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read RCFile.");
                                ClusLogger.getInstance().writeInLog("Not exporting comparison.");
                                RCFile = "404";
                            }
                            break;
                        case "exportTrades":
                            notTreated.remove("exportTrades");
                            if (elior.group(2).matches("YES|NO")) {
                                exportTrades = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportTrades. Guessing yes, default value.");
                                exportTrades = true;
                            }
                            break;
                        case "exportWeights":
                            notTreated.remove("exportWeights");
                            if (elior.group(2).matches("YES|NO")) {
                                exportWeights = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportWeights. Guessing yes, default value.");
                                exportWeights = true;
                            }
                            break;
                        case "exportLRWeights":
                            notTreated.remove("exportLRWeights");
                            if (elior.group(2).matches("YES|NO")) {
                                exportLRWeights = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportLRWeights. Guessing yes, default value.");
                                exportLRWeights = true;
                            }
                            break;
                        case "exportVariances":
                            notTreated.remove("exportVariances");
                            if (elior.group(2).matches("YES|NO")) {
                                exportVariances = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportVariances. Guessing yes, default value.");
                                exportVariances = true;
                            }
                            break;
                        case "exportDistances":
                            notTreated.remove("exportDistances");
                            if (elior.group(2).matches("YES|NO")) {
                                exportDistances = elior.group(2).equals("YES");
                            } else {
                                ClusLogger.getInstance().warning("Impossible to read exportDistances. Guessing yes, default value.");
                                exportDistances = true;
                            }
                            break;
                        default:
                            ClusLogger.getInstance().warning("Unknown value " + elior.group(1) + " in the settings file. To regenerate the file, try #regenerate:YES.");
                            break;
                    }
                }
            }
            if (dataFileName.equals("dataFileNameToChange.csv")) {
                ClusLogger.getInstance().error("The data file name is not given, left to dataFileNameToChange.csv. Please enter the name of the data file.");
            } else if (notTreated.contains("dataFileName")) {
                ClusLogger.getInstance().error("Data file name not present in settings.");
            } else if (dataFileName.equals("")) {
                ClusLogger.getInstance().error("Data file name empty in settings.");
            }

            if (merger && mergerRate == -1) {
                ClusLogger.getInstance().warning("Merge wanted but merger criterion incorrect. Merge set to false.");
                merger = false;
            }

            if (train && (trainingFileName.equals("404") || trainingFileName == null)) {
                ClusLogger.getInstance().warning("Training wanted but training file incorrect. Train set to false.");
                train = false;
            }

            if (exportCompareToRC && (RCFile.equals("404") || RCFile == null)) {
                ClusLogger.getInstance().warning("Comparison to Root Causes wanted but RC file incorrect. exportCompareToRC set to false.");
                exportCompareToRC = false;
            }
        } catch (Exception e) {
            String message;
            if (e.getMessage() == null) message = "No file specified.";
            else message = e.getMessage();
            reinitializeSettingsFile();
            ClusLogger.getInstance().error("Impossible to read the settings file : " + message);
        }
    }

    // To reinitialize the settings file.
    private void reinitializeSettingsFile() {
        ClusLogger.getInstance().writeInLog("Regenerating the setting file ...");
        try {
            PrintWriter settingsFile = new PrintWriter(new BufferedWriter(new FileWriter(settingsFileName, false))); //false to init the file");
            settingsFile.println("                                              ********************************************");
            settingsFile.println("                                              **                                        **");
            settingsFile.println("                                              **                SETTINGS                **");
            settingsFile.println("                                              **                                        **");
            settingsFile.println("                                              ********************************************");
            settingsFile.println("");
            settingsFile.println("");
            settingsFile.println("PLEASE REFER TO THE DOCUMENTATION TO HAVE INFORMATION ABOUT THE PARAMETERS.");
            settingsFile.println("");
            settingsFile.println("- Only the lines beginning with a '#' are taken into account, feel free to add more stuff.");
            settingsFile.println("- If the file is detected as corrupted (i.e. incorrect parameters), it will be regenerated.");
            settingsFile.println("- Each value follows this syntax : #parameterName:value.");
            settingsFile.println("- Float numbers must be written with dotes (0.3 or 2.938 and not 3,4).");
            settingsFile.println("- The order does not matter.");
            settingsFile.println("");
            settingsFile.println("");
            settingsFile.println("");

            settingsFile.println("DATA");
            settingsFile.println("");
            settingsFile.println("#dataFileName:dataFileNameToChange.csv");
            settingsFile.println("#useLastDataFile:YES");
            settingsFile.println("");
            settingsFile.println("");
            settingsFile.println("ALGORITHM");
            settingsFile.println("");
            settingsFile.println("#algorithm:FUZZYART");
            settingsFile.println("#vigilanceCriterion:0.9");
            settingsFile.println("#irrelevantColumns:_ID|_REF|LineNumber|_NB|KEY|Key|SourceName|TIMESTAMP");
            settingsFile.println("#merger:NO");
            settingsFile.println("#mergerRate:0.1");
            settingsFile.println("#train:NO");
            settingsFile.println("#trainingFileName:training.csv");
            settingsFile.println("");
            settingsFile.println("");
            settingsFile.println("EXPORT");
            settingsFile.println("");
            settingsFile.println("#exportTrades:YES");
            settingsFile.println("#exportWeights:YES");
            settingsFile.println("#exportLRWeights:YES");
            settingsFile.println("#exportVariances:NO");
            settingsFile.println("#exportDistances:NO");
            settingsFile.println("#exportCompareToRC:NO");
            settingsFile.println("#RCFile:rc.csv");
            settingsFile.println("");
            settingsFile.println("");
            settingsFile.println("");

            settingsFile.println("#regenerateSettings:NO");
            settingsFile.println("");

            settingsFile.close();

            ClusLogger.getInstance().writeInLog("Done.");
            ClusLogger.getInstance().writeInLog("");
            ClusLogger.getInstance().endProg("The settings file is regenerated, please fill it before starting the program again.");
        } catch (Exception e) {
            ClusLogger.getInstance().warning("Impossible to regenerate the settings file. Proceeding with default values.");
        }
    }


    // Getters and Setters

    public String getDataFileName() {
        return dataFileName;
    }

    public PossibleAlgorithms getAlgorithm() {
        return algorithm;
    }

    public double getVigilanceCriterion() {
        return vigilanceCriterion;
    }

    public boolean isMerger() {
        return merger;
    }

    public double getMergerRate() {
        return mergerRate;
    }

    public boolean isUseLastDataFile() {
        return useLastDataFile;
    }

    public String getDataName() {
        return dataName;
    }

    public boolean isTrain() {
        return train;
    }

    public String getIrrelevantColumns() {
        return irrelevantColumns;
    }

    public boolean isExportCompareToRC() {
        return exportCompareToRC;
    }

    public String getRCFile() {
        return RCFile;
    }

    public String getTrainingFileName() {
        return trainingFileName;
    }

    public boolean isExportTrades() {
        return exportTrades;
    }

    public boolean isExportWeights() {
        return exportWeights;
    }

    public boolean isExportLRWeights() {
        return exportLRWeights;
    }

    public boolean isExportVariances() {
        return exportVariances;
    }

    public boolean isExportDistances() {
        return exportDistances;
    }

    public void setUseLastDataFileToFalse() {
        this.useLastDataFile = false;
    }

    public void setTrainToFalse() {
        this.train = false;
    }   

    public String getDataAbsolutePath() {
		return dataAbsolutePath;
	}

	public String getCsvSplitBy() {
		return csvSplitBy;
	}

	public void setCsvSplitBy(String csvSplitBy) {
		this.csvSplitBy = csvSplitBy;
	}

	@Override
    public String toString() {
        return "Settings{" +
                "settingsFileName='" + settingsFileName + '\'' +
                ", dataFileName='" + dataFileName + '\'' +
                ", dataName='" + dataName + '\'' +
                ", useLastDataFile=" + useLastDataFile +
                ", algorithm=" + algorithm +
                ", vigilanceCriterion=" + vigilanceCriterion +
                ", merger=" + merger +
                ", mergerRate=" + mergerRate +
                ", train=" + train +
                ", trainingFileName='" + trainingFileName + '\'' +
                ", exportCompareToRC=" + exportCompareToRC +
                ", RCFile='" + RCFile + '\'' +
                ", exportTrades=" + exportTrades +
                ", exportWeights=" + exportWeights +
                ", exportLRWeights=" + exportLRWeights +
                ", exportVariances=" + exportVariances +
                ", exportDistances=" + exportDistances +
                '}';
    }

}
