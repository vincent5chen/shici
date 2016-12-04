package com.itranswarp.shici.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.bean.StaticPageBean;
import com.itranswarp.shici.exception.APIEntityConflictException;
import com.itranswarp.shici.exception.APIEntityNotFoundException;
import com.itranswarp.shici.model.StaticPage;
import com.itranswarp.shici.util.MapUtil;

@RestController
public class StaticPageService extends AbstractService {

	public StaticPage getStaticPage(String id) {
		StaticPage sp = warpdb.fetch(StaticPage.class, id);
		if (sp == null) {
			throw new APIEntityNotFoundException(StaticPage.class);
		}
		return sp;
	}

	public StaticPage getStaticPageByAlias(String alias) {
		StaticPage sp = warpdb.from(StaticPage.class).where("alias=?", alias).first();
		if (sp == null) {
			throw new APIEntityNotFoundException(StaticPage.class);
		}
		return sp;
	}

	public List<StaticPage> getStaticPages() {
		return warpdb.from(StaticPage.class).orderBy("alias").list();
	}

	@GetMapping("/api/staticpages")
	public Map<String, List<StaticPage>> restGetStaticPages() {
		assertEditorRole();
		return MapUtil.createMap("results", getStaticPages());
	}

	@GetMapping("/api/staticpages/{id}")
	public StaticPage restGetStaticPage(@PathVariable(value = "id") String spId) {
		assertEditorRole();
		return getStaticPage(spId);
	}

	@PostMapping(value = "/api/staticpages")
	public StaticPage restCreateStaticPage(@RequestBody StaticPageBean bean) {
		assertEditorRole();
		bean.validate();
		StaticPage exist = warpdb.from(StaticPage.class).where("alias=?", bean.alias).first();
		if (exist != null) {
			throw new APIEntityConflictException("alias", "alias exist");
		}
		// create:
		StaticPage sp = new StaticPage();
		sp.alias = bean.alias;
		sp.name = bean.name;
		sp.content = bean.content;
		warpdb.save(sp);
		return sp;
	}

	@PostMapping("/api/staticpages/{id}")
	public StaticPage restUpdateStaticPage(@PathVariable("id") String id, @RequestBody StaticPageBean bean) {
		assertEditorRole();
		bean.validate();
		// update:
		StaticPage sp = getStaticPage(id);
		if (!sp.alias.equals(bean.alias)) {
			StaticPage exist = warpdb.from(StaticPage.class).where("alias=?", bean.alias).first();
			if (exist != null) {
				throw new APIEntityConflictException("alias", "alias exist");
			}
		}
		sp.alias = bean.alias;
		sp.name = bean.name;
		sp.content = bean.content;
		warpdb.update(sp);
		return sp;
	}

	@PostMapping("/api/staticpages/{id}/delete")
	public StaticPage restDeleteStaticPage(@PathVariable("id") String id) {
		assertEditorRole();
		StaticPage sp = getStaticPage(id);
		warpdb.remove(sp);
		return sp;
	}
}
