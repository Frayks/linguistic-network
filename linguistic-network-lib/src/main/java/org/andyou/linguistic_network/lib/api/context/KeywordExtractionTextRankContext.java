package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.TRNode;

import java.util.List;

@Getter
@Setter
public class KeywordExtractionTextRankContext {

    private List<TRNode> trNodes;
    private long spentTime;

}
