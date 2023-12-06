package org.andyou.linguistic_network.lib.util;

import org.andyou.linguistic_network.lib.api.data.ElementNode;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class DijkstraUtil {

    public static List<Double> calcPathLengths(ElementNode targetNode) {
        Map<ElementNode, Double> pathLengthMap = new HashMap<>();
        PriorityQueue<Pair<ElementNode, Double>> unvisitedNodes = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));

        pathLengthMap.put(targetNode, 0.0);
        unvisitedNodes.add(new Pair<>(targetNode, 0.0));

        while (!unvisitedNodes.isEmpty()) {
            Pair<ElementNode, Double> pairNode = unvisitedNodes.poll();
            ElementNode node = pairNode.getKey();
            double currentPathLength = pairNode.getValue();
            double pathLength = pathLengthMap.get(node);

            if (currentPathLength > pathLength) {
                continue;
            }

            for (Map.Entry<ElementNode, Integer> neighborEntry : node.getNeighbors().entrySet()) {
                ElementNode neighborNode = neighborEntry.getKey();
                double neighborDistance = 1.0 / neighborEntry.getValue();
                double newNeighborPathLength = currentPathLength + neighborDistance;

                Double neighborPathLength = pathLengthMap.get(neighborNode);
                if (neighborPathLength == null || newNeighborPathLength < neighborPathLength) {
                    pathLengthMap.put(neighborNode, newNeighborPathLength);
                    unvisitedNodes.offer(new Pair<>(neighborNode, newNeighborPathLength));
                }
            }
        }
        pathLengthMap.remove(targetNode);
        return new ArrayList<>(pathLengthMap.values());
    }

}
