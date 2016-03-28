package com.itranswarp.shici.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringValueResolver;

import com.itranswarp.shici.util.EncryptUtil;

/**
 * Spring PropertySourcesPlaceholderConfigurer extension for load encrypted
 * properties.
 * 
 * @author michael
 */
public class SecurePropertyConfigurer extends PropertySourcesPlaceholderConfigurer {

	final Log log = LogFactory.getLog(getClass());

	@Override
	protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			StringValueResolver valueResolver) {
		super.doProcessProperties(beanFactoryToProcess, new StringValueResolver() {
			@Override
			public String resolveStringValue(String strVal) {
				String value = valueResolver.resolveStringValue(strVal);
				if (value != null && value.startsWith("AES:")) {
					log.info("Try resolve AES-encrypted property: " + value);
					value = EncryptUtil.decryptByAES(value.substring(4));
				}
				return value;
			}
		});
	}

}
