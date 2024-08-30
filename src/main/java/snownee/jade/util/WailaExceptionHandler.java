package snownee.jade.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;

public class WailaExceptionHandler {

	private static final Set<IJadeProvider> ERRORS = Sets.newConcurrentHashSet();
	private static boolean NULL_ERROR = false;
	private static final File ERROR_OUTPUT = new File("logs", "JadeErrorOutput.txt");

	public static void handleErr(Throwable e, @Nullable IJadeProvider provider, @Nullable ITooltip tooltip, @Nullable String whoToBlame) {
		if (CommonProxy.isDevEnv()) {
			ExceptionUtils.wrapAndThrow(e);
			return;
		}
		if (provider == null) {
			if (!NULL_ERROR) {
				NULL_ERROR = true;
				writeLog(e, null);
			}
		} else if (!ERRORS.contains(provider)) {
			ERRORS.add(provider);
			writeLog(e, provider);
		}
		if (tooltip != null) {
			String modid = whoToBlame;
			if (provider != null) {
				modid = provider.getUid().getNamespace();
			}
			if (modid == null || ResourceLocation.DEFAULT_NAMESPACE.equals(modid)) {
				modid = Jade.ID;
			}
			tooltip.add(Component.translatable("jade.error", ModIdentification.getModName(modid).orElse(modid))
					.withStyle(ChatFormatting.DARK_RED));
		}
	}

	private static void writeLog(Throwable e, IJadeProvider provider) {
		Jade.LOGGER.error("Caught unhandled exception : [{}] {}", provider, e);
		Jade.LOGGER.error("See JadeErrorOutput.txt for more information");
		Jade.LOGGER.error("", e);
		try {
			FileUtils.writeStringToFile(
					ERROR_OUTPUT,
					DateFormat.getDateTimeInstance().format(new Date()) + "\n" + provider + "\n" + ExceptionUtils.getStackTrace(e) + "\n",
					StandardCharsets.UTF_8,
					true);
		} catch (Exception ignored) {
		}
	}
}
