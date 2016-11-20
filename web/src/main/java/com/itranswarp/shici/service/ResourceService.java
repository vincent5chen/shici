package com.itranswarp.shici.service;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.itranswarp.shici.model.Resource;

@Controller
public class ResourceService extends AbstractService {

	@RequestMapping(value = "/resources/{id}", method = RequestMethod.GET)
	public ModelAndView images(@PathVariable("id") String imageId, HttpServletResponse response) throws IOException {
		Resource r = warpdb.fetch(Resource.class, imageId);
		if (r == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			response.setContentType(r.mime);
			response.setContentLength(r.size);
			OutputStream output = response.getOutputStream();
			output.write(Base64.decodeBase64(r.data));
			output.flush();
		}
		return null;
	}

	public Resource getResource(String resourceId) {
		return warpdb.get(Resource.class, resourceId);
	}
}
