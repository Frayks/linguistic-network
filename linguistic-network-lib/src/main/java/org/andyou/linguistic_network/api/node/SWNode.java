package org.andyou.linguistic_network.api.node;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SWNode {

    private String element;
    private int frequency;
    private Set<SWNode> neighbors;

    public SWNode(String element, int frequency) {
        this.element = element;
        this.frequency = frequency;
        this.neighbors = new HashSet<>();
    }

    public SWNode(SWNode swNode) {
        this.element = swNode.getElement();
        this.frequency = swNode.getFrequency();
        this.neighbors = new HashSet<>();
    }

}
