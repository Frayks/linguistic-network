package org.andyou.linguistic_network.util;

import org.andyou.linguistic_network.api.node.CDFNode;
import org.andyou.linguistic_network.api.node.SWNode;

import java.util.*;

public class CDFNodeUtil {

    private static List<CDFNode> createCDFNodes(Set<SWNode> swNodeGraph) {
        Map<Integer, CDFNode> cdfNodeMap = new HashMap<>();

        for (SWNode swNode : swNodeGraph) {
            int neighborCount = swNode.getNeighbors().size();
            CDFNode cdfNode = cdfNodeMap.computeIfAbsent(neighborCount, key -> new CDFNode(key, 0));
            cdfNode.setN(cdfNode.getN() + 1);
        }

        List<CDFNode> cdfNodes = new ArrayList<>(cdfNodeMap.values());
        cdfNodes.sort(Comparator.comparingInt(CDFNode::getK));

        for (int i = 0; i < cdfNodes.size(); i++) {
            CDFNode cdfNode = cdfNodes.get(i);
            cdfNode.setPdf((double) cdfNode.getN() / swNodeGraph.size());
            if (i == 0) {
                cdfNode.setCdf(1);
            } else {
                CDFNode previousCDFNode = cdfNodes.get(i - 1);
                cdfNode.setCdf(previousCDFNode.getCdf() - previousCDFNode.getPdf());
            }
        }

        return cdfNodes;
    }

}
