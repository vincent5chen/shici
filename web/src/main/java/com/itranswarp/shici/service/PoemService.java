package com.itranswarp.shici.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.bean.CategoryBean;
import com.itranswarp.shici.bean.FeaturedBean;
import com.itranswarp.shici.bean.IdsBean;
import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.model.Category;
import com.itranswarp.shici.model.CategoryPoem;
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
import com.itranswarp.wxapi.util.MapUtil;

@RestController
@Transactional
public class PoemService extends AbstractService {

	@Autowired
	HanziService hanziService;

	// dynasty ////////////////////////////////////////////////////////////////

	public List<Dynasty> getDynasties() {
		return database.from(Dynasty.class).orderBy("displayOrder").list();
	}

	public Dynasty getDynasty(String dynastyId) {
		return database.get(Dynasty.class, dynastyId);
	}

	// poet ///////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/api/{id}/poets", method = RequestMethod.GET)
	public Map<String, List<Poet>> restGetPoets(@PathVariable("id") String dynastyId) {
		return MapUtil.createMap("results", getPoets(dynastyId));
	}

	public List<Poet> getPoets(String dynastyId) {
		return database.from(Poet.class).where("dynastyId=?", dynastyId).orderBy("name").list();
	}

	@RequestMapping(value = "/api/poets/{id}", method = RequestMethod.GET)
	public Poet getPoet(@PathVariable("id") String poetId) {
		return database.get(Poet.class, poetId);
	}

