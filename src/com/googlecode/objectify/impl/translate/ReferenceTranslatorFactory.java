package com.googlecode.objectify.impl.translate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Subclass;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.TypeUtils;
import com.googlecode.objectify.repackaged.gentyref.GenericTypeReflector;


/**
 * Converts full entity references to datastore keys and vice-versa.  Also clever enough to look for @Load annotations
 * and possibly return Result<Object> instead of Object, which will cause a delayed fetch.
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class ReferenceTranslatorFactory implements TranslatorFactory<Object>
{
	@Override
	public ValueTranslator<Object, com.google.appengine.api.datastore.Key> create(Path path, Annotation[] fieldAnnotations, Type type, CreateContext ctx)
	{
		final Class<?> clazz = GenericTypeReflector.erase(type);
		
		if (clazz.getAnnotation(Entity.class) == null && clazz.getAnnotation(Subclass.class) == null)
			return null;
		
		final Load load = TypeUtils.getAnnotation(Load.class, fieldAnnotations);
		
		final ObjectifyFactory fact = ctx.getFactory();
		
		return new ValueTranslator<Object, com.google.appengine.api.datastore.Key>(path, com.google.appengine.api.datastore.Key.class) {
			@Override
			protected Object loadValue(com.google.appengine.api.datastore.Key value, LoadContext ctx) {
				return ctx.makeReference(load, clazz, value);
			}
			
			@Override
			protected com.google.appengine.api.datastore.Key saveValue(Object value, SaveContext ctx) {
				return fact.getRawKey(value);
			}
		};
	}
}