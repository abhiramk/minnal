/**
 * 
 */
package org.minnal.core.resource;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.minnal.autopojo.AttributeMetaData;
import org.minnal.autopojo.GenerationStrategy;
import org.minnal.autopojo.resolver.ObjectResolver;
import org.minnal.autopojo.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * @author ganeshs
 *
 */
public class BiDirectionalObjectResolver extends ObjectResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(BiDirectionalObjectResolver.class);

	public BiDirectionalObjectResolver(GenerationStrategy strategy) {
		super(strategy);
	}

	/**
	 * @param strategy
	 * @param excludeAnnotations
	 * @param excludeFields
	 */
	public BiDirectionalObjectResolver(GenerationStrategy strategy, List<Class<? extends Annotation>> excludeAnnotations, List<String> excludeFields) {
		super(strategy, excludeAnnotations, excludeFields);
	}
	
	@Override
	protected void setAttribute(Object pojo, AttributeMetaData attribute, Object value) {
		super.setAttribute(pojo, attribute, value);
		JsonManagedReference managedReference = attribute.getAnnotation(JsonManagedReference.class);
		if (managedReference != null && value != null) {
			PropertyDescriptor backReference = getManagedBackReference(value.getClass(), managedReference.value());
			if (backReference != null) {
				try {
					PropertyUtils.setProperty(value, backReference.getName(), pojo);
				} catch (Exception e) {
					logger.info("Failed while setting the property {} on the class {}", backReference.getName(), value.getClass());
				}
			}
		}
	}
	
	private PropertyDescriptor getManagedBackReference(Class<?> clazz, String name) {
		List<PropertyDescriptor> descriptors = PropertyUtil.getPopertiesWithAnnotation(clazz, JsonBackReference.class);
		if (descriptors == null) {
			return null;
		}
		for (PropertyDescriptor descriptor : descriptors) {
			JsonBackReference backReference = PropertyUtil.getAnnotation(descriptor, JsonBackReference.class);
			if (backReference.value().equals(name)) {
				return descriptor;
			}
		}
		return null;
	}

}
