package com.itranswarp.shici.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.FeaturedPoem;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.warpdb.EntityNotFoundException;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.PagedResults;
import com.itranswarp.warpdb.entity.BaseEntity;

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
		copyToPoet(poet, bean);
		database.save(poet);
		return poet;
	}

	public Poet updatePoet(String poetId, PoetBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		getDynasty(bean.dynastyId);
		// update:
		Poet poet = getPoet(poetId);
		copyToPoet(poet, bean);
		database.update(poet);
		return poet;
	}

	private void copyToPoet(Poet poet, PoetBean bean) {
		poet.dynastyId = bean.dynastyId;
		poet.name = bean.name;
		poet.nameCht = hanzService.toCht(bean.name);
		poet.description = bean.description;
		poet.descriptionCht = hanzService.toCht(bean.description);
		poet.birth = bean.birth;
		poet.death = bean.death;
	}

	public void deletePoet(String poetId) {
		// check:
		assertEditorRole();
		Poet poet = getPoet(poetId);
		// delete:
		database.remove(poet);
	}

	// poem ///////////////////////////////////////////////////////////////////

	public Poem getPoem(String poemId) {
		return database.get(Poem.class, poemId);
	}

	public PagedResults<Poem> getPoems(String poetId, int pageIndex) {
		return database.from(Poem.class).where("poetId=?", poetId).orderBy("name").list(pageIndex, 20);
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

	public Poem createPoem(PoemBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		Poet poet = getPoet(bean.poetId);
		Poem poem = new Poem();
		poem.id = IdUtil.next();
		// create image:
		if (bean.imageData != null) {
			Resource resource = createResource(poem, "cover", ".jpg", bean.imageData);
			poem.imageId = resource.id;
		} else {
			poem.imageId = "";
		}
		// create:
		copyToPoem(poem, poet, bean);
		database.save(poem);
		updatePoemCountOfPoet(bean.poetId);
		return poem;
	}

	public Poem updatePoem(String poemId, PoemBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		Poet poet = getPoet(bean.poetId);
		Poem poem = getPoem(poemId);
		String oldPoetId = poem.poetId;
		String newPoetId = bean.poetId;
		copyToPoem(poem, poet, bean);
		// update:
		database.update(poem);
		if (!oldPoetId.equals(newPoetId)) {
			updatePoemCountOfPoet(oldPoetId, newPoetId);
		}
		// update image:
		if (bean.imageData != null) {
			String oldResourceId = poem.imageId;
			Resource resource = createResource(poem, "cover", ".jpg", bean.imageData);
			poem.imageId = resource.id;
			database.updateProperties(poem, "imageId");
			if (!oldResourceId.isEmpty()) {
				deleteResource(oldResourceId);
			}
		}
		return poem;
	}

	private void copyToPoem(Poem poem, Poet poet, PoemBean bean) {
		poem.dynastyId = poet.dynastyId;
		poem.poetId = poet.id;
		poem.poetName = poet.name;
		poem.poetNameCht = poet.nameCht;
		poem.form = bean.form;
		poem.tags = bean.tags;
		poem.name = bean.name;
		poem.nameCht = hanzService.toCht(bean.name);
		poem.content = bean.content;
		poem.contentCht = hanzService.toCht(bean.content);
		poem.appreciation = bean.appreciation;
		poem.appreciationCht = hanzService.toCht(bean.appreciation);
	}

	public void deletePoem(String poemId) {
		// check:
		assertEditorRole();
		Poem poem = getPoem(poemId);
		// delete:
		database.remove(poem);
		updatePoemCountOfPoet(poem.poetId);
	}

	private void updatePoemCountOfPoet(String... poetIds) {
		for (String poetId : poetIds) {
			int count = database.queryForInt("select count(id) from Poem where poetId=?", poetId);
			database.update("update Poet set poemCount=? where id=?", count, poetId);
		}
	}

	// Resource ///////////////////////////////////////////////////////////////

	public Resource getResource(String resourceId) {
		return database.get(Resource.class, resourceId);
	}

	public Resource createResource(BaseEntity ref, String name, String ext, String base64Data) {
		Resource resource = new Resource();
		resource.meta = "";
		resource.mime = HttpUtil.guessContentType(ext);
		resource.name = name;
		resource.refId = ref.id;
		resource.refType = ref.getClass().getSimpleName();
		resource.size = getSizeOfBase64String(base64Data);
		resource.data = base64Data;
		database.save(resource);
		return resource;
	}

	public void deleteResource(String resourceId) {
		Resource resource = new Resource();
		resource.id = resourceId;
		database.remove(resource);
	}

	int getSizeOfBase64String(String base64Data) {
		int n = base64Data.length();
		if (base64Data.endsWith("==")) {
			n = n - 2;
		} else if (base64Data.endsWith("=")) {
			n = n - 1;
		}
		int seg = (n / 4) * 3;
		int mod = n % 4;
		if (mod == 3) {
			seg = seg + 2;
		} else if (mod == 2) {
			seg = seg + 1;
		}
		return seg;
	}

}
