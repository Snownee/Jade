package mcp.mobius.waila.utils;

import mcp.mobius.waila.Waila;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WailaExceptionHandler {

    private static final ArrayList<String> ERRORS = new ArrayList<>();
    private static final File ERROR_OUTPUT = new File("WailaErrorOutput.txt");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss");

    public static void handleErr(Throwable e, String className, List<ITextComponent> tooltip) {
        if (!ERRORS.contains(className)) {
            ERRORS.add(className);

            Waila.LOGGER.error("Caught unhandled exception : [{}] {}", className, e);
            Waila.LOGGER.error("See WailaErrorOutput.txt for more information");
            try {
                FileUtils.writeStringToFile(ERROR_OUTPUT, DATE_FORMAT.format(new Date()) + "\n" + className + "\n" + ExceptionUtils.getStackTrace(e) + "\n", StandardCharsets.UTF_8, true);
            } catch (Exception what) {
                // no
            }
        }
        if (tooltip != null)
            tooltip.add(new StringTextComponent("<ERROR>"));
    }
}
