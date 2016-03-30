package com.itranswarp.shici.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.FeaturedPoem;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.warpdb.EntityNotFoundException;
import com.itranswarp.warpdb.PagedResults;

@Component
public class PoemService extends AbstractService {

	@Autowired
	HanzService hanzService;

	// dynasty ////////////////////////////////////////////////////////////////

	public List<Dynasty> getDynasties() {
		return database.from(Dynasty.class).orderBy("displayOrder").list();
	}

	public Dynasty getDynasty(String dynastyId) {
		return database.get(Dynasty.class, dynastyId);
	}

	// poet ///////////////////////////////////////////////////////////////////

	public List<Poet> getPoets(String dynastyId) {
		return database.from(Poet.class).where("dynastyId=?", dynastyId).orderBy("name").list();
	}

	public Poet getPoet(String poetId) {
		return database.get(Poet.class, poetId);
	}

	public Poet createPoet(PoetBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		getDynasty(bean.dynastyId);
		// create:
		Poet poet = new Poet();
		poet.dynastyId = bean.dynastyId;
		poet.name = bean.name;
		poet.nameCht = HanzService.toCht(bean.name);
		poet.description = bean.description;
		poet.descriptionCht = HanzService.toCht(bean.description);
		poet.birth = bean.birth;
		poet.death = bean.death;
		poet.poemCount = 0;
		database.save(poet);
		return poet;
	}

	// poem ///////////////////////////////////////////////////////////////////

	public PagedResults<Poem> getPoems(String poetId, int pageIndex) {
		return database.from(Poem.class).where("poetId=?", poetId).orderBy("name").list(pageIndex, 20);
	}

	public Poem getPoem(String poemId) {
		return database.get(Poem.class, poemId);
	}

	public Poem getFeaturedPoem(LocalDate targetDate) {
		FeaturedPoem fp = database.from(FeaturedPoem.class).where("pubDate<=?", targetDate).orderBy("pubDate desc")
				.first();
		if (fp == null) {
			throw new EntityNotFoundException(Poem.class);
		}
		return getPoem(fp.poemId);
	}

	public List<Poem> getFeaturedPoems() {
		return database
				.list("select p.* from FeaturedPoem fp inner join Poem p on fp.poemId=p.id order by fp.displayOrder");
	}

}
