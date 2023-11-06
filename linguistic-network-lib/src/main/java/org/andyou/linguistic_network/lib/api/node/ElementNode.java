package org.andyou.linguistic_network.lib.api.node;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ElementNode {

    private int index;
    private String element;
    private int frequency;
    private Set<ElementNode> neighbors;

    public ElementNode(String element, int frequency) {
        this.element = element;
        this.frequency = frequency;
        this.neighbors = new HashSet<>();
    }

    public ElementNode(ElementNode elementNode) {
        this.index = elementNode.index;
        this.element = elementNode.element;
        this.frequency = elementNode.frequency;
        this.neighbors = new HashSet<>();
    }

    public int getNeighborCount() {
        return neighbors.size();
    }

}
