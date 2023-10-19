package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.*;

public class LinguisticNetworkUtil {

    public static Map<SWNode, Double> calcKeywordStatisticsSmallWorld(Set<SWNode> swNodeGraph) {
        swNodeGraph = SWNodeGraphUtil.clone(swNodeGraph);
        Map<SWNode, Double> keywordStatistics = new HashMap<>();

        int swNodeGraphSize = swNodeGraph.size();
        double gl = calcAveragePathLength(swNodeGraph, 0);

        System.out.println(swNodeGraph.size());

        int counter = 0;
        List<SWNode> swNodes = new ArrayList<>(swNodeGraph);
        for (SWNode swNode : swNodes) {
            SWNodeGraphUtil.removeSWNode(swNodeGraph, swNode);

            double l = calcAveragePathLength(swNodeGraph, swNodeGraphSize);
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

    public static double calcAverageClusteringCoefficient(Set<SWNode> swNodeGraph) {
        return swNodeGraph.parallelStream()
                .mapToDouble(swNode -> {
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
                })
                .average().orElse(0);
    }

    public static double calcAveragePathLength(Set<SWNode> swNodeGraph, int maxLength) {
        return swNodeGraph.parallelStream()
                .mapToDouble(swNode -> {
                    List<Integer> pathLengths = BFSUtil.calcPathLengths(swNode);

                    int missingConnectionsNumber = swNodeGraph.size() - pathLengths.size() - 1;
                    pathLengths.addAll(Collections.nCopies(missingConnectionsNumber, maxLength));

                    return pathLengths.stream()
                            .mapToInt(Integer::intValue)
                            .average().orElse(0);
                })
                .average().orElse(0);
    }

    public static double calcAverageNeighbourCount(Set<SWNode> swNodeGraph) {
        return swNodeGraph.parallelStream()
                .mapToInt(swNode -> swNode.getNeighbors().size())
                .average().orElse(0);
    }


}
