package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.constant.StopConditionType;
import org.andyou.linguistic_network.lib.api.data.*;
import org.andyou.linguistic_network.lib.gui.ProgressBarProcessor;

import java.util.*;

public class LinguisticNetworkUtil {

    public static List<SWNode> calcKeywordStatisticsSmallWorld(Set<ElementNode> elementNodeGraph, boolean weightedGraph, ProgressBarProcessor progressBarProcessor) {
        elementNodeGraph = ElementNodeGraphUtil.clone(elementNodeGraph);
        List<SWNode> swNodes = new ArrayList<>();

        if (progressBarProcessor != null) {
            int stepsCount = elementNodeGraph.size();
            progressBarProcessor.initNextBlock(stepsCount);
        }

        double cg = calcAverageClusteringCoefficient(elementNodeGraph, null);
        double lg = calcAveragePathLength(elementNodeGraph, weightedGraph, true, null);
        List<ElementNode> elementNodes = new ArrayList<>(elementNodeGraph);
        for (ElementNode elementNode : elementNodes) {
            ElementNodeGraphUtil.removeElementNode(elementNodeGraph, elementNode);

            double l = calcAveragePathLength(elementNodeGraph, weightedGraph, true, null);
            double c = calcAverageClusteringCoefficient(elementNodeGraph, null);
            double contribution1 = l - lg;
            double contribution2 = (cg / lg) / (c / l);
            double contribution3 = (cg / lg) - (c / l);

            swNodes.add(new SWNode(elementNode, contribution1, contribution2, contribution3));

            ElementNodeGraphUtil.addElementNode(elementNodeGraph, elementNode);

            if (progressBarProcessor != null) {
                progressBarProcessor.walk();
            }
        }

        double minContribution1 = swNodes.stream().mapToDouble(SWNode::getContribution1).min().orElse(0);
        swNodes.forEach(swNode -> swNode.setAdjustedContribution1(swNode.getContribution1() - minContribution1));
        double sumContribution1 = swNodes.stream().mapToDouble(SWNode::getAdjustedContribution1).sum();
        swNodes.forEach(swNode -> swNode.setNormalizedContribution1(swNode.getAdjustedContribution1() / sumContribution1));

        double sumContribution2 = swNodes.stream().mapToDouble(SWNode::getContribution2).sum();
        swNodes.forEach(swNode -> swNode.setNormalizedContribution2(swNode.getContribution2() / sumContribution2));

        double minContribution3 = swNodes.stream().mapToDouble(SWNode::getContribution3).min().orElse(0);
        swNodes.forEach(swNode -> swNode.setAdjustedContribution3(swNode.getContribution3() - minContribution3));
        double sumContribution3 = swNodes.stream().mapToDouble(SWNode::getAdjustedContribution3).sum();
        if (sumContribution3 == 0.0) {
            swNodes.forEach(swNode -> swNode.setNormalizedContribution3(0.0));
        } else {
            swNodes.forEach(swNode -> swNode.setNormalizedContribution3(swNode.getAdjustedContribution3() / sumContribution3));
        }

        return swNodes;
    }

    public static TextRankStatistics calcKeywordStatisticsTextRank(Set<ElementNode> elementNodeGraph, StopConditionType stopConditionType, double accuracy, int iterationCount, double dampingFactor, boolean weightedGraph, ProgressBarProcessor progressBarProcessor) {
        elementNodeGraph = ElementNodeGraphUtil.clone(elementNodeGraph);
        TextRankStatistics textRankStatistics = new TextRankStatistics();

        Map<ElementNode, TRNode> trNodeMap = new HashMap<>();
        for (ElementNode elementNode : elementNodeGraph) {
            trNodeMap.put(elementNode, new TRNode(elementNode, 1.0));
        }

        List<TRNode> trNodes = new ArrayList<>(trNodeMap.values());
        trNodes.sort(Comparator.comparing(trNode -> trNode.getElementNode().getElement()));

        if (StopConditionType.ACCURACY.equals(stopConditionType)) {
            if (progressBarProcessor != null) {
                progressBarProcessor.setIndeterminate(true);
            }

            int iterationsCompleted = 0;
            double calculationError = Double.MAX_VALUE;
            while (calculationError > accuracy) {
                calculationError = executeTextRankIteration(trNodes, trNodeMap, dampingFactor, weightedGraph);
                iterationsCompleted++;
            }
            textRankStatistics.setIterationsCompleted(iterationsCompleted);
            textRankStatistics.setCalculationError(calculationError);
        } else if (StopConditionType.ITERATION_COUNT.equals(stopConditionType)) {
            if (progressBarProcessor != null) {
                progressBarProcessor.initNextBlock(iterationCount);
            }

            double accuracyAchieved = 0.0;
            for (int i = 0; i < iterationCount; i++) {
                accuracyAchieved = executeTextRankIteration(trNodes, trNodeMap, dampingFactor, weightedGraph);

                if (progressBarProcessor != null) {
                    progressBarProcessor.walk();
                }
            }
            textRankStatistics.setIterationsCompleted(iterationCount);
            textRankStatistics.setCalculationError(accuracyAchieved);
        }

        double sumImportance = trNodes.stream().mapToDouble(TRNode::getImportance).sum();
        trNodes.forEach(trNode -> trNode.setNormalizedImportance(trNode.getImportance() / sumImportance));
        textRankStatistics.setTrNodes(trNodes);

        return textRankStatistics;
    }

