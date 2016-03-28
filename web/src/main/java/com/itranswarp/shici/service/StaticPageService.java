package com.itranswarp.shici.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.bean.StaticPageBean;
import com.itranswarp.shici.exception.APIEntityConflictException;
import com.itranswarp.shici.exception.APIEntityNotFoundException;
import com.itranswarp.shici.model.StaticPage;
import com.itranswarp.shici.util.MapUtil;

@RestController
public class StaticPageService extends AbstractService {

	public StaticPage getStaticPage(String id) {
		StaticPage sp = database.fetch(StaticPage.class, id);
		if (sp == null) {
			throw new APIEntityNotFoundException(StaticPage.class);
		}
		return sp;
	}

	public StaticPage getStaticPageByAlias(String alias) {
		StaticPage sp = database.from(StaticPage.class).where("alias=?", alias).first();
		if (sp == null) {
			throw new APIEntityNotFoundException(StaticPage.class);
		}
		return sp;
	}

	public List<StaticPage> getStaticPages() {
		return database.from(StaticPage.class).orderBy("alias").list();
	}

	@RequestMapping(value = "/api/staticpages", method = RequestMethod.GET)
	public Map<String, List<StaticPage>> restGetStaticPages() {
		assertEditorRole();
		return MapUtil.createMap("results", getStaticPages());
	}

	@RequestMapping(value = "/api/staticpages/{id}", method = RequestMethod.GET)
	public StaticPage restGetStaticPage(@PathVariable(value = "id") String spId) {
		assertEditorRole();
		return getStaticPage(spId);
	}

	@RequestMapping(value = "/api/staticpages", method = RequestMethod.POST)
	public StaticPage restCreateStaticPage(@RequestBody StaticPageBean bean) {
		assertEditorRole();
		bean.validate();
		StaticPage exist = database.from(StaticPage.class).where("alias=?", bean.alias).first();
		if (exist != null) {
			throw new APIEntityConflictException("alias", "alias exist");
		}
		// create:
		StaticPage sp = new StaticPage();
		sp.alias = bean.alias;
		sp.name = bean.name;
		sp.content = bean.content;
		database.save(sp);
		return sp;
	}

	@RequestMapping(value = "/api/staticpages/{id}", method = RequestMethod.POST)
	public StaticPage restUpdateStaticPage(@PathVariable("id") String id, @RequestBody StaticPageBean bean) {
		assertEditorRole();
		bean.validate();
		// update:
		StaticPage sp = getStaticPage(id);
		if (!sp.alias.equals(bean.alias)) {
			StaticPage exist = database.from(StaticPage.class).where("alias=?", bean.alias).first();
			if (exist != null) {
				throw new APIEntityConflictException("alias", "alias exist");
			}
		}
		sp.alias = bean.alias;
		sp.name = bean.name;
		sp.content = bean.content;
		database.update(sp);
		return sp;
	}

	@RequestMapping(value = "/api/staticpages/{id}/delete", method = RequestMethod.POST)
	public StaticPage restDeleteStaticPage(@PathVariable("id") String id) {
		assertEditorRole();
		StaticPage sp = getStaticPage(id);
		database.remove(sp);
		return sp;
	}
}
