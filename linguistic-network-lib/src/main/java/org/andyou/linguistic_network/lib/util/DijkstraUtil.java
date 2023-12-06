package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.data.ElementNode;

import java.util.*;

public class DijkstraUtil {

    public static List<Double> calcPathLengths(ElementNode targetNode) {
        Map<ElementNode, Double> pathLengthMap = new HashMap<>();
        Set<ElementNode> unvisitedNodes = new HashSet<>();

        pathLengthMap.put(targetNode, 0.0);
        unvisitedNodes.add(targetNode);

        while (!unvisitedNodes.isEmpty()) {
            ElementNode node = getMinPathLengthNode(unvisitedNodes, pathLengthMap);
            double nodePathLength = pathLengthMap.get(node);
            unvisitedNodes.remove(node);

            for (Map.Entry<ElementNode, Integer> neighborEntry : node.getNeighbors().entrySet()) {
                ElementNode neighborNode = neighborEntry.getKey();
                double neighborDistance = 1.0 / neighborEntry.getValue();
                double newNeighborPathLength = nodePathLength + neighborDistance;

                Double neighborPathLength = pathLengthMap.get(neighborNode);
                if (neighborPathLength == null) {
                    unvisitedNodes.add(neighborNode);
                    pathLengthMap.put(neighborNode, newNeighborPathLength);
                } else {
                    if (newNeighborPathLength < neighborPathLength) {
                        pathLengthMap.put(neighborNode, newNeighborPathLength);
                    }
                }
            }
        }
        pathLengthMap.remove(targetNode);
        return new ArrayList<>(pathLengthMap.values());
    }

    private static ElementNode getMinPathLengthNode(Set<ElementNode> elementNodes, Map<ElementNode, Double> pathLengthMap) {
        ElementNode minPathLengthElementNode = null;
        double minPathLength = Integer.MAX_VALUE;
        for (ElementNode elementNode : elementNodes) {
            double pathLength = pathLengthMap.get(elementNode);
            if (pathLength < minPathLength) {
                minPathLengthElementNode = elementNode;
                minPathLength = pathLength;
            }
        }
        return minPathLengthElementNode;
    }

}
