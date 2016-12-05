package com.itranswarp.shici.web;

import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.bean.CategoryBean;
import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.model.Category;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.service.PoemService;
import com.itranswarp.shici.service.ResourceService;
import com.itranswarp.shici.util.MapUtil;
import com.itranswarp.warpdb.PagedResults;

@Controller
public class UIController {

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	PoemService poemService;

	@Autowired
	ResourceService resourceService;

	@GetMapping("/")
	public ModelAndView index() {
		Poem poem = poemService.getFeaturedPoem(LocalDate.now());
		Poet poet = poemService.getPoet(poem.poetId);
		return createMV("index.html", MapUtil.createMap("poem", poem, "poet", poet));
	}

	@GetMapping("/dynasty/{id}")
	public ModelAndView poets(@PathVariable("id") String dynastyId) {
		return createMV("dynasty.html", MapUtil.createMap("dynasty", poemService.getDynasty(dynastyId), "poets",
				poemService.getPoets(dynastyId)));
	}

	@GetMapping("/poet/{id}")
	public ModelAndView poet(@PathVariable("id") String poetId,
			@RequestParam(value = "page", defaultValue = "1") int pageIndex) {
		if (pageIndex < 1 || pageIndex > 1000) {
			throw new IllegalArgumentException("page");
		}
		Poet poet = poemService.getPoet(poetId);
		PagedResults<Poem> results = poemService.getPoems(poetId, pageIndex);
		return createMV("poet.html", MapUtil.createMap("dynasty", poemService.getDynasty(poet.dynastyId), "poet", poet,
				"poems", results.results, "page", results.page));
	}

	@GetMapping("/poem/{id}")
	public ModelAndView poem(@PathVariable("id") String poemId) {
		Poem poem = poemService.getPoem(poemId);
		Poet poet = poemService.getPoet(poem.poetId);
		return createMV("poem.html",
				MapUtil.createMap("dynasty", poemService.getDynasty(poet.dynastyId), "poet", poet, "poem", poem));
	}

	@GetMapping("/search")
	public ModelAndView search(@RequestParam("q") String q) {
		return createMV("search.html", "q", q);
	}

	// management /////////////////////////////////////////////////////////////

	@GetMapping("/manage/")
	public ModelAndView manage() {
		return createMV("redirect:/manage/dynasties");
	}

	// manage dynasties, poets, poems /////////////////////////////////////////

	@GetMapping("/manage/dynasties")
	public ModelAndView manageDynasties() {
		return createMV("manage/dynasties.html");
	}

	@GetMapping("/manage/dynasties/{id}/poets")
	public ModelAndView managePoets(@PathVariable("id") String dynastyId) {
		return createMV("manage/poets.html", "dynastyId", dynastyId);
	}

	@GetMapping("/manage/dynasties/{id}/poets/add")
	public ModelAndView manageAddPoet(@PathVariable("id") String dynastyId) {
		PoetBean poet = new PoetBean();
		poet.dynastyId = dynastyId;
		poet.name = "New Poet";
		return createMV("manage/poet.html", MapUtil.createMap("action", "/api/poets.json", "poet", poet));
	}

	@GetMapping("/manage/dynasties/poets/{id}/edit")
	public ModelAndView manageEditPoet(@PathVariable("id") String poetId) {
		Poet poet = poemService.getPoet(poetId);
		return createMV("manage/poet.html", MapUtil.createMap("action", "/api/poets/" + poetId, "poet", poet));
	}

	@GetMapping("/manage/dynasties/poets/{id}/poems")
	public ModelAndView managePoems(@PathVariable("id") String poetId) {
		return createMV("manage/poems.html", "poetId", poetId);
	}

	@GetMapping("/manage/dynasties/poets/{id}/poems/add")
	public ModelAndView manageAddPoem(@PathVariable("id") String poetId) {
		PoemBean poem = new PoemBean();
		poem.poetId = poetId;
		return createMV("manage/poem.html", MapUtil.createMap("action", "/api/poems", "poem", poem));
	}

	@GetMapping("/manage/dynasties/poets/poems/{id}/edit")
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
		return createMV("manage/poem.html", MapUtil.createMap("action", "/api/poems/" + poemId, "poem", poem));
	}

	// manage categories //////////////////////////////////////////////////////

	@GetMapping("/manage/categories")
	public ModelAndView manageCategories() {
		return createMV("manage/categories.html");
	}

	@GetMapping("/manage/categories/add")
	public ModelAndView manageAddCategory() {
		CategoryBean category = new CategoryBean();
		category.name = "New Category";
		category.description = "";
		return createMV("manage/category.html", MapUtil.createMap("action", "/api/categories", "category", category));
	}

	@GetMapping("/manage/categories/{id}/edit")
	public ModelAndView manageEditCategory(@PathVariable("id") String categoryId) {
		Category category = poemService.getCategory(categoryId);
		return createMV("manage/category.html",
				MapUtil.createMap("action", "/api/categories/" + categoryId, "category", category));
	}

	@GetMapping("/manage/categories/{id}/poems")
	public ModelAndView manageCategoryPoems(@PathVariable("id") String categoryId) {
		return createMV("manage/categorypoems.html", "categoryId", categoryId);
	}

	// manage featured ////////////////////////////////////////////////////////

	@GetMapping("/manage/featured")
	public ModelAndView manageFeaturedPoems() {
		return createMV("manage/featured.html");
	}

	// manage users ///////////////////////////////////////////////////////////

	@GetMapping("/manage/users")
	public ModelAndView manageUsers(@RequestParam(name = "page", defaultValue = "1") int page) {
		if (page < 1) {
			throw new IllegalArgumentException("page");
		}
		return createMV("manage/users.html", "page", page);
	}

	// manage search //////////////////////////////////////////////////////////

	@GetMapping("/manage/search")
	public ModelAndView manageSearch(@RequestParam(name = "q") String q) {
		return createMV("manage/search.html", "q", q);
	}

	// enhance ModelAndView:

	ModelAndView createMV(String viewName) {
		return bindModel(new ModelAndView(viewName));
	}

	ModelAndView createMV(String viewName, String modelName, Object modelObject) {
		return bindModel(new ModelAndView(viewName, modelName, modelObject));
	}

	ModelAndView createMV(String viewName, Map<String, ?> model) {
		return bindModel(new ModelAndView(viewName, model));
	}

	ModelAndView bindModel(ModelAndView mv) {
		ModelMap mm = mv.getModelMap();
		mm.put("user", UserContext.getCurrentUser());
		mm.put("dynasties", poemService.getDynasties());
		return mv;
	}
}
