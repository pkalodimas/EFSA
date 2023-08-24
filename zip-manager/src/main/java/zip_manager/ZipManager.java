package zip_manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZipManager {
	
	private static final Logger LOGGER = LogManager.getLogger(ZipManager.class);
	
	/**
	 * Write a file reading it from the zip stream
	 * @param zipStream
	 * @param filename
	 */
	public static void unzipStream(InputStream inputStream, File file) {
		
		try (ZipInputStream zipStream = new ZipInputStream(inputStream);
				FileOutputStream fos = new FileOutputStream(file);) {
			
			// get the next entry
			zipStream.getNextEntry();
			
			final byte[] buf = new byte[2000];
			
			int length;
			
			// write until there is something
			while ((length = zipStream.read(buf, 0, buf.length)) >= 0) {
				fos.write(buf, 0, length);
			}
			
			fos.close();
			zipStream.close();
			
		} catch (IOException e) {
			LOGGER.error("There was a problem during unzip");
			e.printStackTrace();
		}
	}
	
	static public void extractFolder(String zipFile , String newPath) throws ZipException, IOException {
		
		int BUFFER = 2048;
		File file = new File(zipFile);

		ZipFile zip = new ZipFile(file);
		// String newPath = zipFile.substring(0, zipFile.length() - 4);
		LOGGER.info("Reading zip file " + zip + ".New path for extraction will be " + newPath);

		new File(newPath).mkdir();
		Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath , currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos , BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}
		
		zip.close();
	}
}
