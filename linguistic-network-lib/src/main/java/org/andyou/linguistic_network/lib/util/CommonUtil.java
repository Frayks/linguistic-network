package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.context.KeywordExtractionSmallWorldContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticMetricsContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.CDFNode;
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
        if (mainContext != null && mainContext.getSwNodeGraph() != null) {
            File file = new File(directory, "linguistic_network.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Element count: " + mainContext.getSwNodeGraph().size());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(mainContext.getSpentTime()));

            Set<SWNode> swNodeGraph = mainContext.getSwNodeGraph();
            List<SWNode> swNodes = new ArrayList<>(swNodeGraph);
            swNodes.sort(Comparator.comparingInt(SWNode::getFrequency).thenComparing(SWNode::getNeighborCount).thenComparing(SWNode::getElement).reversed());
            printWriter.printf("\nRank\tElement\tFrequency\tNeighbors count\n");
            for (int i = 0; i < swNodes.size(); i++) {
                SWNode swNode = swNodes.get(i);
                int rank = i + 1;
                printWriter.printf("%d\t%s\t%d\t%d\n", rank, swNode.getElement(), swNode.getFrequency(), swNode.getNeighborCount());
            }
            printWriter.close();
        }

        LinguisticMetricsContext linguisticMetricsContext = context.getLinguisticMetricsContext();
        if (mainContext != null && linguisticMetricsContext != null && linguisticMetricsContext.getCdfNodes() != null) {
            File file = new File(directory, "linguistic_metrics.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Average Clustering Coefficient: " + linguisticMetricsContext.getAverageClusteringCoefficient());
            printWriter.println("Average Path Length: " + linguisticMetricsContext.getAveragePathLength());
            printWriter.println("Average Neighbour Count: " + linguisticMetricsContext.getAverageNeighbourCount());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(linguisticMetricsContext.getSpentTime()));

            List<CDFNode> cdfNodes = linguisticMetricsContext.getCdfNodes();
            printWriter.printf("\nK\tN\tPDF\tCDF\n");
            for (CDFNode cdfNode : cdfNodes) {
                printWriter.printf("%d\t%d\t%f\t%f\n", cdfNode.getK(), cdfNode.getN(), cdfNode.getPdf(), cdfNode.getCdf());
            }
            printWriter.close();
        }

        KeywordExtractionSmallWorldContext keywordExtractionSmallWorldContext = context.getKeywordExtractionSmallWorldContext();
        if (mainContext != null && keywordExtractionSmallWorldContext != null && keywordExtractionSmallWorldContext.getKeywordStatistics() != null) {
            File file = new File(directory, "keyword_extraction_small_world.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println("Text file: " + mainContext.getTextFile().getAbsolutePath());
            printWriter.println("Spent time: " + CommonUtil.formatDuration(keywordExtractionSmallWorldContext.getSpentTime()));

            Map<SWNode, Double> keywordStatistics = keywordExtractionSmallWorldContext.getKeywordStatistics();
            List<Map.Entry<SWNode, Double>> entries = new ArrayList<>(keywordStatistics.entrySet());
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            printWriter.printf("\nRank\tElement\tCB\n");
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<SWNode, Double> entry = entries.get(i);
                int rank = i + 1;
                printWriter.printf("%d\t%s\t%f\n", rank, entry.getKey().getElement(), entry.getValue());
            }
            printWriter.close();
        }

    }

    public static void saveStatisticsToXlsxFile(File file, LinguisticNetworkContext context) throws IOException {
        FileUtils.forceDeleteOnExit(file);

        Workbook workbook = new XSSFWorkbook();
        CellStyle leftCellStyle = workbook.createCellStyle();
        leftCellStyle.setAlignment(HorizontalAlignment.LEFT);

        MainContext mainContext = context.getMainContext();
        if (mainContext != null && mainContext.getSwNodeGraph() != null) {
            Sheet sheet = workbook.createSheet("linguistic_network");

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("Rank");
            row4.createCell(1).setCellValue("Element");
            row4.createCell(2).setCellValue("Frequency");
            row4.createCell(3).setCellValue("Neighbors count");

            Set<SWNode> swNodeGraph = mainContext.getSwNodeGraph();
            List<SWNode> swNodes = new ArrayList<>(swNodeGraph);
            swNodes.sort(Comparator.comparingInt(SWNode::getFrequency).thenComparing(SWNode::getNeighborCount).thenComparing(SWNode::getElement).reversed());
            int rowShift = 5;
            for (int i = 0; i < swNodes.size(); i++) {
                SWNode swNode = swNodes.get(i);
                int rank = i + 1;
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(rank);
                row.createCell(1).setCellValue(swNode.getElement());
                row.createCell(2).setCellValue(swNode.getFrequency());
                row.createCell(3).setCellValue(swNode.getNeighborCount());
            }
            sheet.autoSizeColumn(1);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Element count");
            row1.createCell(1).setCellValue(mainContext.getSwNodeGraph().size());

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Spent time");
            row2.createCell(1).setCellValue(CommonUtil.formatDuration(mainContext.getSpentTime()));
        }

        LinguisticMetricsContext linguisticMetricsContext = context.getLinguisticMetricsContext();
        if (mainContext != null && linguisticMetricsContext != null && linguisticMetricsContext.getCdfNodes() != null) {
            Sheet sheet = workbook.createSheet("linguistic_metrics");

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Average Clustering Coefficient");
            row1.createCell(3).setCellValue(linguisticMetricsContext.getAverageClusteringCoefficient());

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Average Path Length");
            row2.createCell(3).setCellValue(linguisticMetricsContext.getAveragePathLength());

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Average Neighbour Count");
            row3.createCell(3).setCellValue(linguisticMetricsContext.getAverageNeighbourCount());

            Row row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("Spent time");
            row4.createCell(3).setCellValue(CommonUtil.formatDuration(linguisticMetricsContext.getSpentTime()));

            Row row6 = sheet.createRow(6);
            row6.createCell(0).setCellValue("K");
            row6.createCell(1).setCellValue("N");
            row6.createCell(2).setCellValue("PDF");
            row6.createCell(3).setCellValue("CDF");

            List<CDFNode> cdfNodes = linguisticMetricsContext.getCdfNodes();
            int rowShift = 7;
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
        if (mainContext != null && keywordExtractionSmallWorldContext != null && keywordExtractionSmallWorldContext.getKeywordStatistics() != null) {
            Sheet sheet = workbook.createSheet("keyword_extraction_small_world");

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Rank");
            row3.createCell(1).setCellValue("Element");
            row3.createCell(2).setCellValue("CB");

            Map<SWNode, Double> keywordStatistics = keywordExtractionSmallWorldContext.getKeywordStatistics();
            List<Map.Entry<SWNode, Double>> entries = new ArrayList<>(keywordStatistics.entrySet());
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            int rowShift = 4;
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<SWNode, Double> entry = entries.get(i);
                int rank = i + 1;
                Row row = sheet.createRow(rowShift + i);
                row.createCell(0).setCellValue(rank);
                row.createCell(1).setCellValue(entry.getKey().getElement());
                row.createCell(2).setCellValue(entry.getValue());
            }
            sheet.autoSizeColumn(1);

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Text file");
            row0.createCell(1).setCellValue(mainContext.getTextFile().getAbsolutePath());

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Spent time");
            row1.createCell(1).setCellValue(CommonUtil.formatDuration(keywordExtractionSmallWorldContext.getSpentTime()));
        }

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

        if (workbook.getNumberOfSheets() > 0) {
            OutputStream outputStream = Files.newOutputStream(file.toPath());
            workbook.write(outputStream);
            outputStream.close();
        }
        workbook.close();
    }

}
