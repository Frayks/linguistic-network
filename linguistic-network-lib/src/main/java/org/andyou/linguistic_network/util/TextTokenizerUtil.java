package org.andyou.linguistic_network.util;

import org.apache.commons.lang3.StringUtils;

public class TextTokenizerUtil {

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

}
