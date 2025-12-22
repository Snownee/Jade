package tobii;
// https://github.com/GazePlay/TobiiStreamEngineForJava

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Tobii {
	private static boolean verbose = true;

	private static boolean loaded = false;

	private static boolean errorReported = false;

	private static String OS = System.getProperty("os.name").toLowerCase();
	private static int tobiiStatus = -2; // 0 = detected / -2 = not detected

	public static float[] gazePosition() {
		try {
			loadIfNotLoaded();
			return jniGazePosition();
		} catch (Throwable e) {
			if (!errorReported) {
				e.printStackTrace();
				errorReported = true;
			}
			return null;
//            return new float[]{0.5f, 0.5f};
		}
	}

	public static void loadIfNotLoaded() throws Exception {
		if (loaded) {
			return;
		}
		loaded = true;
		loadNeededLibraries();
		tobiiStatus = jniInit();
		printIfVerbose("Init code error " + tobiiStatus);
	}

	public static void reloadIfNotLoaded() {
		String dataDirectoryPath = getDataDirectoryPath();
		loadLibrary(dataDirectoryPath, "/lib/tobii/x64/tobii_stream_engine.dll");
		loadLibrary(dataDirectoryPath, "/lib/tobii/x64/tobii_jni_stream_engine.dll");
		tobiiStatus = jniInit();
		printIfVerbose("Init code error " + tobiiStatus);
	}

	public static int getTobiiStatus() {
		return tobiiStatus;
	}

	private static void loadNeededLibraries() throws Exception {

		String dataDirectoryPath = getDataDirectoryPath();

		printIfVerbose("Loading needed libraries using directory " + dataDirectoryPath);

		loadTobiiLibraries(dataDirectoryPath);

	}

	private static String getDataDirectoryPath() {
		String appDataDirectoryPath = "";

		if (isWindows()) {

			appDataDirectoryPath = System.getenv("LocalAppData");

		} else if (isUnix()) {

			appDataDirectoryPath = System.getProperty("user.home");

		} else if (isMac()) {

			//TODO test following commented lines on Mac
            /*appDataDirectoryPath = System.getProperty("user.home");
            appDataDirectoryPath += "/Library/Application Support";*/

		}

		appDataDirectoryPath += "/.tobiiStreamEngineForJava";
		return appDataDirectoryPath;
	}

	private static void loadTobiiLibraries(String dataDirectoryPath) throws Exception {
		if (isWindows()) {

//			copyResourceIntoDir("/lib/tobii/x64/tobii_stream_engine.dll", dataDirectoryPath);
//			copyResourceIntoDir("/lib/tobii/x64/tobii_jni_stream_engine.dll", dataDirectoryPath);

			loadLibrary(dataDirectoryPath, "/lib/tobii/x64/tobii_stream_engine.dll");
			loadLibrary(dataDirectoryPath, "/lib/tobii/x64/tobii_jni_stream_engine.dll");

		} else if (isUnix()) {

//			copyResourceIntoDir("/lib/tobii/x64/libtobii_stream_engine.so", dataDirectoryPath);
//			copyResourceIntoDir("/lib/tobii/x64/libtobii_jni_stream_engine.so", dataDirectoryPath);

			loadLibrary(dataDirectoryPath, "/lib/tobii/x64/libtobii_stream_engine.so");
			loadLibrary(dataDirectoryPath, "/lib/tobii/x64/libtobii_jni_stream_engine.so");

		} else if (isMac()) {

			//TODO Compile and add MacOS libraries here

		}
	}

	private static boolean isWindows() {
		return (OS.contains("win"));
	}

	private static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
	}

	private static boolean isMac() {
		return (OS.contains("mac"));
	}

	private static void copyResourceIntoDir(String resourceFilePath, String dirPath) throws Exception {
		printIfVerbose("Copying " + resourceFilePath + " into " + dirPath);
		InputStream in = Tobii.class.getResourceAsStream(resourceFilePath);
		File tmpFile = new File(dirPath, resourceFilePath);
		tmpFile.getParentFile().mkdirs();
		Files.copy(in, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		in.close();
	}

	private static void loadLibrary(String dirPath, String filePath) {
		File tmpFile = new File(dirPath, filePath);
		printIfVerbose("Loading library " + tmpFile.getAbsolutePath());
		System.load(tmpFile.getAbsolutePath());
	}

	private static void printIfVerbose(String what) {
		if (verbose) {
			System.out.println("Tobii: " + what);
		}
	}

	private static native int jniInit();

	private static native float[] jniGazePosition();
}
