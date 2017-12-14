/*
 * Created by benoit.audigier on 6/13/2017 5:24 PM 6:17 PM 6:17 PM 6:17 PM.
 */
package DataTool;

import NeurTools.PossibleAlgorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

public class Data implements Serializable {
    private int numberOfTrade;
    private int numberOfCategories;
    private Trade[] trades;
    private String[] categories;
    private String[] principalCategories;
    private HashMap<String, FigureCategory> figureCategories;
    private HashMap<String, Integer> reversedCategories;
    private HashMap<String, Integer> reversedPrincipalCategories;
    private HashMap<Integer, String> irrelevantCategories;

    // Main constructor, that retrieves the data from scratch
    public Data(String fileName, PossibleAlgorithms algorithm, String irrelevantColumns, String cvsSplitBy) {
        ClusLogger.getInstance().writeInLog("Reading data ...");

        /**
         * Thanh Nguyen
         * add CSVSplitBy
         */
        CSVReader tmp = new CSVReader(fileName, irrelevantColumns, cvsSplitBy);
        Object[] categoriesInformation = tmp.getCategoriesInformation(); // This function retrieves several information, stored in an Object array; the order is important.

        numberOfTrade = (int) categoriesInformation[0];
        categories = (String[]) categoriesInformation[1];
        principalCategories = (String[]) categoriesInformation[2];
        irrelevantCategories = (HashMap<Integer, String>) categoriesInformation[3];
        HashMap<Integer, String> figureCategoriesName = (HashMap<Integer, String>) categoriesInformation[4];

        initializeFigureCategories(figureCategoriesName, algorithm); // To generate the categories linked to figure categories.
        this.numberOfCategories = categories.length;

        // categories are now almost final: some will be removed if useless, but none will be added.

        reversedCategories = new HashMap<>(1000); // the position of each category with its name.
        reversedPrincipalCategories = new HashMap<>(200); // same
        for (int i = 0; i < categories.length; i++) reversedCategories.put(categories[i], i);
        for (int i = 0; i < principalCategories.length; i++) reversedPrincipalCategories.put(principalCategories[i], i);

        // Time to retrieve the information for the trade.
        // If figure categories are taken into account, then the min and max (see documentation, 2. The algorithm -> A. Treatment of the data -> The categories.
        trades = tmp.getTradesValues(numberOfTrade, categories, principalCategories, reversedCategories, irrelevantCategories, figureCategories, algorithm);

        // Only FuzzyArt can deal with analog values, Art1 can only use binary vectors.
        if (algorithm == PossibleAlgorithms.FUZZYART) processMinAndMaxValues();

        removeUselessColumns();
        // The categories are now final.


    }

    // For the dev, for the tests
    public Data(Trade[] trades) {
        this.trades = trades;
        this.numberOfCategories = trades[0].getValues().length;
        this.numberOfTrade = trades.length;
        this.categories = new String[numberOfCategories];
        for (int i = 0; i < numberOfCategories; i++) {
            categories[i] = "Category " + i;
        }
    }

    
    public void printAll(Data data){
    	for(int i=0; i<data.trades.length; ++i){
    		System.out.println(data.trades[i].tradeID + " : " + i);
    	}
    }

    static boolean isOnlySpace(String s) {
        // Sometimes the value is not null but only space.
        return s.matches("^([ ]+)?$");
    }

    // To shuffle the trades
    public void randomize() {
        Collections.shuffle(Arrays.asList(trades));
    }