	@RequestMapping(value = "/api/poets", method = RequestMethod.POST)
	public Poet createPoet(@RequestBody PoetBean bean) {
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

	@RequestMapping(value = "/api/poets/{id}", method = RequestMethod.POST)
	public Poet updatePoet(@PathVariable("id") String poetId, @RequestBody PoetBean bean) {
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
		poet.nameCht = hanziService.toCht(bean.name);
		poet.description = bean.description;
		poet.descriptionCht = hanziService.toCht(bean.description);
		poet.birth = bean.birth;
		poet.death = bean.death;
	}

	@RequestMapping(value = "/api/poets/{id}/delete", method = RequestMethod.POST)
	public void deletePoet(@PathVariable("id") String poetId) {
		// check:
		assertEditorRole();
		Poet poet = getPoet(poetId);
		// delete:
		database.remove(poet);
	}

	// poem ///////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/api/poems/{id}", method = RequestMethod.GET)
	public Poem getPoem(@PathVariable("id") String poemId) {
		return database.get(Poem.class, poemId);
	}

	@RequestMapping(value = "/api/poets/{id}/poems", method = RequestMethod.GET)
	public PagedResults<Poem> getPoems(@PathVariable("id") String poetId,
			@RequestParam(value = "page", defaultValue = "1") int pageIndex) {
		return database.from(Poem.class).where("poetId=?", poetId).orderBy("name").list(pageIndex, 20);
	}

	@RequestMapping(value = "/api/poems", method = RequestMethod.POST)
	public Poem createPoem(@RequestBody PoemBean bean) {
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

	@RequestMapping(value = "/api/poems/{id}", method = RequestMethod.POST)
	public Poem updatePoem(@PathVariable("id") String poemId, @RequestBody PoemBean bean) {
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
		poem.nameCht = hanziService.toCht(bean.name);
		poem.content = bean.content;
		poem.contentCht = hanziService.toCht(bean.content);
		poem.appreciation = bean.appreciation;
		poem.appreciationCht = hanziService.toCht(bean.appreciation);
	}

	@RequestMapping(value = "/api/poems/{id}/delete", method = RequestMethod.POST)
	public void deletePoem(@PathVariable("id") String poemId) {
		// check:
		assertEditorRole();
		Poem poem = getPoem(poemId);
		// delete:
		database.remove(poem);
		updatePoemCountOfPoet(poem.poetId);
	}

	private void updatePoemCountOfPoet(String... poetIds) {
		for (String poetId : poetIds) {
			database.update("update Poet set poemCount=(select count(id) from Poem where poetId=?) where id=?", poetId,
					poetId);
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

	// category ///////////////////////////////////////////////////////////////

	@RequestMapping(value = "/api/categories", method = RequestMethod.GET)
	public Map<String, List<Category>> restGetCategories() {
		return MapUtil.createMap("results", getCategories());
	}

	public List<Category> getCategories() {
		return database.from(Category.class).orderBy("displayOrder").list();
	}

	public Category getCategory(String categoryId) {
		return database.get(Category.class, categoryId);
	}

	@RequestMapping(value = "/api/categories", method = RequestMethod.POST)
	public Category createCategory(@RequestBody CategoryBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		// create:
		Category category = new Category();
		category.name = bean.name;
		category.nameCht = hanziService.toCht(bean.name);
		category.description = bean.description;
		category.descriptionCht = hanziService.toCht(bean.description);
		long max = -1;
		for (Category c : getCategories()) {
			max = c.displayOrder;
		}
		category.displayOrder = max + 1;
		database.save(category);
		return category;
	}

	@RequestMapping(value = "/api/categories/{id}", method = RequestMethod.POST)
	public Category updateCategory(@PathVariable("id") String categoryId, @RequestBody CategoryBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		// update:
		Category category = getCategory(categoryId);
		category.name = bean.name;
		category.nameCht = hanziService.toCht(bean.name);
		category.description = bean.description;
		category.descriptionCht = hanziService.toCht(bean.description);
		database.update(category);
		return category;
	}

	public void deleteCategory(String categoryId) {
		// check:
		assertEditorRole();
		Category category = getCategory(categoryId);
		database.remove(category);
	}

	@RequestMapping(value = "/api/categories/{id}/poems", method = RequestMethod.GET)
	public Map<String, List<Poem>> restGetPoemsOfCategory(@PathVariable("id") String categoryId) {
		List<Poem> list = getPoemsOfCategory(categoryId);
		return MapUtil.createMap("results", list);
	}

	public List<Poem> getPoemsOfCategory(@PathVariable("id") String categoryId) {
		return database.list(
				"select p.* from CategoryPoem cp inner join Poem p on cp.poemId=p.id where cp.id=? order by cp.displayOrder",
				categoryId);
	}

	@RequestMapping(value = "/api/categories/{id}/poems", method = RequestMethod.POST)
	public void updatePoemsOfCategory(@PathVariable("id") String categoryId, @RequestBody IdsBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		Category category = getCategory(categoryId);
		// add:
		database.update("delete from CategoryPoem where categoryId=?", categoryId);
		List<CategoryPoem> list = new ArrayList<CategoryPoem>(bean.ids.size());
		long n = 0;
		for (String poemId : bean.ids) {
			CategoryPoem cp = new CategoryPoem();
			cp.categoryId = categoryId;
			cp.poemId = poemId;
			cp.displayOrder = n++;
			list.add(cp);
		}
		database.save(list.toArray(new CategoryPoem[list.size()]));
		database.save(category); // update version!
	}

	@RequestMapping(value = "/api/categories/{id}/sort", method = RequestMethod.POST)
	public void orderCategories(@PathVariable("id") String categoryId, @RequestBody IdsBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		// update orders:
		throw new RuntimeException();
	}

	// featured ///////////////////////////////////////////////////////////////

	@RequestMapping(value = "/api/featured/poem", method = RequestMethod.GET)
	public Poem restGetFeaturedPoem() {
		return getFeaturedPoem(LocalDate.now());
	}

	public Poem getFeaturedPoem(LocalDate targetDate) {
		FeaturedPoem fp = database.from(FeaturedPoem.class).where("pubDate<=?", targetDate).orderBy("pubDate desc")
				.first();
		if (fp == null) {
			throw new EntityNotFoundException(Poem.class);
		}
		return getPoem(fp.poemId);
	}

	@RequestMapping(value = "/api/featured/poems", method = RequestMethod.GET)
	public Map<String, List<Poem>> restGetFeaturedPoems() {
		return MapUtil.createMap("results", getFeaturedPoems());
	}

	public List<Poem> getFeaturedPoems() {
		return database
				.list("select p.* from FeaturedPoem fp inner join Poem p on fp.poemId=p.id order by fp.displayOrder");
	}

	@RequestMapping(value = "/api/featured", method = RequestMethod.POST)
	public void setPoemAsFeatured(@RequestBody FeaturedBean bean) {
		// check:
		assertEditorRole();
		bean.validate();
		Poem poem = getPoem(bean.poemId);
		if (poem.imageId.isEmpty()) {
			throw new APIArgumentException("poemId", "Poem does not have image.");
		}
		FeaturedPoem fp = new FeaturedPoem();
		fp.poemId = bean.poemId;
		fp.pubDate = bean.pubDate;
		database.save(fp);
	}

	@RequestMapping(value = "/api/featured/{poemId}/delete", method = RequestMethod.POST)
	public void setPoemAsUnfeatured(@PathVariable("poemId") String poemId) {
		// check:
		assertEditorRole();
		Poem poem = getPoem(poemId);
		FeaturedPoem fp = database.from(FeaturedPoem.class).where("poemId=?", poem.id).first();
		if (fp == null) {
			throw new APIArgumentException("poemId", "Poem is not featured.");
		}
		database.remove(fp);
	}

}
