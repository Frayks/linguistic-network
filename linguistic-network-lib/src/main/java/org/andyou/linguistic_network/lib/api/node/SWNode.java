package org.andyou.linguistic_network.lib.api.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SWNode {

    private ElementNode elementNode;
    private Double dirtyContribution;
    private Double contribution;
    private Double normalizedContribution;

    public SWNode(ElementNode elementNode, Double dirtyContribution) {
        this.elementNode = elementNode;
        this.dirtyContribution = dirtyContribution;
    }
}