    private static double executeTextRankIteration(List<TRNode> trNodes, Map<ElementNode, TRNode> trNodeMap, double dampingFactor, boolean weightedGraph) {
        return trNodes.stream().mapToDouble(trNode -> {
            ElementNode elementNode = trNode.getElementNode();

            double sum = elementNode.getNeighbors().entrySet().stream()
                    .mapToDouble(neighborEntry -> {
                        ElementNode neighborElementNode = neighborEntry.getKey();
                        TRNode neighborTRNode = trNodeMap.get(neighborElementNode);
                        if (weightedGraph) {
                            double neighborWeight = 1.0 / neighborEntry.getValue();
                            double sumWeight = neighborElementNode.getNeighbors().values().stream()
                                    .mapToDouble(multiplicity -> 1.0 / multiplicity).sum();
                            return neighborWeight / sumWeight * neighborTRNode.getImportance();
                        } else {
                            return 1.0 / neighborElementNode.getNeighborCount() * neighborTRNode.getImportance();
                        }
                    }).sum();
            double oldImportance = trNode.getImportance();
            double newImportance = (1 - dampingFactor) + dampingFactor * sum;
            trNode.setImportance(newImportance);
            return Math.abs(newImportance - oldImportance);
        }).max().orElse(0);
    }

    public static List<CMNode> calcKeywordStatisticsCentralityMeasures(Set<ElementNode> elementNodeGraph, boolean weightedGraph, ProgressBarProcessor progressBarProcessor) {
        elementNodeGraph = ElementNodeGraphUtil.clone(elementNodeGraph);

        List<CMNode> cmNodes = new ArrayList<>();

        for (ElementNode elementNode : elementNodeGraph) {

            double eccentricity;
            double closeness;
            double averageCloseness;

            if (weightedGraph) {
                eccentricity = elementNode.getNeighbors().values().stream().mapToInt(Integer::intValue).max().orElse(0);
                closeness = elementNode.getNeighbors().values().stream().mapToInt(Integer::intValue).sum();
                averageCloseness = elementNode.getNeighbors().values().stream().mapToInt(Integer::intValue).average().orElse(0);
            } else {
                eccentricity = 1;
                closeness = elementNode.getNeighborCount();
                averageCloseness = 1;
            }

            cmNodes.add(new CMNode(elementNode, eccentricity, closeness, averageCloseness));
        }

        double sumEccentricity = cmNodes.stream().mapToDouble(CMNode::getEccentricity).sum();
        cmNodes.forEach(cmNode -> cmNode.setNormalizedEccentricity(cmNode.getEccentricity() / sumEccentricity));

        double sumCloseness = cmNodes.stream().mapToDouble(CMNode::getCloseness).sum();
        cmNodes.forEach(cmNode -> cmNode.setNormalizedCloseness(cmNode.getCloseness() / sumCloseness));

        double sumAverageCloseness = cmNodes.stream().mapToDouble(CMNode::getAverageCloseness).sum();
        cmNodes.forEach(cmNode -> cmNode.setNormalizedAverageCloseness(cmNode.getAverageCloseness() / sumAverageCloseness));

        return cmNodes;
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

        double weightedPathLength = elementNodeGraph.stream()
                .mapToDouble(elementNode -> elementNode.getNeighbors().values().stream()
                        .mapToDouble(value -> 1.0 / value).average().orElse(0))
                .sum();

        return elementNodeGraph.parallelStream()
                .mapToDouble(elementNode -> {
                    try {
                        double averagePathLength;

                        if (weightedGraph) {
                            List<Double> pathLengths = DijkstraUtil.calcPathLengths(elementNode);
                            if (considerLostConnections) {
                                int lostConnectionsNumber = elementNodeGraph.size() - pathLengths.size() - 1;
                                if (lostConnectionsNumber > 0) {
                                    pathLengths.addAll(Collections.nCopies(lostConnectionsNumber, weightedPathLength));
                                }
                            }
                            averagePathLength = pathLengths.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .average().orElse(0);

                        } else {
                            List<Integer> pathLengths = BFSUtil.calcPathLengths(elementNode);
                            if (considerLostConnections) {
                                int lostConnectionsNumber = elementNodeGraph.size() - pathLengths.size() - 1;
                                if (lostConnectionsNumber > 0) {
                                    pathLengths.addAll(Collections.nCopies(lostConnectionsNumber, elementNodeGraph.size()));
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


    public static double calcAverageMultiplicity(Set<ElementNode> elementNodeGraph, boolean weightedGraph) {
        return elementNodeGraph.stream().mapToInt(elementNode -> {
            int multiplicity;
            if (weightedGraph) {
                multiplicity = elementNode.getNeighbors().values().stream().mapToInt(Integer::intValue).sum();
            } else {
                multiplicity = elementNode.getNeighborCount();
            }
            elementNode.setMultiplicity(multiplicity);
            return multiplicity;
        }).average().orElse(0);
    }


}
