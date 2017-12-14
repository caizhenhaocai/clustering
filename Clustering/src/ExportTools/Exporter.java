/*
 * Created by benoit.audigier on 6/30/2017 12:17 PM.
 */

package ExportTools;

import DataTool.Data;
import DataTool.ClusLogger;
import NeurTools.*;

import Settings.Settings;
import DataTool.Utils;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Exporter {
	private String fileName;
	private Nodes nodes;
	private Inputs inputs;

	private boolean exportVariances;
	private boolean exportDistances;
	private boolean exportWeights;
	private boolean exportTrades;
	private boolean exportLR;

	private boolean exportComparisonToRC;
	private String RCFile;

	private PossibleAlgorithms algorithm;
	private double vigilanceCriterion;

	public Exporter(Inputs inputs, Nodes nodes, Settings settings) {
		this.fileName = settings.getDataName();
		this.nodes = nodes;
		this.inputs = inputs;

		this.exportTrades = settings.isExportTrades();
		this.exportWeights = settings.isExportWeights();
		this.exportVariances = settings.isExportVariances();
		this.exportDistances = settings.isExportDistances();
		this.exportLR = settings.isExportLRWeights();

		this.exportComparisonToRC = settings.isExportCompareToRC();
		this.RCFile = settings.getRCFile();

		this.algorithm = settings.getAlgorithm();
		this.vigilanceCriterion = settings.getVigilanceCriterion();
	}

	// To trigger the export of all the files specified in the settings
	public void export() {
		File directory = new File("results_cluster\\"); // to be sure the
														// directory is created
		if (directory.mkdir())
			ClusLogger.getInstance().writeInLog("Folder results_cluster/ created");

		ClusLogger.getInstance().writeInLog("Exporting output for " + fileName + " ...");
		String resultFileName = fileName + "_" + algorithm + ((int) (vigilanceCriterion * 10));
		ExcelWriter excelFile = null;
		try {
			excelFile = new ExcelWriter("results_cluster/" + resultFileName);
		} catch (Exception e) {
			ClusLogger.getInstance().warning(e.getMessage());
			try {
				excelFile = new ExcelWriter(resultFileName);
			} catch (Exception ex) {
				ClusLogger.getInstance().error("Error : " + e.getMessage());
			}
		}

		if (excelFile.getWorkbook() != null) { // NullPointerException not
												// possible since the program
												// would stop at Logger.getInstance().error
			if (exportTrades)
				excelFile.addSheetArrayList(exportTrades(), "Trades");
			if (exportWeights) {
				Pair<ArrayList<ArrayList<Object>>, ArrayList<ArrayList<Object>>> weights = exportWeights();
				excelFile.addSheetArrayList(weights.getKey(), "Weights - prciple categories");
				excelFile.addSheetArrayList(weights.getValue(), "Weights - all categories");
			}
			if (exportLR) {
				Pair<ArrayList<ArrayList<Object>>, ArrayList<ArrayList<Object>>> weights = exportLogicalRegressionWeights();
				if (weights != null) {
					excelFile.addSheetArrayList(weights.getKey(), "LR Weights - prciple categories");
					excelFile.addSheetArrayList(weights.getValue(), "LR Weights - all categories");
				}
			}
			if (exportVariances)
				excelFile.addSheetArrayList(exportVariances(), "Variances");
			if (exportDistances)
				excelFile.addSheetArrayList(exportDistances(), "Distances");

			if (exportComparisonToRC)
				excelFile.addSheetArrayList(exportComparisonToRC(RCFile), "Comparison to RC");

		}

		// Nodes rc = RootCause.getRootCauses("rc2.csv", true);
		// excelFile.addSheetArrayList(exportComparison(nodes, "FUZZY9merged",
		// rc, "RCSEP"), "Comparison to RCSEP");

		ClusLogger.getInstance().writeInLog("Done.");
		excelFile.closeFile();
	}

	// All those functions create an ArrayList<ArrayList<Object>> to use the
	// function of ExcelWriter to create a sheet
	private ArrayList<ArrayList<Object>> exportVariances() {

		ArrayList<ArrayList<Object>> content = new ArrayList<>(100);

		ArrayList<Object> firstLine = new ArrayList<>();
		firstLine.add("Cluster ID");
		ArrayList<Object> secondLine = new ArrayList<>();
		secondLine.add("Size");
		ArrayList<Object> thirdLine = new ArrayList<>();
		thirdLine.add("Variance");

		int i = 1;
		for (Node n : nodes.getNodes()) {
			firstLine.add(i);
			secondLine.add(n.getSize());
			thirdLine.add(n.variance());
			i++;
		}

		content.add(firstLine);
		content.add(secondLine);
		content.add(thirdLine);

		ClusLogger.getInstance().writeInLog("- Variances");
		return content;
	}

	private ArrayList<ArrayList<Object>> exportDistances() {
		ArrayList<ArrayList<Object>> content = new ArrayList<>(100);
		ArrayList<Object> firstLine = new ArrayList<>(100);
		firstLine.add("Cluster ID");
		for (int j = 1; j <= nodes.getNumberOfClusters(); j++) {
			firstLine.add(j);
		}
		content.add(firstLine);

		int i = 1;
		for (Node n : nodes.getNodes()) {
			ArrayList<Object> line = new ArrayList<>(100);
			line.add(i);
			for (Node n2 : nodes.getNodes()) {
				line.add(Node.distance(n2.centroid(), n.centroid()));
			}
			content.add(line);
			i++;
		}

		ClusLogger.getInstance().writeInLog("- Distances");
		return content;
	}

	private ArrayList<ArrayList<Object>> exportTrades() {

		Data data = inputs.getData();

		// data.printAll(data);
		ArrayList<ArrayList<Object>> content = new ArrayList<>(nodes.getNumberOfClusters() + 10);

		try {
			RootCauseNodes test = (RootCauseNodes) nodes; // Not possible to
															// export the trades
															// classified as
															// root causes.
			ClusLogger.getInstance().warning("Trying to export the trades in clusters of Root Cause.");
		} catch (Exception e) {
			ArrayList<Object> line = new ArrayList<>();
			line.add("Cluster ID");
			line.addAll(Arrays.asList(data.getPrincipalCategories()));
			content.add(line);

			/**
			 * author: Thanh Nguyen fix bug for wrong iteration Contruct a
			 * HashMap for matching NB from Nodes and its Data (to the correct
			 * cluster ID)
			 */

			Map<Integer, TradeInfo> tradeData = new HashMap<Integer, TradeInfo>();
			TradeInfo info;

			for (Node n : nodes.getNodes()) {
				for (Input t : n.getElements()) {
					if (tradeData.containsKey(t.getId())) {
						ClusLogger.getInstance().warning("Duplicate trade number: " + t.getId()
								+ ". Please check your input file for duplication");
					} else {
						info = new TradeInfo();
						info.setClusterID(n.getId() + 1);
						tradeData.put(t.getId(), info);
					}
				}
			}

			System.out.println("Map size: " + tradeData.size());

			for (int i = 0; i < data.getTrades().length; ++i) {
				if (tradeData.containsKey(data.getTrades()[i].getTradeID())) {

					if (tradeData.get(data.getTrades()[i].getTradeID()).getTrade() == null) {
						tradeData.get(data.getTrades()[i].getTradeID()).setTrade(data.getTrades()[i]);
					} else {
						ClusLogger.getInstance().warning("Alr exist trade data for this TRN_NB : " + data.getTrades()[i].getTradeID());
					}

				} else {
					ClusLogger.getInstance().warning("Cannot find TRN_NB : " + data.getTrades()[i].getTradeID() + " in Nodes");
				}
			}

			tradeData = Utils.sortHashMapByValues(tradeData);

			for (Entry<Integer, TradeInfo> entry : tradeData.entrySet()) {
				// MX 2
				line = new ArrayList<>();
				line.add(entry.getValue().getClusterID());
				line.addAll(Arrays.asList(entry.getValue().getTrade().getVersion1()));
				content.add(line);

				// MX3 line
				line = new ArrayList<>();
				line.add(entry.getValue().getClusterID());
				line.addAll(Arrays.asList(entry.getValue().getTrade().getVersion2()));
				content.add(line);
			}

			// int id = 0;
			// for (Node n : nodes.getNodes()) {
			// for (Input t : n.getElements()) { //t.id is TRN_NB
			// // MX2 line
			// line = new ArrayList<>();
			// line.add(n.getId()+1); // cluster id
			//// line.addAll(Arrays.asList(data.getTrades()[t.getTradeNumber()].getVersion1()));
			//// line.addAll(Arrays.asList(data.getTrades()[n.getId()].getVersion1()));
			// line.addAll(Arrays.asList(data.getTrades()[id].getVersion1()));
			// content.add(line);
			//
			// //MX3 line
			// line = new ArrayList<>();
			// line.add(n.getId()+1);
			//// line.addAll(Arrays.asList(data.getTrades()[t.getTradeNumber()].getVersion2()));
			//// line.addAll(Arrays.asList(data.getTrades()[n.getId()].getVersion2()));
			// line.addAll(Arrays.asList(data.getTrades()[id].getVersion2()));
			// content.add(line);
			// id++;
			// }
			// }
		}

		ClusLogger.getInstance().writeInLog("- Trades");
		return content;
	}

	private Pair<ArrayList<ArrayList<Object>>, ArrayList<ArrayList<Object>>> exportWeights() {

		// retrieving categories
		Data data = inputs.getData();
		HashMap<Integer, String> isPrincipalCategory = new HashMap<>(data.getPrincipalCategories().length);

		String[] categories = data.getCategories();
		for (int i = 0; i < data.getPrincipalCategories().length; i++) {
			if (!data.getIrrelevantCategories()
					.containsKey(data.getReversedCategories().get(data.getPrincipalCategories()[i])))
				isPrincipalCategory.put(data.getReversedCategories().get(data.getPrincipalCategories()[i] + "_DIFF"),
						data.getPrincipalCategories()[i]);
		}

		// Calculation of variances
		Double[] variances = new Double[categories.length];

		for (int j = 0; j < categories.length; j++) {
			double sum = 0;
			double squaredSum = 0;
			for (int i = 0; i < nodes.getNumberOfClusters(); i++) {
				double val = nodes.getNodes().get(i).getWeights()[0][j];
				sum += val;
				squaredSum += val * val;
			}
			double var = squaredSum / nodes.getNumberOfClusters() - Math.pow(sum / nodes.getNumberOfClusters(), 2);
			variances[j] = var;
		}

		int[] categoriesOrder = Algorithm.argSort(variances);
		int[] nodesOrder = sortBySize(nodes);

		// Preparation to export to excel
		ArrayList<ArrayList<Object>> content = new ArrayList<>(nodes.getNumberOfClusters() + 10);
		ArrayList<ArrayList<Object>> contentOnlyPrincipals = new ArrayList<>(nodes.getNumberOfClusters() + 10);

		ArrayList<Object> line = new ArrayList<>();
		ArrayList<Object> lineOnlyPrincipals = new ArrayList<>();

		line.add("Cluster ID");
		line.add("Size");
		lineOnlyPrincipals.add("Cluster ID");
		lineOnlyPrincipals.add("Size");

		for (int i : categoriesOrder) { // categoriesOrder can't be null
			if (isPrincipalCategory.containsKey(i))
				lineOnlyPrincipals.add(categories[i]);
			line.add(categories[i]);
		}
		content.add(line);
		contentOnlyPrincipals.add(lineOnlyPrincipals);

		for (int i : nodesOrder) {
			line = new ArrayList<>();
			lineOnlyPrincipals = new ArrayList<>();

			line.add(i + 1);
			line.add(nodes.getNodes().get(i).getSize());
			lineOnlyPrincipals.add(i + 1);
			lineOnlyPrincipals.add(nodes.getNodes().get(i).getSize());

			for (int j : categoriesOrder) {
				if (isPrincipalCategory.containsKey(j))
					lineOnlyPrincipals.add(nodes.getNodes().get(i).getWeights()[0][j]);
				line.add(nodes.getNodes().get(i).getWeights()[0][j]);
			}
			content.add(line);
			contentOnlyPrincipals.add(lineOnlyPrincipals);
		}

		line = new ArrayList<>();
		lineOnlyPrincipals = new ArrayList<>();

		line.add("");
		line.add("Variance");
		lineOnlyPrincipals.add("");
		lineOnlyPrincipals.add("Variance");

		for (int i : categoriesOrder) {
			if (isPrincipalCategory.containsKey(i))
				lineOnlyPrincipals.add(variances[i]);
			line.add(variances[i]);
		}

		content.add(line);
		contentOnlyPrincipals.add(lineOnlyPrincipals);

		ArrayList<Object> lastLineOnlyPrincipals = new ArrayList<>();
		lastLineOnlyPrincipals.add(
				"Note : columns might be removed if they are equivalent to another one or if every trade has the same value.");
		contentOnlyPrincipals.add(lastLineOnlyPrincipals);

		ClusLogger.getInstance().writeInLog("- Weights");
		return new Pair<>(contentOnlyPrincipals, content);
	}

	private Pair<ArrayList<ArrayList<Object>>, ArrayList<ArrayList<Object>>> exportLogicalRegressionWeights() {

		// retrieving the categories
		Data data = inputs.getData();
		HashMap<Integer, String> isPrincipalCategory = new HashMap<>(data.getPrincipalCategories().length);
		String[] categories = data.getCategories();
		String[] principalCategories = data.getPrincipalCategories();
		HashMap<String, Integer> reversedCategories = data.getReversedCategories();

		for (String s : principalCategories) {
			if (reversedCategories.containsKey(s + "_DIFF")) {
				isPrincipalCategory.put(reversedCategories.get(s + "_DIFF"), s + "_DIFF");
			}
		}

		// retrieving the weights
		Pair<double[], int[]> LR;
		if (nodes.getRegressionInformation() == null)
			LR = Algorithm.getLogisticRegression(inputs, nodes);
		else
			LR = nodes.getRegressionInformation(); // Regression may already
													// have been calculated

		if (LR == null) {
			ClusLogger.getInstance().warning("impossible to export logical regression.");
			return null;
		}
		Double[] weights = new Double[LR.getKey().length]; // necessary not to
															// be a primitive
															// type array for
															// the functions
															// used afterwards.
		for (int i = 0; i < weights.length; i++) {
			weights[i] = LR.getKey()[i];
		}
		int[] numberOf1 = LR.getValue();

		int[] categoriesOrder = Algorithm.argSort(weights);

		// Preparation to export to excel
		ArrayList<ArrayList<Object>> content = new ArrayList<>(nodes.getNumberOfClusters() + 10);
		ArrayList<ArrayList<Object>> contentOnlyPrincipals = new ArrayList<>(nodes.getNumberOfClusters() + 10);

		ArrayList<Object> firstLine = new ArrayList<>();
		ArrayList<Object> firstLineOnlyPrincipals = new ArrayList<>();
		firstLine.add("Category");
		firstLineOnlyPrincipals.add("Category");
		ArrayList<Object> secondLine = new ArrayList<>();
		ArrayList<Object> secondLineOnlyPrincipals = new ArrayList<>();
		secondLine.add("Number of 1");
		secondLineOnlyPrincipals.add("Number of 1");
		ArrayList<Object> thirdLine = new ArrayList<>();
		ArrayList<Object> thirdLineOnlyPrincipals = new ArrayList<>();
		thirdLine.add("Regression weight");
		thirdLineOnlyPrincipals.add("Regression weight");
		ArrayList<Object> fourthLineOnlyPrincipals = new ArrayList<>();
		fourthLineOnlyPrincipals.add(
				"Note : columns might be removed if they are equivalent to another one or if every trade has the same value.");

		for (int i : categoriesOrder) { // categoriesOrder can't be null
			if (isPrincipalCategory.containsKey(i)) {
				firstLineOnlyPrincipals.add(categories[i]);
				secondLineOnlyPrincipals.add(numberOf1[i]);
				thirdLineOnlyPrincipals.add(weights[i]);
			}
			firstLine.add(categories[i]);
			secondLine.add(numberOf1[i]);
			thirdLine.add(weights[i]);
		}

		content.add(firstLine);
		contentOnlyPrincipals.add(firstLineOnlyPrincipals);
		content.add(secondLine);
		contentOnlyPrincipals.add(secondLineOnlyPrincipals);
		content.add(thirdLine);
		contentOnlyPrincipals.add(thirdLineOnlyPrincipals);
		contentOnlyPrincipals.add(fourthLineOnlyPrincipals);

		ClusLogger.getInstance().writeInLog("- Logical Regression weights");
		return new Pair<>(contentOnlyPrincipals, content);
	}

	// This is for the tests, to export the comparison between real root cause
	// and obtained clusters.
	private ArrayList<ArrayList<Object>> exportComparisonToRC(String RCFile) {
		Nodes rcSeparated = RootCause.getRootCauses(RCFile, true);
		String RCFileName = RCFile.split(".csv")[0]; // the RCFile contains at
														// most one ".", for
														// ".csv".
		return exportComparison(nodes, fileName, rcSeparated, RCFileName);
	}

	// This function is to compare two Nodes on the same set of data. The trades
	// are recognized by their ID.
	private ArrayList<ArrayList<Object>> exportComparison(Nodes ns1, String name1, Nodes ns2, String name2) {

		ClusLogger.getInstance().writeInLog("Exporting comparison between " + name1 + " and " + name2 + " ...");
		ArrayList<ArrayList<Object>> content = new ArrayList<>(100);

		int[] nodesOrder1 = sortBySize(ns1);
		int[] nodesOrder2 = sortBySize(ns2);

		ArrayList<Object> firstLine = new ArrayList<>();
		ArrayList<Object> secondLine = new ArrayList<>();
		int size1 = 0;
		int size2 = 0;
		for (Node n : ns1.getNodes())
			size1 += n.getSize();
		for (Node n : ns2.getNodes())
			size2 += n.getSize();
		firstLine.add("Total");
		firstLine.add(size2);
		secondLine.add(size1);
		secondLine.add("Clusters");

		for (int i : nodesOrder2) {
			firstLine.add(ns2.getNodes().get(i).getSize());
		}
		for (int i : nodesOrder2) {
			if (ns2.getAlgorithm() == null)
				secondLine.add(((RootCauseNodes) ns2).getRootCauses()[i]);
			else
				secondLine.add((i));
		}
		content.add(firstLine);
		content.add(secondLine);

		for (int i : nodesOrder1) {
			ArrayList<Object> line = new ArrayList<>();
			line.add(ns1.getNodes().get(i).getSize());
			line.add((i+1));
			for (int j : nodesOrder2) {
				double percentage = ns1.getNodes().get(i).similarityPercentage(ns2.getNodes().get(j));
				if (NeurTools.Main.LOGALL)
					ClusLogger.getInstance().writeInLog("Node " + ns1.getNodes().get(i).getId() + " is " + percentage + "% in node "
							+ ns2.getNodes().get(j).getId());
				line.add(percentage);
			}
			content.add(line);
		}
		return content;
	}

	public static String tabToCsvLine(String[] tab) {
		StringBuilder toWrite = new StringBuilder();
		for (int i = 0; i < tab.length - 1; i++) {
			toWrite.append(tab[i]);
			toWrite.append(",");
		}
		toWrite.append(tab[tab.length - 1]);
		return toWrite.toString();
	}

	public static String tabToCsvLine(double[] tab) {
		StringBuilder toWrite = new StringBuilder();
		for (int i = 0; i < tab.length - 1; i++) {
			if (tab[i] <= 0.0001)
				tab[i] = 0;
			toWrite.append(tab[i]);
			toWrite.append(",");
		}
		toWrite.append(tab[tab.length - 1]);
		return toWrite.toString();
	}

	private static int[] sortBySize(Nodes nodes) {
		if (nodes.getNumberOfClusters() == 0) {
			return new int[] {};
		}
		nodes.updateNumberOfCluster();
		Integer[] tab = new Integer[nodes.getNumberOfClusters()];
		for (Node n : nodes.getNodes()) {
			tab[n.getId()] = n.getSize();
		}
		return Algorithm.argSort(tab);
	}

	public static void generateFileInResults(String path, ArrayList<StringBuilder> content) throws Exception {
		PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(path, false))); // false
																								// to
																								// init
																								// the
																								// file
		for (StringBuilder currentLine : content) {
			file.println(currentLine);
		}
		file.close();
	}

	// private static String[][] sortByLine(int lineNumber, int startingColumn,
	// String[][] content) {
	// String[][] res = new String[content.length][];
	// if (lineNumber < startingColumn || lineNumber > content.length - 1 ||
	// content[lineNumber] == null) {
	// Logger.getInstance().warning("Trying to sort by a line that does not exist, number " +
	// lineNumber);
	// return content;
	// } else if (!isProperMatrix(content)) {
	// Logger.getInstance().warning("Trying to sort not a proper matrix ");
	// return content;
	// } else {
	// try {
	// Double[] toArgSort = new Double[content[lineNumber].length -
	// startingColumn];
	// for (int i = 0; i < toArgSort.length; i++) {
	// toArgSort[i] = parseDouble(content[lineNumber][i + startingColumn]);
	// }
	// int[] newOrder = Algorithm.argSort(toArgSort);
	// for (int i = 0; i < content.length; i++) {
	// res[i] = new String[content[0].length];
	// }
	// for (int i = 0; i < content[0].length; i++) {
	// if (i < startingColumn) putColumn(content, res, i, i);
	// else putColumn(content, res, newOrder[i - startingColumn] +
	// startingColumn, i);
	// }
	// return res;
	// } catch (NumberFormatException e) {
	// Logger.getInstance().warning("Trying to sort a line that does not contains only
	// numbers");
	// return content;
	// }
	// }
	// }
	//
	// private static void putColumn(String[][] from, String[][] to, int where,
	// int index) {
	// for (int i = 0; i < from.length; i++) {
	// to[i][where] = from[i][index];
	// }
	// }
	//
	// private static boolean isProperMatrix(Object[][] a) {
	// if (a == null) return false;
	// if (a[0] == null) return false;
	// int size = a[0].length;
	// for (Object[] line : a) if (line.length != size) return false;
	// return true;
	// }

}
