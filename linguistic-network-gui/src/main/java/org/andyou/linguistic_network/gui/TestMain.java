package org.andyou.linguistic_network.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        String[] elements = new String[4];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = String.valueOf(i);
        }
        int nGramSize = 5;

        List<String> nGrams = new ArrayList<>();
        int normalizedNGramSize = Math.min(elements.length, nGramSize);
        for (int i = 0; i <= elements.length - normalizedNGramSize; i++) {
            System.out.println(String.join(" ", Arrays.copyOfRange(elements, i, i + normalizedNGramSize)));
        }
    }

}
