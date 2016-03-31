package com.itranswarp.shici.service;
 
import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
 
import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.warpdb.entity.BaseEntity; 

@Component
public class ResourceService extends AbstractService {

	@PostConstruct
	public void init() {
	
	}

	public Resource createResource(BaseEntity ref,  String name, String ext, byte[] data) {
		Resource resource=new Resource();
		resource.meta="";
		resource.mime=HttpUtil.guessContentType(ext);
		resource.name=name;
		resource.refId=ref.id;
		resource.refType=ref.getClass().getSimpleName();
		resource.size=data.length;
		resource.url=url;
		resource.internalUrl=internalUrl;
		database.save(resource);
		return resource;
	}

}