    // Add figure categories to the categories (min, max and everything)
    private void initializeFigureCategories(HashMap<Integer, String> figureCategoriesName, PossibleAlgorithms algorithm) {
        // For each figure category we need to add several categories to characterize the values.
        // See the documentation, 2. The Algorithm -> A. Treatment of data -> The categories.
        figureCategories = new HashMap<>(50);
        String[] newCat;
        if (algorithm == PossibleAlgorithms.FUZZYART)
            newCat = new String[categories.length + figureCategoriesName.size() * 8];
        else newCat = new String[categories.length + figureCategoriesName.size() * 4];
        int i = categories.length;
        System.arraycopy(categories, 0, newCat, 0, categories.length);
        for (Map.Entry<Integer, String> cat : figureCategoriesName.entrySet()) {
            FigureCategory catObject = new FigureCategory(cat.getValue());
            figureCategories.put(cat.getValue(), catObject);
            if (algorithm == PossibleAlgorithms.FUZZYART) {
                newCat[i] = cat.getValue() + "_MAX";
                i++;
                //newCat[i] = cat.getValue() + "_MAX_REV";
                //i++;
                newCat[i] = cat.getValue() + "_MIN";
                i++;
                //newCat[i] = cat.getValue() + "_MIN_REV";
                //i++;
            }
            newCat[i] = cat.getValue() + "_oneIs1TheOther0";
            i++;
            //newCat[i] = cat.getValue() + "_oneIs1TheOther0_REV";
            //i++;
            newCat[i] = cat.getValue() + "_signError";
            i++;
           // newCat[i] = cat.getValue() + "_signError_REV";
            //i++;
        }
        categories = newCat;
    }

    // Implement the values for the added figure categories.
    private void processMinAndMaxValues() {
        // For each trade, we need to implement the value of the ratio, if calculable. if not, 1 is forced into MIN and MAX.
        for (Trade t : trades) {
            for (Map.Entry<String, FigureCategory> cat : figureCategories.entrySet()) {
                if (t.getValues()[reversedCategories.get(cat.getKey() + "_oneIs1TheOther0")] == 1.) {
                    t.modifyValue(reversedCategories.get(cat.getKey() + "_MIN"), 1);
                    //t.modifyValue(reversedCategories.get(cat.getKey() + "_MIN_REV"), 0);
                    t.modifyValue(reversedCategories.get(cat.getKey() + "_MAX"), 1);
                    //t.modifyValue(reversedCategories.get(cat.getKey() + "_MAX_REV"), 0);
                } else {
                    double percentage;
                    double v1 = Double.parseDouble(t.getVersion1()[reversedPrincipalCategories.get(cat.getKey())]);
                    double v2 = Double.parseDouble(t.getVersion2()[reversedPrincipalCategories.get(cat.getKey())]);
                    if (t.getValues()[reversedCategories.get(cat.getKey() + "_DIFF")] != 0)
                        percentage = Math.abs(v2 / v1 - 1);
                    else percentage = 0;
                    double x;
                    if (cat.getValue().getMax() - cat.getValue().getMin() != 0)
                        x = (percentage - cat.getValue().getMin()) / (cat.getValue().getMax() - cat.getValue().getMin());
                    else x = 0.5;
                    t.modifyValue(reversedCategories.get(cat.getKey() + "_MIN"), x);
                    //t.modifyValue(reversedCategories.get(cat.getKey() + "_MIN_REV"),1 - x);
                    t.modifyValue(reversedCategories.get(cat.getKey() + "_MAX"), 1 - x);
                    //t.modifyValue(reversedCategories.get(cat.getKey() + "_MAX_REV"), 1 - x);
                }
            }
        }
    }

    // Removing all the redundant column or those where every trade have the same value.
    private void removeUselessColumns() {
        ClusLogger.getInstance().writeInLog("Removing useless data ...");
        // The idea is to calculate a matrix of difference between the column, and to remove the column that are identical.

        double[] difference = new double[categories.length];
        double[][] correlation = new double[categories.length][categories.length];

        // Calculating the matrix
        for (int t = 0; t < trades.length; t++) {
            for (int i = 0; i < categories.length; i++) {
                if (t != 0) difference[i] += Math.abs(trades[t].getValues()[i] - trades[t - 1].getValues()[i]);
                for (int j = 0; j < i; j++) {
                    correlation[i][j] += Math.abs(trades[t].getValues()[i] - trades[t].getValues()[j]);
                }
            }
        }

        // Removing identical columns
        ArrayList<Integer> toRemove = new ArrayList<>(300);
        for (int i = 0; i < difference.length; i++) {
            if (difference[i] == 0) toRemove.add(i);
        }
        for (int i = 0; i < correlation.length; i++) {
            for (int j = 0; j < i; j++) {
                if (correlation[i][j] == 0 && !toRemove.contains(i)) toRemove.add(i);
            }
        }

        ClusLogger.getInstance().writeInLog("Updating trade values ...");
        for (Trade t : trades) {
            Double[] oldValues = new Double[categories.length];
            for (int i = 0; i < oldValues.length; i++) {
                oldValues[i] = t.getValues()[i];
            }
            Object[] tmp = updateFields(oldValues, toRemove);
            double[] newValues = new double[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                newValues[i] = (double) tmp[i];
            }
            t.setValues(newValues);
        }


        // Updating category fields
        Object[] tmp = updateFields(categories, toRemove);
        categories = new String[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            categories[i] = (String) tmp[i];
        }

        numberOfCategories = categories.length;
        reversedCategories = new HashMap<>(1000);
        for (int i = 0; i < categories.length; i++) reversedCategories.put(categories[i], i);

        ClusLogger.getInstance().writeInLog("Removed " + toRemove.size() + " categories.");
        ClusLogger.getInstance().writeInLog("Number of categories : " + numberOfCategories);

    }

