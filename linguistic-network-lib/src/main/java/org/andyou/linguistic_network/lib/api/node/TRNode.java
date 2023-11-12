package org.andyou.linguistic_network.lib.api.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TRNode {

    private ElementNode elementNode;
    private double importance;
    private Double normalizedImportance;

    public TRNode(ElementNode elementNode, double importance) {
        this.elementNode = elementNode;
        this.importance = importance;
    }
}
