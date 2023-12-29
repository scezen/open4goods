package org.open4goods.api.services.aggregation.services.batch.scores;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.open4goods.config.yml.ui.AttributesConfig;
import org.open4goods.exceptions.ValidationException;
import org.open4goods.model.data.Score;
import org.open4goods.model.product.Product;

/**
 * Create a score based on data quality (number of non virtual scores for this product)
 * @author goulven
 *
 */
public class DataCompletion2ScoreAggregationService extends AbstractScoreAggregationService {

	private static final String DATA_QUALITY_SCORENAME = "DATA-QUALITY";
	private final AttributesConfig attributesConfig;

	public DataCompletion2ScoreAggregationService(final AttributesConfig attributesConfig,  final String logsFolder,boolean toConsole) {
		super(logsFolder,toConsole);
		this.attributesConfig = attributesConfig;
	}



	@Override
	public void onProduct(Product data) {
		if (StringUtils.isEmpty(data.brand())) {
			return;
		}
		
		try {
			Double score = generateScoreFromDataquality(data.getScores());

			// Processing cardinality
			processCardinality(DATA_QUALITY_SCORENAME,score);			
			Score s = new Score(DATA_QUALITY_SCORENAME, score);
			// Saving in product
			data.getScores().put(s.getName(),s);
		} catch (ValidationException e) {
			dedicatedLogger.warn("DataQuality to score fail for {}",data,e);
		}								
		
		
	}


	/**
	 * The data score is the number of score that are not virtuals
	 * @param map
	 * @return
	 */
	private Double generateScoreFromDataquality(Map<String, Score> map) {
		
		return  Double.valueOf(map.values().stream().filter(e -> !e.getVirtual()).filter(e -> !e.getName().equals(DATA_QUALITY_SCORENAME)) .count());		
		
	}


}