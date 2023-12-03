package org.open4goods.api.services;

import ch.qos.logback.classic.Level;
import org.open4goods.aggregation.aggregator.BatchedAggregator;
import org.open4goods.api.config.yml.ApiProperties;
import org.open4goods.config.yml.ui.VerticalConfig;
import org.open4goods.dao.ProductRepository;
import org.open4goods.exceptions.AggregationSkipException;
import org.open4goods.helper.GenericFileLogger;
import org.open4goods.model.dto.VerticalSearchRequest;
import org.open4goods.model.product.Product;
import org.open4goods.services.SearchService;
import org.open4goods.services.VerticalsConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service is in charge of various batches
 * @author Goulven.Furet
 */
public class BatchService {



	private static final Logger logger = LoggerFactory.getLogger(BatchService.class);



	private ProductRepository dataRepository;

	private VerticalsConfigService verticalsService;

	private BatchAggregationService batchAggregationService;



	private final ApiProperties apiProperties;

	private SearchService searchService;

	private Logger dedicatedLogger;




	public BatchService(
			ProductRepository dataRepository,
			ApiProperties apiProperties,
			VerticalsConfigService verticalsService,
			BatchAggregationService batchAggregationService,
			SearchService searchService,
			boolean toConsole
			) {
		super();

		dedicatedLogger = GenericFileLogger.initLogger("stats-batch", Level.INFO, apiProperties.logsFolder(), toConsole);
		this.apiProperties = apiProperties;
		this.dataRepository =dataRepository;
		this.verticalsService=verticalsService;
		this.batchAggregationService=batchAggregationService;
		this.searchService = searchService;
	}




	
	/**
	 * Update verticals with the batch Aggragator
	 * @throws AggregationSkipException 
	 */
	public void testScoring(String v)  {

		VerticalConfig vertical = verticalsService.getConfigById(v).get();
		BatchedAggregator agg = batchAggregationService.getAggregator(vertical);

		
		
		VerticalSearchRequest vsr = new VerticalSearchRequest();
		vsr.setPageNumber(0);
		vsr.setPageSize(100);
		
		
		
		List<Product> datas =  searchService.verticalSearch(vertical, vsr).getData();
		
		agg.update(datas);

		dataRepository.index(datas);

		
	}
	
	
	

	/**
	 * Update verticals with the batch Aggragator
	 * @throws AggregationSkipException 
	 */
	//TODO : cron schedule, at night
	@Scheduled( initialDelay = 1000 * 3600*24, fixedDelay = 1000 * 3600*24)
	public void scoreVertical()  {



		for (VerticalConfig vertical : verticalsService.getConfigsWithoutDefault()) {
			BatchedAggregator agg = batchAggregationService.getAggregator(vertical);
			
			// NOTE : !!!Warning!!!, full vertical load in memory
			Set<Product> datas = dataRepository.exportVerticalWithValidDate(vertical.getId()).collect(Collectors.toSet());
			
			agg.update(datas);
			
			Set<Product> buffer = new HashSet<>(201);
			for (Product data : datas) {
				buffer.add(data);
				// TODO : from conf
				if (buffer.size() > 200) {
					dataRepository.index(buffer);
					buffer.clear();
				}
			}
			// Index remainings
			dataRepository.index(buffer);
			buffer.clear();
		}
	}




}
