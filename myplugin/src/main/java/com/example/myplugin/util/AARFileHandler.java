package com.example.myplugin.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AARFileHandler {

	public static HashSet<String> extractJarFilesFromAARFile(String aarFilePath, String outputDir) {
		HashSet<String> jarFiles = new HashSet<String>();
		try {
			
			File outputDirectory = new File(outputDir);
			outputDirectory.mkdirs();
			ZipFile zipFile = new ZipFile(aarFilePath);
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				if (entry.getName().toLowerCase().endsWith(".jar")) {
//					System.out.println(entry.getName());
					InputStream is = zipFile.getInputStream(entry);
					byte[] b = new byte[1024];
					int length = -1;
					File outputFileDir = new File(outputDirectory.getAbsolutePath() + File.separator + (new File(aarFilePath)).getName() + File.separator );
//					outputFileDir.mkdirs();
					File outputFile = new File(outputFileDir.getAbsolutePath() + File.separator + entry.getName());
					outputFile.getParentFile().mkdirs();					
//					outputFile.createNewFile();
					jarFiles.add(outputFile.getAbsolutePath());
					FileOutputStream fos = new FileOutputStream(outputFile);										
					while (( length=is.read(b)) !=-1){
						fos.write(b, 0, length);						
					}					
					fos.flush();
					fos.close();					
					is.close();					
				}
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jarFiles;
	}
	
	

}
