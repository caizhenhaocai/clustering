/*
 * Created by benoit.audigier on 6/13/2017 5:24 PM 6:17 PM 6:17 PM 6:17 PM.
 */
package DataTool;

import NeurTools.PossibleAlgorithms;

import java.io.*;
import java.util.HashMap;


class CSVReader {
    private String csvFile;
    private BufferedReader br = null;
    private String line = "";
//    private String cvsSplitBy = ",";
    private static String cvsSplitBy = "_;_";
    private String irrelevantColumns;

    
    CSVReader(String fileName, String irrelevantColumns, String cvsSplitBy) {
        csvFile = fileName;
        this.irrelevantColumns = irrelevantColumns;
        this.cvsSplitBy = cvsSplitBy;
    }

    //This function deals with lines that are not the right length. The "right" length is considered to be the number of category, hence the length of the first line.
    private static String[] treatLines(String[] v, int numberOfCategories) {
        if (v.length > numberOfCategories) {
            String[] tmp = v;
            v = new String[numberOfCategories];
            System.arraycopy(tmp, 0, v, 0, numberOfCategories);
        } else if (v.length < numberOfCategories) {
        	ClusLogger.getInstance().warning("There are empty values, categories and values might not be aligned.");
            String[] tmp = v;
            v = new String[numberOfCategories];
            for (int i = 0; i < numberOfCategories; i++) {
                if (i < tmp.length) v[i] = tmp[i];
                else v[i] = "";
            }
        }

        for (int i = 0; i < v.length; i++) {
            v[i] = treatValue(v[i]);
        }
        return v;
    }

    static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isEmptyLine(String[] str) {
        for (String s : str) {
            if (!s.equals("")) return false;
        }
        return true;
    }

    // This function tries to eliminate the noises from the data for the value, such as spaces at the end of the values, or unwanted quotation marks.
    // This is to be ameliorated since new sets of data may have other defects not seen so far.
    static String treatValue(String s) {
        s = s.replaceAll("[\"]", "");
        return s.trim();
    }

    // Tackling the case where a value is like 234441_3. , not relevant for the algorithm.
    // This is to be ameliorated since new sets of data may have other defects not seen so far.
    static boolean isNotRelevantValue(String s) {
        return s.matches("^[0-9]+[_][0-9]+?");
    }

    // Removing the category that are not meaningful.
    // This is to be ameliorated since new sets of data may have other defects not seen so far.
    private boolean isIrrelevantCategory(String s) {
        String regex = ".*(" + irrelevantColumns + ").*";
//        return s.matches(".*(_ID|_REF|LineNumber|_NB|KEY|Key|SourceName|TIMESTAMP).*");
        return s.matches(regex);
    }

