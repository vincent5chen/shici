package com.itranswarp.shici.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.bean.Dynasty;
import com.itranswarp.shici.bean.Hit;
import com.itranswarp.shici.bean.Poem;
import com.itranswarp.shici.bean.Poet;
import com.itranswarp.shici.util.Utils;

/**
 * Provide MVC.
 * 
 * @author liaoxuefeng
 */
@Controller
public class MvcController extends AbstractController {

	@GetMapping("/")
	public ModelAndView index() {
		Poem poem = poemService.getFeatured();
		return prepareModelAndView("index.html", Utils.ofMap("poem", poem));
	}

	@GetMapping("/dynasty/{id}")
	public ModelAndView dynasty(@PathVariable("id") long id) {
		Dynasty dynasty = poemService.getDynasty(id);
		List<Poet> poets = poemService.getPoets(id);
		return prepareModelAndView("dynasty.html", Utils.ofMap("dynasty", dynasty, "poets", poets));
	}

	@GetMapping("/poet/{id}")
	public ModelAndView poet(@PathVariable("id") long id) {
		Poet poet = poemService.getPoet(id);
		Dynasty dynasty = poet.getDynasty();
		List<Poem> poems = poemService.getPoems(id);
		return prepareModelAndView("poet.html", Utils.ofMap("dynasty", dynasty, "poet", poet, "poems", poems));
	}

	@GetMapping("/poem/{id}")
	public ModelAndView poem(@PathVariable("id") long id) {
		Poem poem = poemService.getPoem(id);
		Poet poet = poem.getPoet();
		Dynasty dynasty = poet.getDynasty();
		return prepareModelAndView("poem.html", Utils.ofMap("dynasty", dynasty, "poet", poet, "poem", poem));
	}

	@GetMapping("/search")
	public ModelAndView search(@RequestParam(value = "q", defaultValue = "") String q) {
		if (q.trim().isEmpty()) {
			return prepareModelAndView("search.html", Utils.ofMap("q", q, "poems", EMPTY_POEMS));
		}
		Hit[] hits = this.searchService.search(q);
		if (hits.length == 0) {
			return prepareModelAndView("search.html", Utils.ofMap("q", q, "poems", EMPTY_POEMS));
		}
		List<Poem> poems = Arrays.stream(hits).map(hit -> poemService.getPoem(hit.id)).collect(Collectors.toList());
		return prepareModelAndView("search.html", Utils.ofMap("q", q, "poems", poems));
	}

	static final List<Poem> EMPTY_POEMS = Collections.emptyList();
}
