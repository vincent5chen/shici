package com.itranswarp.shici.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.service.PoemService;

@Controller
public class WeixinController {

	@Autowired
	PoemService poemService;

	@RequestMapping(value = "/wx/message", method = RequestMethod.GET)
	public ModelAndView onMessage() {
		return null;
	}

	@RequestMapping(value = "/wx/poem/{id}", method = RequestMethod.GET)
	public ModelAndView poem(@PathVariable("id") String id) {
		return null;
	}

}
