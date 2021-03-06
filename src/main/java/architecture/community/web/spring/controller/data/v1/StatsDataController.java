package architecture.community.web.spring.controller.data.v1;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.board.BoardMessageNotFoundException;
import architecture.community.board.BoardThreadNotFoundException;
import architecture.community.query.CustomQueryService;
import architecture.community.query.ParameterValue;
import architecture.community.web.model.ItemList;

@Controller("community-data-v1-stats-controller")
@RequestMapping("/data/v1")
public class StatsDataController {
	
	private Logger log = LoggerFactory.getLogger(getClass());	

	@Inject
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;
	
	
	@RequestMapping(value = "/stats/{target}/{statement}/list.json", method = {RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ItemList getStats(
			@PathVariable String target, 
			@PathVariable String statement, 
			@RequestBody List<ParameterValue> params,
			NativeWebRequest request)
			throws BoardThreadNotFoundException, BoardMessageNotFoundException { 
		List list = customQueryService.list(target, statement, params);	
		return new ItemList(list, list.size());
	}
	
}
