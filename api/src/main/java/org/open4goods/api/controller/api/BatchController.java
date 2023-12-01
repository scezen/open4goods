/**
 * This controller allow export operations.
 *
 * @author gof
 *
 */

package org.open4goods.api.controller.api;

import java.io.IOException;

import org.open4goods.api.services.BatchService;
import org.open4goods.dao.ProductRepository;
import org.open4goods.exceptions.InvalidParameterException;
import org.open4goods.model.constants.RolesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("batch")
@PreAuthorize("hasAuthority('" + RolesConstants.ROLE_ADMIN + "')")
public class BatchController {

	private final BatchService batchService;

	private final ProductRepository aggDataRepository;

	private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

	public BatchController(BatchService batchService, ProductRepository aggDataRepository) {
		this.batchService = batchService;
		this.aggDataRepository = aggDataRepository;
	}

//
//	@GetMapping("/batch/verticals/associateFromCategory")
//	public void backupDatas() throws InvalidParameterException, IOException {
//		batchService.definesVertical();
//	}
//

	@GetMapping("/batch/verticals/")
	public void scoreVerticals() throws InvalidParameterException, IOException {
		batchService.scoreVertical();


	}

	
	@GetMapping("/batch/verticals/test")
	public void testScoring(@RequestParam String vertical) throws InvalidParameterException, IOException {
		batchService.testScoring(vertical);
	}

}
