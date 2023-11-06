package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.List;

@Getter
@Setter
public class KeywordExtractionSmallWorldContext {

    private List<SWNode> swNodes;
    private long spentTime;

}
