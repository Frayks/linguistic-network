package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.util.Map;

@Getter
@Setter
public class KeywordExtractionSmallWorldContext {

    private Map<SWNode, Double> keywordStatistics;
    private long spentTime;

}
