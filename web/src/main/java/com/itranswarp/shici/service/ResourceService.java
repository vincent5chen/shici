package com.itranswarp.shici.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.store.AbstractStorage;
import com.itranswarp.shici.store.RemoteObject;
import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

@Component
public class ResourceService extends AbstractService {

	@Autowired
	AbstractStorage storage;

	public Resource createResource(BaseEntity ref, String name, String ext, byte[] data) throws IOException {
		RemoteObject ro = storage.put(ext, data);
		Resource resource = new Resource();
		resource.meta = "";
		resource.mime = HttpUtil.guessContentType(ext);
		resource.name = name;
		resource.refId = ref.id;
		resource.refType = ref.getClass().getSimpleName();
		resource.size = data.length;
		resource.url = storage.toUrl(ro);
		resource.internalUrl = storage.toInternalUrl(ro);
		if (isImage(ext)) {
			resource.largeImageUrl = storage.toImageUrl(ro, "1e_1c_0o_0l_360h_640w_50q.jpg");
			resource.mediumImageUrl = storage.toImageUrl(ro, "1e_1c_0o_0l_180h_320w_40q.jpg");
			resource.smallImageUrl = storage.toImageUrl(ro, "1e_1c_0o_0l_90h_160w_20q.jpg");
		} else {
			resource.largeImageUrl = resource.mediumImageUrl = resource.smallImageUrl = "";
		}
		database.save(resource);
		return resource;
	}

	boolean isImage(String ext) {
		return ".jpg".equals(ext) || ".png".equals(ext) || ".gif".equals(ext);
	}
}
