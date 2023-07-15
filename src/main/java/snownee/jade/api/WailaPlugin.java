package snownee.jade.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WailaPlugin {

	/**
	 * Defines a modid required before this plugin can be loaded. Leave it empty if the plugin is included in the same mod.
	 * <p>
	 * On Forge, if this modid is not found, the class will not be loaded. While on other platforms, the class will be loaded, but the plugin will not be registered.
	 *
	 * @return a modid required for this plugin
	 */
	String value() default "";

}
