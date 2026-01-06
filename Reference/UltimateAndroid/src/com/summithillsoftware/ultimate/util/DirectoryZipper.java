package com.summithillsoftware.ultimate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryZipper {

	public static boolean zipDirectoryToFile(File directory, File zipFile) {
		boolean success = true;
		try { 
		    ZipOutputStream outStream = new  ZipOutputStream(new FileOutputStream(zipFile)); 
		    success = zipDir(directory, outStream);
		    outStream.close(); 
		} catch (Exception e) { 
			success = false;
			UltimateLogger.logError("Error zipping directory " + directory.getAbsolutePath(), e);
		}
		return success;
	}
 
	private static boolean zipDir(File directory, ZipOutputStream zipOutputStream) { 
		boolean success = true;
	    try { 
	        String[] dirList = directory.list(); 
	        byte[] readBuffer = new byte[2156]; 
	        int bytesIn = 0; 
	        for(int i = 0; i < dirList.length; i++)  { 
	            File file = new File(directory, dirList[i]); 
	            if(file.isDirectory()) { 
	            	String filePath = file.getPath(); 
	            	zipDir(new File(filePath), zipOutputStream); 
	            } else {
		            FileInputStream fileInputStream = new FileInputStream(file); 
		            ZipEntry anEntry = new ZipEntry(file.getPath()); 
		            zipOutputStream.putNextEntry(anEntry); 
		            while((bytesIn = fileInputStream.read(readBuffer)) != -1) { 
		                zipOutputStream.write(readBuffer, 0, bytesIn); 
		            } 
		           fileInputStream.close(); 
	            }
	        } 
	    } catch(Exception e) 	{ 
	    	success = false;
	    } 
	    return success;
	}
}
