package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.constant.BoundsType;
import org.andyou.linguistic_network.lib.api.constant.NGramType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextTokenizerUtil {

    // https://www.regular-expressions.info/unicode.html

    public static String[][] createElementGroups(String text, NGramType nGramType, boolean caseSensitive, boolean includeSpaces, BoundsType boundsType, String sentenceDelimiters) {
        if (!caseSensitive) {
            text = text.toLowerCase();
        }

        String[] parts = new String[0];
        if (BoundsType.ABSENT.equals(boundsType)) {
            parts = new String[]{text};
        } else if (BoundsType.WORD.equals(boundsType)) {
            parts = splitIntoWords(text);
        } else if (BoundsType.SENTENCE.equals(boundsType)) {
            parts = splitIntoSentences(text, sentenceDelimiters);
        }

        return splitIntoElementGroups(parts, nGramType, includeSpaces);
    }

    public static String[] splitIntoSentences(String text, String sentenceDelimiters) {
        return StringUtils.split(text, sentenceDelimiters);
    }

    public static String[][] splitIntoElementGroups(String[] parts, NGramType nGramType, boolean includeSpaces) {
        String[][] elementGroups = new String[parts.length][];

        if (NGramType.WORDS.equals(nGramType)) {
            for (int i = 0; i < parts.length; i++) {
                elementGroups[i] = splitIntoWords(parts[i]);
            }
        } else if (NGramType.LETTERS_AND_NUMBERS.equals(nGramType)) {
            for (int i = 0; i < parts.length; i++) {
                elementGroups[i] = splitIntoLettersAndNumbers(parts[i]);
            }
        } else if (NGramType.SYMBOLS.equals(nGramType)) {
            for (int i = 0; i < parts.length; i++) {
                elementGroups[i] = splitIntoSymbols(parts[i], includeSpaces);
            }
        }

        return elementGroups;
    }

    public static String[] splitIntoWords(String text) {
        text = text.replaceAll("[\\p{C}\\p{Z}]+|-{2,}", " ");
        text = text.replaceAll("(^[\\p{S}\\p{P}]+)|([\\p{S}\\p{P}]+$)", "");
        text = text.replaceAll("([\\p{L}\\p{N}])[\\p{S}\\p{P}]+($| )", "$1 ");
        text = text.replaceAll("(^| )[\\p{S}\\p{P}]+([\\p{L}\\p{N}])", " $2");
        text = text.replaceAll(" [\\p{S}\\p{P}]+ ", " ");
        return StringUtils.split(text, " ");
    }

    public static String[] splitIntoLettersAndNumbers(String text) {
        text = text.replaceAll("[^\\p{L}\\p{N}]+", "");
        return text.split("");
    }

    public static String[] splitIntoSymbols(String text, boolean includeSpaces) {
        text = text.replaceAll("[\t\\p{Zs}]", " ");
        if (!includeSpaces) {
            text = text.replace(" ", "");
        }
        text = text.replaceAll("\\p{C}", "");
        return text.split("");
    }

    public static void combineIntoNGrams(String[][] elementGroups, int nGramSize, String separator) {
        for (int i = 0; i < elementGroups.length; i++) {
            elementGroups[i] = combineIntoNGrams(elementGroups[i], nGramSize, separator);
        }
    }

    public static String[] combineIntoNGrams(String[] elements, int nGramSize, String separator) {
        int normalizedNGramSize = Math.min(elements.length, nGramSize);
        List<String> nGrams = new ArrayList<>();
        for (int i = 0; i <= elements.length - normalizedNGramSize; i++) {
            nGrams.add(String.join(separator, Arrays.copyOfRange(elements, i, i + normalizedNGramSize)));
        }
        return nGrams.toArray(new String[0]);
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