    // Function to remove several indexes from an array
    private Object[] updateFields(Object[] toUpdate, ArrayList<Integer> toRemove) {
        // Removes field from an array
        Object[] res = new Object[toUpdate.length - toRemove.size()];
        try {

            int j = 0;
            for (int i = 0; i < toUpdate.length; i++) {
                if (!toRemove.contains(i)) {
                    res[j] = toUpdate[i];
                    j++;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // That should never happen.
            ClusLogger.getInstance().error("trying to remove a column that does not exist in Data.java, updateFields. Please contact a developer to solve this error.");
        }
        return res;
    }

    // Normalizing to make sure the average is 0 and the variance 1. Used by KMeans, revealed not adapted, not used in the program.
    private void normalizeColumns() {
        ClusLogger.getInstance().writeInLog("Normalizing ...");
        double[] average = new double[numberOfCategories];
        double[] variance = new double[numberOfCategories];

        // calculation of the average and variance for each column
        for (Trade t : trades) {
            for (int i = 0; i < t.getValues().length; i++) {
                average[i] += t.getValues()[i];
                variance[i] = average[i] * average[i];
            }
        }
        for (int i = 0; i < average.length; i++) {
            average[i] = average[i] / numberOfTrade;
            variance[i] = variance[i] / numberOfTrade;
        }

        // normalization
        for (Trade t : trades) {
            double[] val = t.getValues();
            for (int i = 0; i < t.getValues().length; i++) {
                val[i] = (val[i] - average[i]) / variance[i];
            }
            t.setValues(val);
        }
        ClusLogger.getInstance().writeInLog("Done.");
    }


    // Getters and Setters

    public Trade[] getTrades() {
        return trades;
    }

    public int getNumberOfCategories() {
        return numberOfCategories;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getPrincipalCategories() {
        return principalCategories;
    }

    public int getNumberOfTrade() {
        return numberOfTrade;
    }

    public HashMap<Integer, String> getIrrelevantCategories() {
        return irrelevantCategories;
    }

    public HashMap<String, Integer> getReversedCategories() {
        return reversedCategories;
    }


    // Function to regenerate the data to confirm the treatment, not used in the program.
    public void generate(String fileName) {
        try {
            PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false))); // false to init the file
            StringBuilder firstLine = new StringBuilder();
            int i = 0;
            for (String cat : principalCategories) {
                if (i == 0) {
                    firstLine.append(cat);
                    i = 1;
                } else firstLine.append(",").append(cat);
            }
            file.println(firstLine);

            for (Trade t : trades) {
                StringBuilder line1 = new StringBuilder();
                i = 0;
                for (String s : t.getVersion1()) {
                    if (i == 0) {
                        firstLine.append(s);
                        i = 1;
                    } else firstLine.append(",").append(s);
                }
                file.println(line1);
                StringBuilder line2 = new StringBuilder();
                i = 0;
                for (String s : t.getVersion2()) {
                    if (i == 0) {
                        firstLine.append(s);
                        i = 1;
                    } else firstLine.append(",").append(s);
                }
                file.println(line2);
            }

            file.close();
        } catch (Exception e) {
            ClusLogger.getInstance().writeInLog(e.getMessage());
        }
    }


    @Override
    public String toString() {
        return "NewData{" +
                "NumberOfTrade=" + numberOfTrade +
                ", trades=" + Arrays.toString(trades) +
                '}';
    }
}
