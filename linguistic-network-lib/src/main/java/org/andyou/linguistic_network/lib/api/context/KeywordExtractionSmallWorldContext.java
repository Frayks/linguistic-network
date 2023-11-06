package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.ElementNode;

import java.util.Map;

@Getter
@Setter
public class KeywordExtractionSmallWorldContext {

    private Map<ElementNode, Double> keywordStatistics;
    private long spentTime;

}
