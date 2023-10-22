package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.node.CDFNode;
import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.*;

public class LinguisticNetworkUtil {

    public static Map<SWNode, Double> calcKeywordStatisticsSmallWorld(Set<SWNode> swNodeGraph) {
        swNodeGraph = SWNodeGraphUtil.clone(swNodeGraph);
        Map<SWNode, Double> keywordStatistics = new HashMap<>();

        int swNodeGraphSize = swNodeGraph.size();
        double gl = calcAveragePathLength(swNodeGraph, 0, null);

        System.out.println(swNodeGraph.size());

        int counter = 0;
        List<SWNode> swNodes = new ArrayList<>(swNodeGraph);
        for (SWNode swNode : swNodes) {
            SWNodeGraphUtil.removeSWNode(swNodeGraph, swNode);

            double l = calcAveragePathLength(swNodeGraph, swNodeGraphSize, null);
            double sw = l - gl;
            keywordStatistics.put(swNode, sw);

            SWNodeGraphUtil.addSWNode(swNodeGraph, swNode);

            counter++;
            if (counter % 10 == 0) {
                System.out.println(counter);
            }
        }

        return keywordStatistics;
    }

    public static List<CDFNode> calcCDFNodes(Set<SWNode> swNodeGraph, ProgressBarProcessor progressBarProcessor) {
        Map<Integer, CDFNode> cdfNodeMap = new HashMap<>();

        for (SWNode swNode : swNodeGraph) {
            int neighborCount = swNode.getNeighborCount();
            CDFNode cdfNode = cdfNodeMap.computeIfAbsent(neighborCount, key -> new CDFNode(key, 0));
            cdfNode.setN(cdfNode.getN() + 1);
        }

        List<CDFNode> cdfNodes = new ArrayList<>(cdfNodeMap.values());
        cdfNodes.sort(Comparator.comparingInt(CDFNode::getK));

        if (progressBarProcessor != null) {
            int stepsCount = cdfNodes.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        for (int i = 0; i < cdfNodes.size(); i++) {
            CDFNode cdfNode = cdfNodes.get(i);
            cdfNode.setPdf((double) cdfNode.getN() / swNodeGraph.size());
            if (i == 0) {
                cdfNode.setCdf(1);
            } else {
                CDFNode previousCDFNode = cdfNodes.get(i - 1);
                cdfNode.setCdf(previousCDFNode.getCdf() - previousCDFNode.getPdf());
            }

            if (progressBarProcessor != null) {
                progressBarProcessor.walk();
            }
        }

        return cdfNodes;
    }

    public static double calcAverageClusteringCoefficient(Set<SWNode> swNodeGraph, ProgressBarProcessor progressBarProcessor) {
        if (progressBarProcessor != null) {
            int stepsCount = swNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        return swNodeGraph.parallelStream()
                .mapToDouble(swNode -> {
                    try {
                        if (swNode.getNeighborCount() > 1) {
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

                            int k = swNode.getNeighborCount();
                            return (double) (2 * n) / (k * (k - 1));
                        } else {
                            return 0;
                        }
                    } finally {
                        if (progressBarProcessor != null) {
                            progressBarProcessor.walk();
                        }
                    }
                })
                .average().orElse(0);
    }

    public static double calcAveragePathLength(Set<SWNode> swNodeGraph, int maxLength, ProgressBarProcessor progressBarProcessor) {
        if (progressBarProcessor != null) {
            int stepsCount = swNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        return swNodeGraph.parallelStream()
                .mapToDouble(swNode -> {
                    try {
                        List<Integer> pathLengths = BFSUtil.calcPathLengths(swNode);

                        int missingConnectionsNumber = swNodeGraph.size() - pathLengths.size() - 1;
                        pathLengths.addAll(Collections.nCopies(missingConnectionsNumber, maxLength));

                        return pathLengths.stream()
                                .mapToInt(Integer::intValue)
                                .average().orElse(0);
                    } finally {
                        if (progressBarProcessor != null) {
                            progressBarProcessor.walk();
                        }
                    }
                })
                .average().orElse(0);
    }

    public static double calcAverageNeighbourCount(Set<SWNode> swNodeGraph) {
        return swNodeGraph.parallelStream()
                .mapToInt(SWNode::getNeighborCount)
                .average().orElse(0);
    }


}
