package ExportTools;

import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import DataTool.ClusLogger;

class ExcelWriter {

    private int sheetIndex;
    private WritableWorkbook workbook;
    private String fileName;

    ExcelWriter(String outputFileName) {
        this.sheetIndex = 0;
        this.fileName = outputFileName;
        try {
            workbook = Workbook.createWorkbook(new File(outputFileName + ".xls"));
        } catch (IOException e) {
            ClusLogger.getInstance().error("Impossible to generate the file " + outputFileName + " : " + e.getMessage());
        }
    }

    void addSheetArray(ArrayList<Object[]> content, String sheetName) { // can write Strings, Integers and Doubles.

        ClusLogger.getInstance().writeInLog("Adding sheet " + sheetName + " to " + fileName + " ...");

        if (workbook == null) {
            ClusLogger.getInstance().warning("Trying to add sheet " + sheetName + " to non existent file.");
            ClusLogger.getInstance().writeInLog("Operation aborted.");
        } else if (content.size() > 65536) {
            ClusLogger.getInstance().warning("The sheet you are trying to export, " + sheetName + ", contains more than 65,536 rows which is the maximum of rows allowed in a .xls file. ");
            ClusLogger.getInstance().writeInLog("Please contact an administrator to ask for the development of the export in .xlsx (1,048,576 rows).");
            ClusLogger.getInstance().writeInLog("Operation aborted.");
        } else {
            // create an Excel sheet
            WritableSheet excelSheet = workbook.createSheet(sheetName, sheetIndex);
            sheetIndex++;

            for (int i = 0; i < content.size(); i++) {
                for (int j = 0; j < content.get(i).length; j++) {
                    try {
                        Label label = new Label(j, i, (String) content.get(i)[j]);
                        try {
                            excelSheet.addCell(label);
                        } catch (WriteException e) {
                            ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + e.getMessage());
                        }
                    } catch (java.lang.ClassCastException e) {
                        Number num;
                        try {
                            num = new Number(j, i, (Integer) content.get(i)[j]);
                            try {
                                excelSheet.addCell(num);
                            } catch (WriteException ex) {
                                ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + ex.getMessage());
                            }
                        } catch (java.lang.ClassCastException ex) {
                            try {
                                num = new Number(j, i, (Double) content.get(i)[j]);
                                try {
                                    excelSheet.addCell(num);
                                } catch (WriteException exe) {
                                    ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + exe.getMessage());
                                }
                            } catch (java.lang.ClassCastException exec) {
                                ClusLogger.getInstance().warning("Unknown type of data, not a String, Integer or Double : " + exec.getMessage());
                            }
                        }
                    }
                }
            }
            ClusLogger.getInstance().writeInLog("Done.");
        }
    }

    // To create a new sheet and fill it with content
    void addSheetArrayList(ArrayList<ArrayList<Object>> content, String sheetName) { // can write Strings, Integers and Doubles.
        if (workbook == null) {
            ClusLogger.getInstance().warning("Trying to add sheet " + sheetName + " to non existent file.");
        } else if (content != null) { // nothing is done if the content is null, the error is already displayed
            if (content.size() > 65536) {
                ClusLogger.getInstance().warning("The sheet you are trying to export, " + sheetName + ", in " + fileName + ", contains " + content.size() + " rows, which is more than the maximum of rows allowed in a .xls file (65,536).");
                ClusLogger.getInstance().writeInLog("Please contact an administrator to ask for the development of the export in .xlsx (1,048,576 rows).");
            }
            // create an Excel sheet
            WritableSheet excelSheet = workbook.createSheet(sheetName, sheetIndex);
            sheetIndex++;

            // Trying to write every data, warning if too many.
            boolean allReadyWarned = false;
            for (int i = 0; i < content.size() && i < 65535; i++) {
                if (content.get(i).size() > 256 && !allReadyWarned) {
                    ClusLogger.getInstance().warning("The sheet you are trying to export, " + sheetName + " in " + fileName + ", contains " + content.get(i).size() + " columns, which is more than the maximum of columns allowed in a .xls file (256). ");
                    ClusLogger.getInstance().writeInLog("Please contact an administrator to ask for the development of the export in .xlsx (16,384 columns).");
                    allReadyWarned = true;
                }
                for (int j = 0; j < content.get(i).size() && j < 255; j++) {
                    try {
                        Label label = new Label(j, i, (String) content.get(i).get(j));
                        try {
                            excelSheet.addCell(label);
                        } catch (WriteException e) {
                            ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + e.getMessage());
                        }
                    } catch (java.lang.ClassCastException e) {
                        Number num;
                        try {
                            num = new Number(j, i, (Integer) content.get(i).get(j));
                            try {
                                excelSheet.addCell(num);
                            } catch (WriteException ex) {
                                ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + ex.getMessage());
                            }
                        } catch (java.lang.ClassCastException ex) {
                            try {
                                num = new Number(j, i, (Double) content.get(i).get(j));
                                try {
                                    excelSheet.addCell(num);
                                } catch (WriteException exe) {
                                    ClusLogger.getInstance().warning("Impossible to write value row " + i + " column " + j + " : " + exe.getMessage());
                                }
                            } catch (java.lang.ClassCastException exec) {
                                ClusLogger.getInstance().warning("Unknown type of data, not a String, Integer or Double : " + exec.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    WritableWorkbook getWorkbook() {
        return workbook;
    }

    void closeFile() {
        if (workbook != null) {
            try {
                workbook.write();
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}