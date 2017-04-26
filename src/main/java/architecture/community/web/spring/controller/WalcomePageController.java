package architecture.community.web.spring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import architecture.community.web.util.ServletUtils;

@Controller("welcome-page-controller")
public class WalcomePageController {

	private static final Logger log = LoggerFactory.getLogger(WalcomePageController.class);	

	@RequestMapping(value={"/","/index"} , method = { RequestMethod.POST, RequestMethod.GET } )
    public String displayWelcomePage (HttpServletRequest request, HttpServletResponse response, Model model) {
		ServletUtils.setContentType(null, response);	
		return "index" ;
    }

}