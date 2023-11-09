package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.context.KeywordExtractionSmallWorldContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticMetricsContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.CDFNode;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CommonUtil {

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, "HH:mm:ss.SSS", true);
    }

    public static void saveStatisticsToTextFiles(File directory, LinguisticNetworkContext context) throws IOException {
        if (directory.exists()) {
            FileUtils.cleanDirectory(directory);
        } else {
            directory.mkdir();
        }

        MainContext mainContext = context.getMainContext();
        if (mainContext != null && mainContext.getElementNodeGraph() != null) {
            File file = new File(directory, "linguistic_network.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Element count: " + mainContext.getElementNodeGraph().size());
            printWriter.println("Average Clustering Coefficient: " + mainContext.getAverageClusteringCoefficient());
            printWriter.println("Average Path Length: " + mainContext.getAveragePathLength());
            printWriter.println("Average Neighbour Count: " + mainContext.getAverageNeighbourCount());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(mainContext.getSpentTime()));

            Set<ElementNode> elementNodeGraph = mainContext.getElementNodeGraph();
            List<ElementNode> elementNodes = ElementNodeGraphUtil.sortAndSetIndex(elementNodeGraph);
            printWriter.println("Index\tElement\tFrequency\tClustering Coefficient\tAvg. Path Length\tNeighbor Count");
            for (ElementNode elementNode : elementNodes) {
                printWriter.printf("%d\t%s\t%d\t%f\t%f\t%d\n", elementNode.getIndex(), elementNode.getElement(), elementNode.getFrequency(), elementNode.getClusteringCoefficient(), elementNode.getAveragePathLength(), elementNode.getNeighborCount());
            }
            printWriter.close();
        }

        LinguisticMetricsContext linguisticMetricsContext = context.getLinguisticMetricsContext();
        if (mainContext != null && linguisticMetricsContext != null && linguisticMetricsContext.getCdfNodes() != null) {
            File file = new File(directory, "linguistic_metrics.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(linguisticMetricsContext.getSpentTime()));

            List<CDFNode> cdfNodes = linguisticMetricsContext.getCdfNodes();
            printWriter.println("\nK\tN\tPDF\tCDF");
            for (CDFNode cdfNode : cdfNodes) {
                printWriter.printf("%d\t%d\t%f\t%f\n", cdfNode.getK(), cdfNode.getN(), cdfNode.getPdf(), cdfNode.getCdf());
            }
            printWriter.close();
        }

        KeywordExtractionSmallWorldContext keywordExtractionSmallWorldContext = context.getKeywordExtractionSmallWorldContext();
        if (mainContext != null && keywordExtractionSmallWorldContext != null && keywordExtractionSmallWorldContext.getSwNodes() != null) {
            File file = new File(directory, "keyword_extraction_small_world.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(keywordExtractionSmallWorldContext.getSpentTime()));

            List<SWNode> swNodes = keywordExtractionSmallWorldContext.getSwNodes();
            swNodes.sort(Comparator.comparingDouble(SWNode::getContribution1)
                    .thenComparing(swNode -> swNode.getElementNode().getNeighborCount())
                    .thenComparing(swNode -> swNode.getElementNode().getFrequency())
                    .thenComparing(swNode -> swNode.getElementNode().getElement())
                    .reversed());
            printWriter.println("Rank\tIndex\tElement\tFrequency\tNeighbors count\tCB1\tCB1 Adj.\tCB1 Norm.\tCB2\tCB2 Norm.\tCB3\tCB3 Adj.\tCB3 Norm.");
            for (int i = 0; i < swNodes.size(); i++) {
                SWNode swNode = swNodes.get(i);
                int rank = i + 1;
                printWriter.printf("%d\t%d\t%s\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",
                        rank,
                        swNode.getElementNode().getIndex(),
                        swNode.getElementNode().getElement(),
                        swNode.getElementNode().getFrequency(),
                        swNode.getElementNode().getNeighborCount(),
                        swNode.getContribution1(),
                        swNode.getAdjustedContribution1(),
                        swNode.getNormalizedContribution1(),
                        swNode.getContribution2(),
                        swNode.getNormalizedContribution2(),
                        swNode.getContribution3(),
                        swNode.getAdjustedContribution3(),
                        swNode.getNormalizedContribution3());
            }
            printWriter.close();
        }
    }

    public static void saveStatisticsToTextFile(File file, ElementNode elementNode) throws IOException {
        FileUtils.forceDeleteOnExit(file);

        if (elementNode != null) {
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Element: " + elementNode.getElement());

            List<Map.Entry<ElementNode, Integer>> neighbors = new ArrayList<>(elementNode.getNeighbors().entrySet());
            neighbors.sort(Map.Entry.<ElementNode, Integer>comparingByValue().reversed());
            printWriter.println("Index\tNeighbor\tMultiplicity");
            for (Map.Entry<ElementNode, Integer> neighbor : neighbors) {
                printWriter.printf("%d\t%s\t%d\n", neighbor.getKey().getIndex(), neighbor.getKey().getElement(), neighbor.getValue());
            }
            printWriter.close();
        }
    }

    public static void saveStatisticsToXlsxFile(File file, LinguisticNetworkContext context) throws IOException {
        FileUtils.forceDeleteOnExit(file);

        Workbook workbook = new XSSFWorkbook();

        MainContext mainContext = context.getMainContext();
        if (mainContext != null && mainContext.getElementNodeGraph() != null) {
            Sheet sheet = workbook.createSheet("linguistic_network");

            Row row6 = sheet.createRow(6);
            row6.createCell(0).setCellValue("Index");
            row6.createCell(1).setCellValue("Element");
            row6.createCell(2).setCellValue("Frequency");
            row6.createCell(3).setCellValue("Clustering Coefficient");
            row6.createCell(4).setCellValue("Avg. Path Length");
            row6.createCell(5).setCellValue("Neighbor Count");

            Set<ElementNode> elementNodeGraph = mainContext.getElementNodeGraph();
            List<ElementNode> elementNodes = ElementNodeGraphUtil.sortAndSetIndex(elementNodeGraph);
            int rowShift = 7;
            for (int i = 0; i < elementNodes.size(); i++) {
                ElementNode elementNode = elementNodes.get(i);
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(elementNode.getIndex());
                row.createCell(1).setCellValue(elementNode.getElement());
                row.createCell(2).setCellValue(elementNode.getFrequency());
                row.createCell(3).setCellValue(elementNode.getClusteringCoefficient());
                row.createCell(4).setCellValue(elementNode.getAveragePathLength());
                row.createCell(5).setCellValue(elementNode.getNeighborCount());
            }
            sheet.autoSizeColumn(1);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Element count");
            row1.createCell(1).setCellValue(mainContext.getElementNodeGraph().size());

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Average Clustering Coefficient");
            row2.createCell(1).setCellValue(mainContext.getAverageClusteringCoefficient());

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Average Path Length");
            row3.createCell(1).setCellValue(mainContext.getAveragePathLength());

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("Average Neighbour Count");
            row4.createCell(1).setCellValue(mainContext.getAverageNeighbourCount());

            Row row5 = sheet.createRow(5);
            row5.createCell(0).setCellValue("Spent time");
            row5.createCell(1).setCellValue(CommonUtil.formatDuration(mainContext.getSpentTime()));
        }

        LinguisticMetricsContext linguisticMetricsContext = context.getLinguisticMetricsContext();
        if (mainContext != null && linguisticMetricsContext != null && linguisticMetricsContext.getCdfNodes() != null) {
            Sheet sheet = workbook.createSheet("linguistic_metrics");

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Spent time");
            row1.createCell(1).setCellValue(CommonUtil.formatDuration(linguisticMetricsContext.getSpentTime()));

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("K");
            row3.createCell(1).setCellValue("N");
            row3.createCell(2).setCellValue("PDF");
            row3.createCell(3).setCellValue("CDF");

            List<CDFNode> cdfNodes = linguisticMetricsContext.getCdfNodes();
            int rowShift = 4;
            for (int i = 0; i < cdfNodes.size(); i++) {
                CDFNode cdfNode = cdfNodes.get(i);
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(cdfNode.getK());
                row.createCell(1).setCellValue(cdfNode.getN());
                row.createCell(2).setCellValue(cdfNode.getPdf());
                row.createCell(3).setCellValue(cdfNode.getCdf());
            }
        }

        KeywordExtractionSmallWorldContext keywordExtractionSmallWorldContext = context.getKeywordExtractionSmallWorldContext();
        if (mainContext != null && keywordExtractionSmallWorldContext != null && keywordExtractionSmallWorldContext.getSwNodes() != null) {
            Sheet sheet = workbook.createSheet("keyword_extraction_small_world");

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Rank");
            row3.createCell(1).setCellValue("Index");
            row3.createCell(2).setCellValue("Element");
            row3.createCell(3).setCellValue("Frequency");
            row3.createCell(4).setCellValue("Neighbors count");
            row3.createCell(5).setCellValue("CB1");
            row3.createCell(6).setCellValue("CB1 Adj.");
            row3.createCell(7).setCellValue("CB1 Norm.");
            row3.createCell(8).setCellValue("CB2");
            row3.createCell(9).setCellValue("CB2 Norm.");
            row3.createCell(10).setCellValue("CB3");
            row3.createCell(11).setCellValue("CB3 Adj.");
            row3.createCell(12).setCellValue("CB3 Norm.");

            List<SWNode> swNodes = keywordExtractionSmallWorldContext.getSwNodes();
            swNodes.sort(Comparator.comparingDouble(SWNode::getContribution1)
                    .thenComparing(swNode -> swNode.getElementNode().getNeighborCount())
                    .thenComparing(swNode -> swNode.getElementNode().getFrequency())
                    .thenComparing(swNode -> swNode.getElementNode().getElement())
                    .reversed());
            int rowShift = 4;
            for (int i = 0; i < swNodes.size(); i++) {
                SWNode swNode = swNodes.get(i);
                int rank = i + 1;
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(rank);
                row.createCell(1).setCellValue(swNode.getElementNode().getIndex());
                row.createCell(2).setCellValue(swNode.getElementNode().getElement());
                row.createCell(3).setCellValue(swNode.getElementNode().getFrequency());
                row.createCell(4).setCellValue(swNode.getElementNode().getNeighborCount());
                row.createCell(5).setCellValue(swNode.getContribution1());
                row.createCell(6).setCellValue(swNode.getAdjustedContribution1());
                row.createCell(7).setCellValue(swNode.getNormalizedContribution1());
                row.createCell(8).setCellValue(swNode.getContribution2());
                row.createCell(9).setCellValue(swNode.getNormalizedContribution2());
                row.createCell(10).setCellValue(swNode.getContribution3());
                row.createCell(11).setCellValue(swNode.getAdjustedContribution3());
                row.createCell(12).setCellValue(swNode.getNormalizedContribution3());
            }
            sheet.autoSizeColumn(2);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Spent time");
            row1.createCell(1).setCellValue(CommonUtil.formatDuration(keywordExtractionSmallWorldContext.getSpentTime()));
        }

        setLeftCellsStyle(workbook);

        if (workbook.getNumberOfSheets() > 0) {
            OutputStream outputStream = Files.newOutputStream(file.toPath());
            workbook.write(outputStream);
            outputStream.close();
        }
        workbook.close();
    }

    public static void saveStatisticsToXlsxFile(File file, ElementNode elementNode) throws IOException {
        FileUtils.forceDeleteOnExit(file);

        Workbook workbook = new XSSFWorkbook();

        if (elementNode != null) {
            Sheet sheet = workbook.createSheet("element_info");

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Index");
            row3.createCell(1).setCellValue("Neighbor");
            row3.createCell(2).setCellValue("Multiplicity");

            List<Map.Entry<ElementNode, Integer>> neighbors = new ArrayList<>(elementNode.getNeighbors().entrySet());
            neighbors.sort(Map.Entry.<ElementNode, Integer>comparingByValue().reversed());
            int rowShift = 4;
            for (int i = 0; i < neighbors.size(); i++) {
                Map.Entry<ElementNode, Integer> neighbor = neighbors.get(i);
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(neighbor.getKey().getIndex());
                row.createCell(1).setCellValue(neighbor.getKey().getElement());
                row.createCell(2).setCellValue(neighbor.getValue());
            }
            sheet.autoSizeColumn(1);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Index");
            row0.createCell(1).setCellValue(elementNode.getIndex());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Element");
            row1.createCell(1).setCellValue(elementNode.getElement());
        }

        setLeftCellsStyle(workbook);

        if (workbook.getNumberOfSheets() > 0) {
            OutputStream outputStream = Files.newOutputStream(file.toPath());
            workbook.write(outputStream);
            outputStream.close();
        }
        workbook.close();
    }

    private static void setLeftCellsStyle(Workbook workbook) {
        CellStyle leftCellStyle = workbook.createCellStyle();
        leftCellStyle.setAlignment(HorizontalAlignment.LEFT);
        for (Iterator<Sheet> sheetIterator = workbook.sheetIterator(); sheetIterator.hasNext(); ) {
            Sheet sheet = sheetIterator.next();
            for (Iterator<Row> rowIterator = sheet.rowIterator(); rowIterator.hasNext(); ) {
                Row row = rowIterator.next();
                for (Iterator<Cell> cellIterator = row.cellIterator(); cellIterator.hasNext(); ) {
                    Cell cell = cellIterator.next();
                    cell.setCellStyle(leftCellStyle);
                }
            }
        }
    }

}
