package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public class MainConsole {

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        File textFile = new File("D:/texts/+The jungle book_Kipling.txt");
        String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

        boolean caseSensitive = true;
        boolean considerSentenceBounds = false;
        boolean useRange = true;
        int rangeSize = 2;

        String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, caseSensitive, considerSentenceBounds);
        Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, useRange, rangeSize);

        long end = System.currentTimeMillis();
        System.out.printf("Time %d ms\n", end - start);
    }

}
