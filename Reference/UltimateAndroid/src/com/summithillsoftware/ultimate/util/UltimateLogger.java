package com.summithillsoftware.ultimate.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.summithillsoftware.ultimate.Constants;
import com.summithillsoftware.ultimate.UltimateApplication;

import android.util.Log;

public class UltimateLogger {
	private static final String LOGS_DIRECTORY = "ultimatelogs";
	private static final long MAX_FILE_SIZE = 250000; // 250k
	private static final String LOG_PREFIX = "log-";
	private static final String LOG_SUFFIX = ".log";	
	
	private static File currentLogFile;
	private static File logAFile;
	private static File logBFile;
	
	static {
		initFilePaths();
		currentLogFile = lastLogWrittenTo();
	}
	
	public static final void logInfo(String s) {
		Log.i(Constants.ULTIMATE, s);
		logEntry(s);
	}
	
	public static final void logError(String s, Throwable t) {
		Log.e(Constants.ULTIMATE, s, t);
		logEntry("ERROR: " + s + "\n" + t.toString() + "\n" + exceptionAsString(t));
	}
	
	public static final void logError(String s) {
		Log.e(Constants.ULTIMATE, s);
		logEntry("ERROR: " + s);
	}
	
	public static final void logWarning(String s) {
		Log.w(Constants.ULTIMATE, s);
		logEntry("WARNING: " + s);
	}
	
	public static final void logWarning(String s, Throwable t) {
		Log.w(Constants.ULTIMATE, s, t);
		logEntry("WARNING: " + s + "\n" + t.toString() + "\n" + exceptionAsString(t));
	}
	
	private static final void logEntry(String s) {
		synchronized (logAFile) {
			FileWriter writer = null;
			try {
				String logEntry = "\n" + new Date().toString() + ": " + s;
				writer = new FileWriter(currentLogFile, true);
				writer.write(logEntry);
				switchFilesIfNeeded();
			} catch (Exception e) {
				Log.e(Constants.ULTIMATE, "Failed to write to log file", e);
			} finally {
				try {
					writer.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public static File getLogsDir() {
		File teamDir = new File(UltimateApplication.current().getFilesDir(),
				LOGS_DIRECTORY);
		if (!teamDir.exists()) {
			teamDir.mkdir();
		}
		return teamDir;
	}
	
	private static void initFilePaths() {
		logAFile = new File(getLogsDir(), LOG_PREFIX + "A" + LOG_SUFFIX);
		logBFile = new File(getLogsDir(), LOG_PREFIX + "B" + LOG_SUFFIX);	
	}
	
	private static File lastLogWrittenTo() {
		long logATimestamp = logAFile.exists() ? logAFile.lastModified() : 0;
		long logBTimestamp = logAFile.exists() ? logBFile.lastModified() : 0;
		return logATimestamp < logBTimestamp ? logBFile : logAFile;
	}
	
	private static void switchFilesIfNeeded() {
		if (currentLogFile.exists() && currentLogFile.length() > MAX_FILE_SIZE) {
			switchLogs();
		}
	}
	
	private static void switchLogs() {
		// switch
		currentLogFile = currentLogFile.equals(logAFile) ? logBFile : logAFile;
		// delete before first write
		if (currentLogFile.exists()) {
			currentLogFile.delete();
		}
	}

	private static String exceptionAsString(Throwable t) {
		String asString = "";
		try {
			StringWriter sWriter = new StringWriter();
			PrintWriter pWriter = new PrintWriter(sWriter);
			t.printStackTrace(pWriter);
			asString = sWriter.toString();
			pWriter.close();
			sWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return asString;
	}

}
