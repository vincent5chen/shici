package com.itranswarp.shici.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.service.PoemService;
import com.itranswarp.shici.service.SearchService;
import com.itranswarp.shici.util.Utils;

/**
 * Super class for controller.
 * 
 * @author liaoxuefeng
 */
public class AbstractController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected SearchService searchService;

	@Autowired
	protected PoemService poemService;

	protected ModelAndView prepareModelAndView(String view) {
		return prepare(view, null);
	}

	protected ModelAndView prepareModelAndView(String view, String k1, Object v1) {
		return prepare(view, Utils.ofMap(k1, v1));
	}

	protected ModelAndView prepareModelAndView(String view, String k1, Object v1, String k2, Object v2) {
		return prepare(view, Utils.ofMap(k1, v1, k2, v2));
	}

	protected ModelAndView prepareModelAndView(String view, Map<String, Object> model) {
		return prepare(view, model);
	}

	private ModelAndView prepare(String view, Map<String, Object> model) {
		ModelAndView mv = new ModelAndView(view, model);
		mv.addObject("dynasties", poemService.getDynasties());
		mv.addObject("__timestamp__", System.currentTimeMillis());
		return mv;
	}
}
