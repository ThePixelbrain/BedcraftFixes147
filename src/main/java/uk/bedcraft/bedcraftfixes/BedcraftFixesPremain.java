package uk.bedcraft.bedcraftfixes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;
import nilloader.api.NilModList;
import nilloader.api.lib.mini.MiniTransformer;

public class BedcraftFixesPremain implements Runnable {

	public static final NilLogger log = NilLogger.get("BedcraftFixes");

	public static final List<String> transformerTargets = new ArrayList<>();

	@Override
	public void run() {
		try (ZipFile zip = new ZipFile(NilModList.getById("bedcraftfixes").get().source)) {
			for (ZipEntry en : asIterable(zip::entries)) {
				String name = en.getName();
				if (name.endsWith("Transformer.class")) {
					name = name.substring(0, name.length()-6).replace('/', '.');
					Class<?> clazz = Class.forName(name);
					if (ClassTransformer.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
						boolean enabled = isEnabled(clazz, name);
						if (enabled) {
							ClassTransformer ct = (ClassTransformer)clazz.newInstance();
							if (ct instanceof MiniTransformer) {
								transformerTargets.add(((MiniTransformer)ct).getClassTargetName());
							}
							ClassTransformer.register(ct);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to discover transformers", e);
		}
	}

	private static boolean isEnabled(Class<?> clazz, String name) throws NoSuchFieldException, IllegalAccessException {
		ConfigOptions options = clazz.getAnnotation(ConfigOptions.class);
		boolean enabled = true;
		if (options != null) {
			enabled = false;
			for (String o : options.value()) {
				Field f = BedcraftFixesConfig.class.getField(o);
				boolean v;
				if (f.getType() == boolean.class || f.getType() == Boolean.class) {
					v = (Boolean)f.get(null);
				} else {
					throw new ClassCastException(f.getType()+" is not boolean-convertible while looking up option "+o+" for "+ name);
				}
				if (v) {
					enabled = true;
					break;
				}
			}
		}
		return enabled;
	}

	private static <T> Iterable<T> asIterable(Supplier<Enumeration<T>> sup) {
		return () -> {
			Enumeration<T> e = sup.get();
			return new Iterator<T>() {
				@Override
				public boolean hasNext() { return e.hasMoreElements(); }
				@Override
				public T next() { return e.nextElement(); }
			};
		};
	}
	
}
