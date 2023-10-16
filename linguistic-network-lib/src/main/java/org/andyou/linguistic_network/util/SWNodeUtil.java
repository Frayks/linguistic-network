package org.andyou.linguistic_network.util;

import org.andyou.linguistic_network.api.node.SWNode;

import java.util.*;

public class SWNodeUtil {

    public static Set<SWNode> clone(Set<SWNode> swNodes) {
        Map<String, SWNode> swNodeMap = new HashMap<>();

        for (SWNode swNode : swNodes) {
            swNodeMap.put(swNode.getElement(), new SWNode(swNode));
        }
        for (SWNode swNode : swNodes) {
            SWNode swNodeCopy = swNodeMap.get(swNode.getElement());
            for (SWNode swNodeNeighbor : swNode.getNeighbors()) {
                swNodeCopy.getNeighbors().add(swNodeMap.get(swNodeNeighbor.getElement()));
            }
        }

        return new HashSet<>(swNodeMap.values());
    }

    public static void removeSWNode(Set<SWNode> swNodes, SWNode swNode) {
        swNodes.remove(swNode);
        for (SWNode neighbor : swNode.getNeighbors()) {
            neighbor.getNeighbors().remove(swNode);
        }
    }

    public static void addSWNode(Set<SWNode> swNodes, SWNode swNode) {
        swNodes.add(swNode);
        for (SWNode neighbor : swNode.getNeighbors()) {
            neighbor.getNeighbors().add(swNode);
        }
    }

    // Erdos–Renyi G(n, M)
    public static Set<SWNode> generateRandomSWNodes(Set<SWNode> swNodes, int m) {
        if (m % 2 != 0) {
            throw new IllegalArgumentException(String.format("The number of edges (%d) should be even!", m));
        }

        int n = swNodes.size();
        int k = m / 2;
        if (k > (n * (n - 1)) / 2) {
            throw new IllegalArgumentException(String.format("Graph G(n, M), i.e. G(%d, %d) is not possible!", n, m));
        }

        List<SWNode> randomSWNodes = new ArrayList<>();
        for (SWNode swNode : swNodes) {
            randomSWNodes.add(new SWNode(swNode.getElement(), 0));
        }

        Random random = new Random();
        int count = 0;
        while (count < k) {
            SWNode swNode1 = randomSWNodes.get(random.nextInt(randomSWNodes.size()));
            SWNode swNode2 = randomSWNodes.get(random.nextInt(randomSWNodes.size()));

            if (!swNode1.equals(swNode2) && !swNode1.getNeighbors().contains(swNode2)) {
                swNode1.getNeighbors().add(swNode2);
                swNode2.getNeighbors().add(swNode1);
                count++;
            }
        }

        return new HashSet<>(randomSWNodes);
    }

}
