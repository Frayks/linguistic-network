package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.node.SWNode;

import java.io.File;
import java.util.Set;

@Getter
@Setter
public class MainContext {

    private File textFile;
    private boolean caseSensitive;
    private boolean considerSentenceBounds;
    private boolean useRange;
    private int rangeSize;
    private boolean removeStopWords;
    private File stopWordsFile;
    private boolean filterByFrequency;
    private int filterFrequency;
    private Set<SWNode> swNodeGraph;
    private long spentTime;

}
