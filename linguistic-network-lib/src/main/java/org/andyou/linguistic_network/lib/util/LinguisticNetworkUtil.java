package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.node.CDFNode;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.*;

public class LinguisticNetworkUtil {

    public static List<SWNode> calcKeywordStatisticsSmallWorld(Set<ElementNode> elementNodeGraph, boolean weightedGraph, ProgressBarProcessor progressBarProcessor) {
        elementNodeGraph = ElementNodeGraphUtil.clone(elementNodeGraph);
        List<SWNode> swNodes = new ArrayList<>();

        if (progressBarProcessor != null) {
            int stepsCount = elementNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        double gl = calcAveragePathLength(elementNodeGraph, weightedGraph, true, null);
        List<ElementNode> elementNodes = new ArrayList<>(elementNodeGraph);
        for (ElementNode elementNode : elementNodes) {
            ElementNodeGraphUtil.removeElementNode(elementNodeGraph, elementNode);

            double l = calcAveragePathLength(elementNodeGraph, weightedGraph, true, null);
            double dirtyContribution = l - gl;
            swNodes.add(new SWNode(elementNode, dirtyContribution));

            ElementNodeGraphUtil.addElementNode(elementNodeGraph, elementNode);

            if (progressBarProcessor != null) {
                progressBarProcessor.walk();
            }
        }

        double dirtyContributionMin = swNodes.stream().mapToDouble(SWNode::getDirtyContribution).min().orElse(0);
        swNodes.forEach(swNode -> swNode.setContribution(swNode.getDirtyContribution() - dirtyContributionMin));

        double contributionSum = swNodes.stream().mapToDouble(SWNode::getContribution).sum();
        swNodes.forEach(swNode -> swNode.setNormalizedContribution(swNode.getContribution() / contributionSum));

        return swNodes;
    }

    public static List<CDFNode> calcCDFNodes(Set<ElementNode> elementNodeGraph, ProgressBarProcessor progressBarProcessor) {
        Map<Integer, CDFNode> cdfNodeMap = new HashMap<>();

        for (ElementNode elementNode : elementNodeGraph) {
            int neighborCount = elementNode.getNeighborCount();
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
            cdfNode.setPdf((double) cdfNode.getN() / elementNodeGraph.size());
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

    public static double calcAverageClusteringCoefficient(Set<ElementNode> elementNodeGraph, ProgressBarProcessor progressBarProcessor) {
        if (progressBarProcessor != null) {
            int stepsCount = elementNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        return elementNodeGraph.parallelStream()
                .mapToDouble(elementNode -> {
                    try {
                        double clusteringCoefficient = 0;
                        if (elementNode.getNeighborCount() > 1) {
                            int n = 0;
                            Set<ElementNode> neighbors = elementNode.getNeighbors().keySet();
                            Set<ElementNode> visited = new HashSet<>();
                            for (ElementNode neighbor : neighbors) {
                                Set<ElementNode> relatedElements = new HashSet<>(neighbor.getNeighbors().keySet());

                                relatedElements.retainAll(neighbors);
                                relatedElements.removeAll(visited);
                                n += relatedElements.size();

                                visited.add(neighbor);
                            }

                            int k = elementNode.getNeighborCount();
                            clusteringCoefficient = (double) (2 * n) / (k * (k - 1));
                        }
                        elementNode.setClusteringCoefficient(clusteringCoefficient);
                        return clusteringCoefficient;
                    } finally {
                        if (progressBarProcessor != null) {
                            progressBarProcessor.walk();
                        }
                    }
                })
                .average().orElse(0);
    }

    public static double calcAveragePathLength(Set<ElementNode> elementNodeGraph, boolean weightedGraph, boolean considerLostConnections, ProgressBarProcessor progressBarProcessor) {
        if (progressBarProcessor != null) {
            int stepsCount = elementNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        return elementNodeGraph.parallelStream()
                .mapToDouble(elementNode -> {
                    try {
                        double averagePathLength;

                        if (weightedGraph) {
                            List<Double> pathLengths = DijkstraUtil.calcPathLengths(elementNode);
                            if (considerLostConnections) {

                            }
                            averagePathLength = pathLengths.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .average().orElse(0);

                        } else {
                            List<Integer> pathLengths = BFSUtil.calcPathLengths(elementNode);
                            if (considerLostConnections) {
                                int lostConnectionsNumber = elementNodeGraph.size() - pathLengths.size() - 1;
                                if (lostConnectionsNumber > 0) {
                                    pathLengths.addAll(Collections.nCopies(lostConnectionsNumber, elementNodeGraph.size() - 1));
                                }
                            }
                            averagePathLength = pathLengths.stream()
                                    .mapToInt(Integer::intValue)
                                    .average().orElse(0);
                        }

                        elementNode.setAveragePathLength(averagePathLength);
                        return averagePathLength;
                    } finally {
                        if (progressBarProcessor != null) {
                            progressBarProcessor.walk();
                        }
                    }
                })
                .average().orElse(0);
    }

    public static double calcAverageNeighbourCount(Set<ElementNode> elementNodeGraph) {
        return elementNodeGraph.parallelStream()
                .mapToInt(ElementNode::getNeighborCount)
                .average().orElse(0);
    }


}
