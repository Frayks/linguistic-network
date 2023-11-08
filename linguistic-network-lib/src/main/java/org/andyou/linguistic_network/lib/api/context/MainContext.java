package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;
import org.andyou.linguistic_network.lib.api.constant.BoundsType;
import org.andyou.linguistic_network.lib.api.constant.NGramType;
import org.andyou.linguistic_network.lib.api.node.ElementNode;

import java.io.File;
import java.util.Set;

@Getter
@Setter
public class MainContext {

    private File textFile;
    private NGramType nGramType;
    private int nGramSize;
    private boolean caseSensitive;
    private boolean includeSpaces;
    private BoundsType boundsType;
    private String sentenceDelimiters;
    private boolean useRange;
    private int rangeSize;
    private boolean removeStopWords;
    private File stopWordsFile;
    private boolean restrictedGraph;
    private double jaccardCoefficient;
    private boolean filterByFrequency;
    private int filterFrequency;
    private boolean weightedGraph;
    private Set<ElementNode> elementNodeGraph;
    private double averageClusteringCoefficient;
    private double averagePathLength;
    private double averageNeighbourCount;
    private long spentTime;

}
