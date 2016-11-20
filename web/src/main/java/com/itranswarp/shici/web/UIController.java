package com.itranswarp.shici.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.service.PoemService;
import com.itranswarp.shici.service.ResourceService;
import com.itranswarp.shici.util.MapUtil;

@Controller
public class UIController {

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	PoemService poemService;

	@Autowired
	ResourceService resourceService;

	@RequestMapping(value = "/debug/manage/{name}", method = RequestMethod.GET)
	public ModelAndView debug(@PathVariable("name") String name) {
		return new ModelAndView("/manage/" + name + ".html");
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index() {
		return null;
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView search(@RequestParam("q") String q) {
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

	// management:

	@RequestMapping(value = "/manage/", method = RequestMethod.GET)
	public ModelAndView manage() {
		return new ModelAndView("redirect:/manage/dynasties");
	}

	@RequestMapping(value = "/manage/dynasties", method = RequestMethod.GET)
	public ModelAndView manageDynasties() {
		return new ModelAndView("manage/dynasties.html");
	}

	@RequestMapping(value = "/manage/dynasties/{id}/poets", method = RequestMethod.GET)
	public ModelAndView managePoets(@PathVariable("id") String dynastyId) {
		return new ModelAndView("manage/poets.html", "dynastyId", dynastyId);
	}

	@RequestMapping(value = "/manage/dynasties/{id}/poets/add", method = RequestMethod.GET)
	public ModelAndView manageAddPoet(@PathVariable("id") String dynastyId) {
		PoetBean poet = new PoetBean();
		poet.dynastyId = dynastyId;
		poet.name = "New Poet";
		return new ModelAndView("manage/poet.html", MapUtil.createMap("action", "/api/poets", "poet", poet));
	}

	@RequestMapping(value = "/manage/dynasties/poets/{id}/edit", method = RequestMethod.GET)
	public ModelAndView manageEditPoet(@PathVariable("id") String poetId) {
		Poet poet = poemService.getPoet(poetId);
		return new ModelAndView("manage/poet.html", MapUtil.createMap("action", "/api/poets/" + poetId, "poet", poet));
	}

	@RequestMapping(value = "/manage/dynasties/poets/{id}/poems", method = RequestMethod.GET)
	public ModelAndView managePoems(@PathVariable("id") String poetId) {
		return new ModelAndView("manage/poems.html", "poetId", poetId);
	}

	@RequestMapping(value = "/manage/dynasties/poets/{id}/poems/add", method = RequestMethod.GET)
	public ModelAndView manageAddPoem(@PathVariable("id") String poetId) {
		PoemBean poem = new PoemBean();
		poem.poetId = poetId;
		return new ModelAndView("manage/poem.html",
				MapUtil.createMap("action", "/api/poets/" + poetId + "/poems", "poem", poem));
	}

	@RequestMapping(value = "/manage/dynasties/poets/poems/{id}/edit", method = RequestMethod.GET)
	public ModelAndView manageEditPoem(@PathVariable("id") String poemId) {
		Poem p = poemService.getPoem(poemId);
		PoemBean poem = new PoemBean();
		poem.appreciation = p.appreciation;
		poem.content = p.content;
		poem.form = p.form;
		poem.imageData = "".equals(p.imageId) ? "" : resourceService.getResource(p.imageId).data;
		poem.name = p.name;
		poem.poetId = p.poetId;
		poem.tags = p.tags;
		return new ModelAndView("manage/poem.html", MapUtil.createMap("action", "/api/poems/" + poemId, "poem", poem));
	}

	@RequestMapping(value = "/manage/categories", method = RequestMethod.GET)
	public ModelAndView manageCategories() {
		return new ModelAndView("manage/categories.html");
	}

	@RequestMapping(value = "/manage/categories/{id}/poems", method = RequestMethod.GET)
	public ModelAndView manageCategoryPoems(@PathVariable("id") String categoryId) {
		return new ModelAndView("manage/categorypoems.html", "categoryId", categoryId);
	}

}
