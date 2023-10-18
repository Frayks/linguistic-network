import org.andyou.linguistic_network.api.node.SWNode;
import org.andyou.linguistic_network.util.LinguisticNetworkUtil;
import org.andyou.linguistic_network.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.util.TextTokenizerUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        File stopWordsFile = new File("D:/texts/stop_words_english.txt");
        String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);

        File textFile = new File("D:/texts/+The jungle book_Kipling.txt");
        String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

        boolean caseSensitive = false;
        boolean considerSentenceBounds = true;
        Integer range = 1;
        boolean removeStopWords = true;
        int filterFrequency = 4;

        String[] stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);

        String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, caseSensitive, considerSentenceBounds);

        if (removeStopWords) {
            elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords);
        }

        Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, range);

        SWNodeGraphUtil.filterByFrequency(swNodeGraph, filterFrequency);

        Map<SWNode, Double> keywordStatistics = LinguisticNetworkUtil.calcKeywordStatisticsSmallWorld(swNodeGraph);

        List<Map.Entry<SWNode, Double>> list = new ArrayList<>(keywordStatistics.entrySet());
        list.sort(Comparator.comparing(Map.Entry<SWNode, Double>::getValue).reversed());

        File file = new File("C:/Users/Andrew/Desktop/results/keywordStatisticsSmallWorld.txt");
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (Map.Entry<SWNode, Double> entry : list) {
            printWriter.printf("%-20s%f\n", entry.getKey().getElement(), entry.getValue());
        }
        printWriter.close();
    }

}