    // Function to retrieve the categories that are going to characterize the trades.
    Object[] getCategoriesInformation() {
        // The idea is to read the data and spot the the figure categories, the "normal" categories and the irrelevant categories.
        // See the documentation, 2. The Algorithm -> A. Treatment of data -> The categories for more information on what should be a category.

        HashMap<String, String> categories = new HashMap<>(400, .75f);
        HashMap<Integer, String> figureCategories = new HashMap<>(100, .8f);
        String[] principalCategories;
        HashMap<Integer, String> irrelevantCategories = new HashMap<>(40);

        try {

            try {
                br = new BufferedReader(new FileReader(csvFile));
            } catch (FileNotFoundException e) {
                ClusLogger.getInstance().error("File " + csvFile + " not found.");
            }
            int lineCounter = 0;

            // The first line is read, and the known irrelevant categories are spotted.
            line = br.readLine();
            principalCategories = line.split(cvsSplitBy);
            for (int i = 0; i < principalCategories.length; i++) {
                principalCategories[i] = treatValue(principalCategories[i]);
                if (isIrrelevantCategory(principalCategories[i])) {
                    irrelevantCategories.put(i, principalCategories[i]);
                }
            }

            while (line != null) {
                String[] values = line.split(cvsSplitBy);
                line = br.readLine();

                if (line == null && lineCounter % 2 == 1)
                    ClusLogger.getInstance().warning("Odd number of lines about trades, probably missing information for one or several trades.");

                else if (isEmptyLine(values)) ClusLogger.getInstance().warning("Empty line, number " + (lineCounter + 1));
                else {
                    for (int i = 0; i < principalCategories.length && i < values.length; i++) {
                        if (!irrelevantCategories.containsKey(i)) {
                            String item = treatValue(values[i]);
                            //String item2 = treatValue(values[i]);
                            if (lineCounter == 0) {
                            	//item2+="_DIFF_REV";
                                item += "_DIFF"; // For the column titles, the interesting parameter is whether the data are different or not for the same trade.
                            } else if (lineCounter == 1 && isNumber(item)) {
                                figureCategories.put(i, principalCategories[i]); // All the data that are numbers in the first trade are added, and removed afterwards if the same category is not a number for some other trade.
                            } else if (lineCounter > 1 && !isNumber(values[i]) && figureCategories.containsKey(i)) {
                                figureCategories.remove(i);
                            }
                            if (Data.isOnlySpace(item)) {
                            	item = "_NULL"; // no data
                            
                            }
                            item = treatValue(item);
                            //item2=treatValue(item2);
                            if (!isNumber(item) && !isNotRelevantValue(item)) {
                                if (lineCounter == 0) {
                                	categories.put(item, item);
                                	//categories.put(item2, item2);
                                }
                                else {
                                    categories.put(principalCategories[i] + item, principalCategories[i] + item);
                                    //categories.put(principalCategories[i] + item+"_REV", principalCategories[i] + item+"_REV");
                                }
                            }
                        }
                    }
                    lineCounter++;
                }
            }
            int size; // each figures has a category for difference to 0, percentage, difference of sign.
            size = categories.size();
            String categoriesArray[] = new String[size];

            int i = 0;
            for (String key : categories.keySet()) {
                categoriesArray[i] = categories.get(key);
                i++;
            }
            ClusLogger.getInstance().writeInLog("Number of trades : " + (lineCounter - 1) / 2);

            return new Object[]{lineCounter - 1, categoriesArray, principalCategories, irrelevantCategories, figureCategories};// Returns number of trades (times two) and categories. Minus 1 is for the first line.
        } catch (IOException e) {
            ClusLogger.getInstance().error(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    ClusLogger.getInstance().warning(e.getMessage());
                }
            }
        }
        ClusLogger.getInstance().error("Encountered problem while categorizing.");
        return null;
    }

    // Implementation of the vectors with the categories provided by the method getCategoriesInformation().
    Trade[] getTradesValues(int numberOfTrades, String[] categories, String[] principalCategories, HashMap<String, Integer> reversedCategories,
                            HashMap<Integer, String> irrelevantCategories, HashMap<String, FigureCategory> figureCategories, PossibleAlgorithms algorithm) {
        // The idea is to retrieve the two lines that correspond to each trade, and create a new trade.
        // This is to be ameliorated since new sets of data may have other defects not seen so far.

        Trade[] res = new Trade[numberOfTrades / 2];

        try {
            try {
                br = new BufferedReader(new FileReader(csvFile));
            } catch (FileNotFoundException e) {
                ClusLogger.getInstance().error("File " + csvFile + " not found.");
            }
            int lineCounter = 0;
            line = br.readLine();
            if (line == null) {
                ClusLogger.getInstance().error("File empty.");
            }
            while ((line = br.readLine()) != null) {
                String[] v1 = line.split(cvsSplitBy);
                if (!isEmptyLine(v1)) {
                    v1 = treatLines(v1, principalCategories.length);
                    line = br.readLine();
                    if (line == null) {
                        ClusLogger.getInstance().warning("Data incomplete, odd number of lines for the trades.");
                    } else {
                        String[] v2 = line.split(cvsSplitBy);
                        v2 = treatLines(v2, principalCategories.length);
                        // The first column must contain the ID
                        try {
                            res[lineCounter] = new Trade(lineCounter, Integer.parseInt(treatValue(v1[0])), v1, v2, categories, principalCategories, reversedCategories,
                                    irrelevantCategories, figureCategories, algorithm);
//                            res[lineCounter] = new Trade(lineCounter, treatValue(v1[0]), v1, v2, categories, principalCategories, reversedCategories,
//                                    irrelevantCategories, figureCategories, algorithm);
                        } catch (NumberFormatException e) {
                            ClusLogger.getInstance().error("First column does not contains the number of the trades. Please put this column first.");
                            break;
                        }
                        lineCounter++;
                    }
                } else ClusLogger.getInstance().warning("Empty line, number " + (2 * (lineCounter + 1)));
            }
        } catch (IOException e) {
            ClusLogger.getInstance().warning(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    ClusLogger.getInstance().warning(e.getMessage());
                }
            }
        }
        return res;
    }

}
