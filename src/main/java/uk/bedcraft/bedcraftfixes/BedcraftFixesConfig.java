package uk.bedcraft.bedcraftfixes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Optional;

import nilloader.api.NilLogger;
import nilloader.api.lib.qdcss.BadValueException;
import nilloader.api.lib.qdcss.QDCSS;

public class BedcraftFixesConfig {
	@Key("mcpc-java-8")
	@Comment("Allow MCPC to run on Java 8")
	public static boolean mcpcJava8 = true;

	@Key("tickthreading-no-deadlock-recovery")
	@Comment("TT's deadlock recovery is broken on Java 8.\n" +
			"Due to an oversight, the automatic restart on deadlock is broken without this patch.")
	public static boolean ttNoDeadlockRecovery = true;

	@Key("bukkit-event-build-gravigun")
	@Comment("Send a bukkit event when a GraviGun interacts with blocks.\n" +
			"Will only activate on Bukkit Servers if set to AUTO.")
	public static Trilean bukkitEventBuildGraviGun = Trilean.AUTO;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface Comment {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface Key {
		String value();
	}
	
	public enum Trilean {
		AUTO,
		OFF,
		ON,
		;
		
		public boolean resolve(boolean def) {
			if (this == AUTO) return def;
			return this == ON;
		}
	}

	static {
		Class<?> me = BedcraftFixesConfig.class;
		if (me.getClassLoader() != ClassLoader.getSystemClassLoader()) {
			NilLogger.get("BedcraftFixes").debug("Delegating BedcraftFixesConfig within relaunch class loader");
			try {
				Class<?> real = Class.forName(BedcraftFixesConfig.class.getName(), true, ClassLoader.getSystemClassLoader());
				for (Field f : real.getFields()) {
					if (Modifier.isStatic(f.getModifiers())) {
						Field mine = me.getField(f.getName());
						if (f.getType() == boolean.class) {
							mine.set(null, f.get(null));
						} else if (f.getType().isEnum()) {
							mine.set(null, Enum.valueOf((Class)mine.getType(), ((Enum<?>)f.get(null)).name()));
						}
					}
				}
			} catch (Throwable t) {
				throw new AssertionError(t);
			}
		} else {
			File cfg = new File("config/bedcraftfixes.css");
			QDCSS css = QDCSS.load("", "");
			try {
				css = QDCSS.load(cfg);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				BedcraftFixesPremain.log.error("Failed to load bedcraftfixes.css", e);
			}
			StringBuilder out = new StringBuilder("/*\r\n * BedcraftFixes configuration file\r\n * Any unrecognized keys or comments you add will be lost!\r\n */\r\n\r\nfeatures {\r\n");
			try {
				for (Field f : BedcraftFixesConfig.class.getDeclaredFields()) {
					String k = f.getAnnotation(Key.class).value();
					String valueStr;
					if (f.getType() == boolean.class) {
						Optional<Boolean> opt;
						try {
							opt = css.getBoolean("features."+k);
						} catch (BadValueException e) {
							BedcraftFixesPremain.log.warn("Config file is malformed", e);
							opt = Optional.empty();
						}
						boolean v;
						if (opt.isPresent()) {
							v = opt.get();
							f.set(null, v);
						} else {
							v = f.getBoolean(null);
						}
						valueStr = v ? "on" : "off";
					} else if (f.getType() == Trilean.class) {
						Optional<Trilean> opt;
						try {
							opt = css.getEnum("features."+k, Trilean.class);
						} catch (BadValueException e) {
							BedcraftFixesPremain.log.warn("Config file is malformed", e);
							opt = Optional.empty();
						}
						Trilean v;
						if (opt.isPresent()) {
							v = opt.get();
							f.set(null, v);
						} else {
							v = (Trilean)f.get(null);
						}
						valueStr = v.name().toLowerCase(Locale.ROOT);
					} else {
						BedcraftFixesPremain.log.warn("Unknown field type {}", f.getType());
						continue;
					}
					Comment comment = f.getAnnotation(Comment.class);
					if (comment != null) out.append("\t/*\r\n\t * "+comment.value().replace("\n", "\r\n\t * ")+"\r\n\t */\r\n");
					out.append("\t"+k+": "+valueStr+";\r\n\r\n");
				}
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
			out.append("}\r\n");
			try (FileOutputStream fos = new FileOutputStream(cfg)) {
				cfg.getParentFile().mkdirs();
				fos.write(out.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to save BedcraftFixes config");
			}
		}
	}

}
