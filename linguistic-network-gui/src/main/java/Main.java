import org.andyou.linguistic_network.api.node.CDFNode;
import org.andyou.linguistic_network.api.node.SWNode;
import org.andyou.linguistic_network.util.BFSUtil;
import org.andyou.linguistic_network.util.SWNodeUtil;
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
        File textFile = new File("D:/texts/test1.txt");
        String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

        boolean caseSensitive = false;
        boolean considerSentenceBounds = true;
        Integer range = 2;

        String[][] elementGroups = createElementGroups(text, caseSensitive, considerSentenceBounds);

        long start = System.currentTimeMillis();
        Set<SWNode> swNodes = createSWNodes(elementGroups, range);
        long end = System.currentTimeMillis();
        System.out.printf("createSWNodes: %d ms\n", end - start);

        start = System.currentTimeMillis();
        List<CDFNode> cdfNodes = createCDFNodes(swNodes);
        end = System.currentTimeMillis();
        System.out.printf("createCDFNodes: %d ms\n", end - start);

        start = System.currentTimeMillis();
        double averageClusteringCoefficient = calcAverageClusteringCoefficient(swNodes);
        end = System.currentTimeMillis();
        System.out.printf("calcAverageClusteringCoefficient: %d ms\n", end - start);
        System.out.println(averageClusteringCoefficient);

        start = System.currentTimeMillis();
        double averagePathLength = calcAveragePathLength(swNodes);
        end = System.currentTimeMillis();
        System.out.printf("calcAveragePathLength: %d ms\n", end - start);
        System.out.println(averagePathLength);

        start = System.currentTimeMillis();
        double averageNeighbourCount = calcAverageNeighbourCount(swNodes);
        end = System.currentTimeMillis();
        System.out.printf("calcAverageNeighbourCount: %d ms\n", end - start);
        System.out.println(averageNeighbourCount);

        start = System.currentTimeMillis();
        Map<SWNode, Double> keywordStatistics = calcKeywordStatisticsSmallWorld(swNodes);
        end = System.currentTimeMillis();
        System.out.printf("calcKeywordStatisticsSmallWorld: %d ms\n", end - start);

        List<Map.Entry<SWNode, Double>> list = new ArrayList<>(keywordStatistics.entrySet());
        list.sort(Comparator.comparing(Map.Entry<SWNode, Double>::getValue).reversed());

        File file = new File("results.txt");
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (Map.Entry<SWNode, Double> entry : list) {
            printWriter.printf("%-20s%f\n", entry.getKey().getElement(), entry.getValue());
        }
        printWriter.close();

    }

    private static Map<SWNode, Double> calcKeywordStatisticsSmallWorld(Set<SWNode> swNodes) {
        Map<SWNode, Double> keywordStatistics = new HashMap<>();

        swNodes = SWNodeUtil.clone(swNodes);
        List<SWNode> swNodeList = new ArrayList<>(swNodes);

        System.out.println(swNodes.size());

        int count = 0;
        for (SWNode swNode : swNodeList) {
            SWNodeUtil.removeSWNode(swNodes, swNode);

            int m = swNodes.stream().mapToInt(v -> v.getNeighbors().size()).sum();
            Set<SWNode> randomSWNodes = SWNodeUtil.generateRandomSWNodes(swNodes, m);

            double cSW = calcAverageClusteringCoefficient(swNodes);
            double lSW = calcAveragePathLength(swNodes);
            double cRand = calcAverageClusteringCoefficient(randomSWNodes);
            double lRand = calcAveragePathLength(randomSWNodes);

            double mu = (cSW / lSW) / (cRand / lRand);
            keywordStatistics.put(swNode, mu);

            if (count % 10 == 0) {
                System.out.println(count);
            }

            count++;
            SWNodeUtil.addSWNode(swNodes, swNode);
        }

        return keywordStatistics;
    }


    private static List<CDFNode> createCDFNodes(Set<SWNode> swNodes) {
        Map<Integer, CDFNode> cdfNodeMap = new HashMap<>();

        for (SWNode swNode : swNodes) {
            int neighborCount = swNode.getNeighbors().size();
            CDFNode cdfNode = cdfNodeMap.computeIfAbsent(neighborCount, key -> new CDFNode(key, 0));
            cdfNode.setN(cdfNode.getN() + 1);
        }

        List<CDFNode> cdfNodes = new ArrayList<>(cdfNodeMap.values());
        cdfNodes.sort(Comparator.comparingInt(CDFNode::getK));

        int swNodesCount = swNodes.size();
        for (int i = 0; i < cdfNodes.size(); i++) {
            CDFNode cdfNode = cdfNodes.get(i);
            cdfNode.setPdf((double) cdfNode.getN() / swNodesCount);
            if (i == 0) {
                cdfNode.setCdf(1);
            } else {
                CDFNode previousCDFNode = cdfNodes.get(i - 1);
                cdfNode.setCdf(previousCDFNode.getCdf() - previousCDFNode.getPdf());
            }
        }

        return cdfNodes;
    }

    private static double calcAverageClusteringCoefficient(Set<SWNode> swNodes) {
        return swNodes.parallelStream().mapToDouble(swNode -> {
            if (swNode.getNeighbors().size() > 1) {
                int n = 0;
                Set<SWNode> neighbors = swNode.getNeighbors();
                Set<SWNode> visited = new HashSet<>();
                for (SWNode neighbor : neighbors) {
                    Set<SWNode> relatedElements = new HashSet<>(neighbor.getNeighbors());

                    relatedElements.retainAll(neighbors);
                    relatedElements.removeAll(visited);
                    n += relatedElements.size();

                    visited.add(neighbor);
                }

                int k = swNode.getNeighbors().size();
                return (double) (2 * n) / (k * (k - 1));
            } else {
                return 0;
            }
        }).average().orElse(0);
    }

    private static double calcAveragePathLength(Set<SWNode> swNodes) {
        return swNodes.parallelStream().mapToDouble(swNode -> {
            List<Integer> pathLengths = BFSUtil.calcPathLengths(swNode);
            return pathLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
        }).average().orElse(0);
    }

    private static double calcAverageNeighbourCount(Set<SWNode> swNodes) {
        return swNodes.parallelStream().mapToInt(swNode -> swNode.getNeighbors().size()).average().orElse(0);
    }

    private static String[][] createElementGroups(String text, boolean caseSensitive, boolean considerSentenceBounds) {
        if (!caseSensitive) {
            text = text.toLowerCase();
        }

        if (considerSentenceBounds) {
            String[] sentences = TextTokenizerUtil.splitIntoSentences(text);
            return TextTokenizerUtil.splitIntoWordGroups(sentences);
        } else {
            return new String[][]{TextTokenizerUtil.splitIntoWords(text)};
        }
    }

    private static Set<SWNode> createSWNodes(String[][] elementGroups, Integer range) {
        Map<String, SWNode> swNodeMap = new HashMap<>();

        for (String[] elementGroup : elementGroups) {
            for (String element : elementGroup) {
                SWNode swNode = swNodeMap.computeIfAbsent(element, key -> new SWNode(key, 0));
                swNode.setFrequency(swNode.getFrequency() + 1);
            }
            for (int i = 0; i < elementGroup.length; i++) {
                String element = elementGroup[i];

                List<String> neighborElements;
                if (range == null) {
                    neighborElements = getNeighborElements(elementGroup, i);
                } else {
                    neighborElements = getNeighborElements(elementGroup, i, range);
                }

                SWNode swNode = swNodeMap.get(element);
                for (String neighborElement : neighborElements) {
                    swNode.getNeighbors().add(swNodeMap.get(neighborElement));
                }
            }
        }

        return new HashSet<>(swNodeMap.values());
    }

    private static List<String> getNeighborElements(String[] elementGroup, int index) {
        List<String> neighborElements = new ArrayList<>();

        String element = elementGroup[index];
        for (String currentElement : elementGroup) {
            if (!element.equals(currentElement)) {
                neighborElements.add(currentElement);
            }
        }

        return neighborElements;
    }

    private static List<String> getNeighborElements(String[] elementGroup, int index, int range) {
        List<String> neighborElements = new ArrayList<>();
        int startIndex = Math.max(0, index - range);
        int endIndex = Math.min(elementGroup.length - 1, index + range);

        String element = elementGroup[index];
        for (int i = startIndex; i <= endIndex; i++) {
            String currentElement = elementGroup[i];
            if (!element.equals(currentElement)) {
                neighborElements.add(currentElement);
            }
        }

        return neighborElements;
    }


}
