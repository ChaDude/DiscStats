package com.summithillsoftware.ultimate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/*
 * Atomic file helper.  When writing a file will create a backup by renaming the file first.  
 * If no errors during writing the backup will be deleted.  Reading will always use the
 * backup file if it exists.
 * New files: a 0-byte file is created before writing a new file
 * 
 * Atomic file consumers should use this helper for 
 * 1.) Writing the file
 * 2.) Reading the file
 * 3.) Deleting the file
 * 4.) Testing existence of the file
 * 5.) Searching for files in a directory
 */

public class AtomicFile {
	public static String BACKUP_SUFFIX = ".bak";
	
	public static boolean exists(File file) {
		if (file.exists()) {
			return true;
		} else {
			File backup = getBackupFile(file);
			return backup.exists() ? backup.length() > 0 : false;
		}
	}
	
	public static Set<String> findFileNames(File directory, String startsWith) {
		Set<String> fileNames = new HashSet<String>();
		File[] allFiles = directory.listFiles();
		for (int i = 0; i < allFiles.length; i++) {
			File file = allFiles[i];
			if (file.getName().startsWith(startsWith)) {
				if (file.getName().endsWith(BACKUP_SUFFIX)) {
					if (file.length() > 0) {
						fileNames.add(file.getName().substring(0, file.getName().length() - BACKUP_SUFFIX.length()));
					}
				} else {
					fileNames.add(file.getName());
				}
			}
		}
		return fileNames;
	}
	
	// will throw runtime ObjectStoreError if write failed
	public static void writeObject(Serializable object, File destination) {
		boolean success = true;
		Exception failure = null;
		boolean isNew = false;
		
		// handle backup
		
		File backup = getBackupFile(destination);
		if (backup.exists()) {
			// last write failed...don't overwrite the backup
		} else {
			if (destination.exists()) {
				// make backup before writing (rename the original to *.bak )
				File originalToRename = new File(destination.getParentFile(), destination.getName());
				originalToRename.renameTo(backup);
			} else {
				// new file..create empty backup
				isNew = true;
				try {
					backup.createNewFile();
				} catch (IOException e) {
					failure = e;
					success = false;
					UltimateLogger.logError( "Error creating file backup for new file " + destination.getAbsolutePath(), e);
				}
			}
		}
	
		// write new contents
	
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(destination);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(object);
		} catch (Exception e) {
			failure = e;
			UltimateLogger.logError( "Error writing object " + object.toString() + " to atomic file " + destination.toString(), e);
			success = false;
			// if new file try and cleanup (but don't compromise state if unsuccessful)
			if (isNew) {
				boolean destDeleted = true;
				if (destination.exists()) {
					destDeleted = destination.delete();
				}
				if (destDeleted) {
					backup.delete();
				}
			}
		} finally {
			try {
				objectOutputStream.close();
				fileOutputStream.close();
			} catch (Exception e2) {
				if (failure == null) {
					failure = e2;
				}
				UltimateLogger.logError( "Unable to close files when saving atomic file", e2);
				success = false;
			}
		}
		
		// remove backup if all went well
		
		if (success) {
			if (backup.exists()) {
				boolean backupDeleted = backup.delete();
				if (!backupDeleted) {
					failure = new Exception("Unable to remove backup file");
				}
			}
		}
		
		if (failure != null) {
			throw new ObjectStoreError("Unable to write object to atomic file", failure);
		}
	}
	
	public static Object readObject(File destination) {
		if (!exists(destination)) {
			return null;
		}
		File backupFile = getBackupFile(destination);
		File inputFile = backupFile.exists() ? backupFile : destination;
		if (!inputFile.exists()) {
			return null;
		} else {
			FileInputStream fileInputStream = null;
			ObjectInputStream objectInputStream = null;
			try {
				fileInputStream = new FileInputStream(inputFile);
				objectInputStream = new ObjectInputStream(fileInputStream);
				Object obj = objectInputStream.readObject();
				return obj;
			} catch (Exception e) {
				UltimateLogger.logError( "Error restoring atomic object from file " + destination.getAbsolutePath(), e);
				throw new ObjectStoreError("Could not restore atomic object", e);
			} finally {
				try {
					objectInputStream.close();
					fileInputStream.close();
				} catch (Exception e2) {
					UltimateLogger.logError( "Unable to close files when restoring atomic object from file " + destination.toString(), e2);
				}
			}
		}
	}
	
	public static boolean delete(File destination) {
		boolean didDeleteOriginal = true;
		boolean didDeleteBackup = true;
		if (destination.exists()) {
			didDeleteOriginal = destination.delete();
		}
		File backup = getBackupFile(destination);
		if (backup.exists()) {
			didDeleteBackup = backup.delete();
		}
		return didDeleteOriginal && didDeleteBackup;
	}
	
	public static boolean rename(File destination, String newName) {
		boolean didRenameOriginal = true;
		boolean didRenameBackup = true;
		if (destination.exists()) {
			File newPath = new File(destination.getParentFile(), newName);
			didRenameOriginal = destination.renameTo(newPath);
		}
		File backup = getBackupFile(destination);
		if (backup.exists()) {
			File newPath = new File(backup.getParentFile(), newName + BACKUP_SUFFIX);
			didRenameBackup = backup.renameTo(newPath);
		}
		return didRenameOriginal && didRenameBackup;
	}
	
	private static File getBackupFile(File file) {
		return new File(file.getParentFile(), file.getName() + BACKUP_SUFFIX);
	}
	
	

}
