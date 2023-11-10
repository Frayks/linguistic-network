package org.andyou.linguistic_network.lib.api.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinguisticNetworkContext {

    private MainContext mainContext;
    private LinguisticMetricsContext linguisticMetricsContext;
    private KeywordExtractionSmallWorldContext keywordExtractionSmallWorldContext;
    private KeywordExtractionTextRankContext keywordExtractionTextRankContext;
    private KeywordExtractionCentralityMeasuresContext keywordExtractionCentralityMeasuresContext;

    public LinguisticNetworkContext() {
        mainContext = new MainContext();
        linguisticMetricsContext = new LinguisticMetricsContext();
        keywordExtractionSmallWorldContext = new KeywordExtractionSmallWorldContext();
        keywordExtractionTextRankContext = new KeywordExtractionTextRankContext();
        keywordExtractionCentralityMeasuresContext = new KeywordExtractionCentralityMeasuresContext();
    }

}
