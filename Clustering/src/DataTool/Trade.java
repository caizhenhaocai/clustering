/*
 * Created by benoit.audigier on 6/13/2017 5:24 PM 6:17 PM 6:17 PM 6:17 PM.
 */
package DataTool;

import NeurTools.PossibleAlgorithms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class Trade implements Serializable {
    private int tradeNumber;
    private String[] version1;
    private String[] version2;
    private double[] values;
    int tradeID;

	// For the dev, for the tests
    public Trade(int tradeNumber, double[] values) {
        this.tradeNumber = tradeNumber;
        this.values = values;
    }

    // Construction of a trade from the CSVReader
    Trade(int tradeNumber, int tradeID, String[] version1, String[] version2, String[] categories, String[] principalCategories, HashMap<String, Integer> reversedCategories,
          HashMap<Integer, String> irrelevantCategories, HashMap<String, FigureCategory> figureCategories, PossibleAlgorithms algorithm) { // principal categories are just the names of the categories.
        // This is a disjunction case, more detailed in the documentation (see 2. The Algorithm, A. Data Treatment, The categories).

        this.tradeID = tradeID;
        this.tradeNumber = tradeNumber;
        this.version1 = version1;
        this.version2 = version2;


        if (version1.length != version2.length) {
            ClusLogger.getInstance().warning("For of trade number " + tradeNumber + "; data from different versions do not have the same length.");
        } else { // same length everywhere
            values = new double[categories.length];
            for (int i = 0; i < principalCategories.length; i++) {
                if (!irrelevantCategories.containsKey(i)) {
                    version1[i] = CSVReader.treatValue(version1[i]);
                    version2[i] = CSVReader.treatValue(version2[i]);
                    boolean same = (version1[i].equals(version2[i]));
                    if (same) {
                        values[reversedCategories.get(principalCategories[i] + "_DIFF")] = 0;
                        //values[reversedCategories.get(principalCategories[i] + "_DIFF_REV")] = 1;
                        if (!CSVReader.isNumber(version1[i])) {
                            if (Data.isOnlySpace(version1[i])) {
                                values[reversedCategories.get(principalCategories[i] + "_NULL")] = 1; // if there is no data, fill it in the i_NULL category associated.
                                //values[reversedCategories.get(principalCategories[i] + "_NULL_REV")]=0;
                            } else if (!CSVReader.isNotRelevantValue(version1[i])) {
                                values[reversedCategories.get(principalCategories[i] + version1[i])] = 1;
                            	//values[reversedCategories.get(principalCategories[i] + version1[i]+"_REV")] = 0;
                            }
                        } else if (figureCategories.containsKey(principalCategories[i])) {
                            values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0")] = 0;
                            //values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0_REV")] = 1;
                            values[reversedCategories.get(principalCategories[i] + "_signError")] = 0;
                            //values[reversedCategories.get(principalCategories[i] + "_signError_REV")] = 1;
                        }
                    } else {
                        values[reversedCategories.get(principalCategories[i] + "_DIFF")] = 1;
                        //values[reversedCategories.get(principalCategories[i] + "_DIFF_REV")] = 0;
                        if (CSVReader.isNumber(version1[i]) && figureCategories.containsKey(principalCategories[i])) {
                            if (Double.parseDouble(version1[i]) == 0 || Double.parseDouble(version2[i]) == 0) {
                                values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0")] = 1;
                                //values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0_REV")] = 0;
                                values[reversedCategories.get(principalCategories[i] + "_signError")] = 0;
                                //values[reversedCategories.get(principalCategories[i] + "_signError_REV")] = 1;

                            } else {
                                double v1 = Double.parseDouble(version1[i]);
                                double v2 = Double.parseDouble(version2[i]);
                                if (algorithm == PossibleAlgorithms.FUZZYART) {
                                    double percentage = v2 / v1 - 1;
                                    // These value will be updated later
                                    values[reversedCategories.get(principalCategories[i] + "_MAX")] = -999999999999999999999.;
                                    //values[reversedCategories.get(principalCategories[i] + "_MAX_REV")] = -999999999999999999999.;
                                    values[reversedCategories.get(principalCategories[i] + "_MIN")] = 999999999999999999999.;
                                    //values[reversedCategories.get(principalCategories[i] + "_MIN_REV")] = 999999999999999999999.;
                                    FigureCategory currentFigureCategory = figureCategories.get(principalCategories[i]);
                                    if (currentFigureCategory.getMax() < percentage)
                                        figureCategories.put(principalCategories[i], new FigureCategory(principalCategories[i], percentage, currentFigureCategory.getMin()));
                                    else if (currentFigureCategory.getMin() > percentage)
                                        figureCategories.put(principalCategories[i], new FigureCategory(principalCategories[i], currentFigureCategory.getMax(), percentage));
                                }
                                values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0")] = 0;
                                //values[reversedCategories.get(principalCategories[i] + "_oneIs1TheOther0_REV")] = 1;
                                values[reversedCategories.get(principalCategories[i] + "_signError")] = Math.abs((Math.signum(v1 / v2) - 1) / 2);
                                //values[reversedCategories.get(principalCategories[i] + "_signError_REV")] = 1-Math.abs((Math.signum(v1 / v2) - 1) / 2);
                                
                            }
                        }
                        if (Data.isOnlySpace(version1[i]) || Data.isOnlySpace(version2[i])) {
                            values[reversedCategories.get(principalCategories[i] + "_NULL")] = 1; // if there is no data, fill it in the i_NULL category associated.
                            //values[reversedCategories.get(principalCategories[i] + "_NULL_REV")] = 0;
                        } else {
                            if (!CSVReader.isNumber(version1[i]) && !Data.isOnlySpace(version1[i]) && !CSVReader.isNotRelevantValue(version1[i])) {
                                values[reversedCategories.get(principalCategories[i] + version1[i])] = 1;
                                //values[reversedCategories.get(principalCategories[i] + version1[i]+"_REV")] = 0;
                            }
                            if (!CSVReader.isNumber(version2[i]) && !Data.isOnlySpace(version2[i]) && !CSVReader.isNotRelevantValue(version2[i])) {
                                values[reversedCategories.get(principalCategories[i] + version2[i])] = 1;
                                //values[reversedCategories.get(principalCategories[i] + version2[i]+"_REV")] = 0;
                            }
                        }
                    }
                }
            }
        }
    }


    // Getters and setters

    public int getTradeID() {
		return tradeID;
	}

	public void setTradeID(int tradeID) {
		this.tradeID = tradeID;
	}
    
    public double[] getValues() {
        return values;
    }

    public int getTradeNumber() {
        return tradeNumber;
    }

    public String[] getVersion1() {
        return version1;
    }

    public String[] getVersion2() {
        return version2;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    void modifyValue(int index, double value) { // Modifies the value of one category for a tread
        values[index] = value;
    }

    @Override
    public String toString() {
        return "Trade Number : " + tradeNumber +
                " : " + Arrays.toString(values);
    }
}