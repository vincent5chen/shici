package com.itranswarp.shici.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.service.PoemService;

@Controller
public class UIController {

	@Autowired
	PoemService poemService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index() {
		return null;
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView search() {
		return null;
	}

	@RequestMapping(value = "/dynasty/{id}", method = RequestMethod.GET)
	public ModelAndView poets(@PathVariable("id") String dynastyId) {
		return null;
	}

	@RequestMapping(value = "/poet/{id}", method = RequestMethod.GET)
	public ModelAndView poems(@PathVariable("id") String poetId,
			@RequestParam(value = "page", defaultValue = "1") int pageIndex) {
		return null;
	}

	@RequestMapping(value = "/poem/{id}", method = RequestMethod.GET)
	public ModelAndView poem(@PathVariable("id") String poemId) {
		Poem poem = poemService.getPoem(poemId);
		Poet poet = poemService.getPoet(poem.poetId);
		return null;
	}

}
