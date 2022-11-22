package snownee.jade.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;

public class WailaExceptionHandler {

	private static final Set<IJadeProvider> ERRORS = Sets.newHashSet();
	private static final File ERROR_OUTPUT = new File("logs", "JadeErrorOutput.txt");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss");

	public static void handleErr(Throwable e, @Nullable IJadeProvider provider, @Nullable ITooltip tooltip) {
		if (PlatformProxy.isDevEnv()) {
			ExceptionUtils.rethrow(e);
			return;
		}
		if (!ERRORS.contains(provider)) {
			ERRORS.add(provider);

			Jade.LOGGER.error("Caught unhandled exception : [{}] {}", provider, e);
			Jade.LOGGER.error("See JadeErrorOutput.txt for more information");
			try {
				FileUtils.writeStringToFile(ERROR_OUTPUT, DATE_FORMAT.format(new Date()) + "\n" + provider + "\n" + ExceptionUtils.getStackTrace(e) + "\n", StandardCharsets.UTF_8, true);
			} catch (Exception what) {
				// no
			}
		}
		if (tooltip != null) {
			String modid = null;
			if (provider != null) {
				modid = provider.getUid().getNamespace();
			}
			if (modid == null || modid.equals("minecraft")) {
				modid = Jade.MODID;
			}
			tooltip.add(Component.translatable("jade.error", ModIdentification.getModName(modid)).withStyle(ChatFormatting.DARK_RED));
		}
	}
}
