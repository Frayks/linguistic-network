package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextTokenizerUtil {

    public static String[][] createElementGroups(String text, boolean caseSensitive, boolean considerSentenceBounds) {
        if (!caseSensitive) {
            text = text.toLowerCase();
        }

        if (considerSentenceBounds) {
            String[] sentences = splitIntoSentences(text);
            return splitIntoWordGroups(sentences);
        } else {
            return new String[][]{splitIntoWords(text)};
        }
    }

    public static String[] splitIntoSentences(String text) {
        return text.split("(\\.\\.\\.)|[.!?…]");
    }

    public static String[] splitIntoWords(String text) {
        text = text.replaceAll("[\\p{C}\\p{Z}]+|-{2,}", " ");
        text = text.replaceAll("(^[\\p{S}\\p{P}]+)|([\\p{S}\\p{P}]+$)", "");
        text = text.replaceAll("([\\p{L}\\p{N}])[\\p{S}\\p{P}]+($| )", "$1 ");
        text = text.replaceAll("(^| )[\\p{S}\\p{P}]+([\\p{L}\\p{N}])", " $2");
        text = text.replaceAll(" [\\p{S}\\p{P}]+ ", " ");
        return StringUtils.split(text, " ");
    }

    public static String[][] splitIntoWordGroups(String[] sentences) {
        String[][] wordGroups = new String[sentences.length][];

        for (int i = 0; i < sentences.length; i++) {
            wordGroups[i] = splitIntoWords(sentences[i]);
        }

        return wordGroups;
    }

    public static String[][] removeStopWords(String[][] wordGroups, String[] stopWords, ProgressBarProcessor progressBarProcessor) {
        List<String[]> filteredWordGroups = new ArrayList<>();
        List<String> stopWordList = Arrays.asList(stopWords);
        if (progressBarProcessor != null) {
            int stepsCount = Arrays.stream(wordGroups).mapToInt(elementGroup -> elementGroup.length).sum();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        for (String[] words : wordGroups) {
            List<String> filteredWords = new ArrayList<>();

            for (String word : words) {
                if (stopWordList.stream().noneMatch(word::equalsIgnoreCase)) {
                    filteredWords.add(word);
                }
                if (progressBarProcessor != null) {
                    progressBarProcessor.walk();
                }
            }

            if (!filteredWords.isEmpty()) {
                filteredWordGroups.add(filteredWords.toArray(new String[0]));
            }
        }

        return filteredWordGroups.toArray(new String[0][]);
    }

}
