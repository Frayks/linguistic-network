package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.lib.api.constant.NGramType;
import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.CommonUtil;
import org.andyou.linguistic_network.lib.util.LinguisticNetworkUtil;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class MainConsole {

    public static void main(String[] args) throws IOException {
        File textFile = new File("D:/texts/+The jungle book_Kipling.txt");
        File stopWordsFile = new File("D:/texts/stop_words_english.txt");
        String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

        NGramType nGramType = NGramType.WORDS;
        int nGramSize = 1;
        boolean caseSensitive = true;
        boolean considerSentenceBounds = true;
        boolean useRange = true;
        int rangeSize = 2;
        boolean removeStopWords = true;

        long start = System.currentTimeMillis();
        String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, nGramType, caseSensitive, considerSentenceBounds);
        if (removeStopWords) {
            String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);
            String[] stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);
            elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords, null);
        }
        Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, useRange, rangeSize, null);
        Map<SWNode, Double> keywordStatisticsSmallWorld = LinguisticNetworkUtil.calcKeywordStatisticsSmallWorld(swNodeGraph, null);
        long end = System.currentTimeMillis();

        System.out.println("Spent time: " + CommonUtil.formatDuration(end - start));
        System.out.println("Element count: " + keywordStatisticsSmallWorld.size());
        List<Map.Entry<SWNode, Double>> entries = new ArrayList<>(keywordStatisticsSmallWorld.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        for (Map.Entry<SWNode, Double> entry : entries) {
            System.out.printf("%d\t%.20s\t%f\n", entry.getKey().getElement(), entry.getValue());
        }
    }

}
