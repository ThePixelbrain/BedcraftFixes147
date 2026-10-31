package uk.bedcraft.bedcraftfixes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.bedcraft.bedcraftfixes.BedcraftFixesConfig.AutoReference;
import uk.bedcraft.bedcraftfixes.BedcraftFixesConfig.Trilean;
import uk.bedcraft.bedcraftfixes.BedcraftFixesConfig.Default;

import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;
import nilloader.api.NilModList;
import nilloader.api.lib.mini.MiniTransformer;

public class BedcraftFixesPremain implements Runnable {

	public static final NilLogger log = NilLogger.get("BedcraftFixes");

	public static final List<String> transformerTargets = new ArrayList<>();

	private final Map<AutoReference, Boolean> autoPresent = new EnumMap<>(AutoReference.class);

	private static boolean isPresent(String classResource) {
		// Mods aren't on the classpath yet at premain, so look inside the jars FML will load later.
		return ClassLoader.getSystemResource(classResource) != null
				|| anyJarContains(new File("mods"), classResource)
				|| anyJarContains(new File("coremods"), classResource);
	}

	private static boolean anyJarContains(File dir, String entry) {
		File[] files = dir.listFiles();
		if (files == null) return false;
		for (File f : files) {
			if (!f.isFile()) continue;
			String name = f.getName().toLowerCase(Locale.ROOT);
			if (!name.endsWith(".jar") && !name.endsWith(".zip")) continue;
			try (ZipFile zip = new ZipFile(f)) {
				if (zip.getEntry(entry) != null) {
					log.debug("Found {} in {}", entry, f.getName());
					return true;
				}
			} catch (IOException ignored) {}
		}
		return false;
	}

	@Override
	public void run() {
		for (AutoReference ref : AutoReference.values()) {
			autoPresent.put(ref, isPresent(ref.classResource));
		}

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

	private boolean isEnabled(Class<?> clazz, String name) throws NoSuchFieldException, IllegalAccessException {
		ConfigOptions options = clazz.getAnnotation(ConfigOptions.class);
		boolean enabled = true;
		if (options != null) {
			enabled = false;
			for (String o : options.value()) {
				Field f = BedcraftFixesConfig.class.getField(o);
				boolean v;
				if (f.getType() == Trilean.class) {
					Default defAnn = f.getAnnotation(Default.class);
					v = ((Trilean)f.get(null)).resolve(autoPresent.get(defAnn.value()));
				} else if (f.getType() == boolean.class || f.getType() == Boolean.class) {
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
