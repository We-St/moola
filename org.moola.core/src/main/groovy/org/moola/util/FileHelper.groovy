package org.moola.util

import java.io.File
import java.io.FilenameFilter
import java.net.MalformedURLException
import java.net.URL;

/**
 * A helper class for common file operations
 * @author Stefan Weghofer
 */
class FileHelper {
	
	
	public static String getFileLocation(File file){
		return file.getAbsolutePath()
	}
	
	public static String getFileLocation(String file){
		return file; //(new File(file)).getParent();
	}
	
	public static String readContents(File file){
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    return sb.toString();
		} finally {
		    br.close();
		}
	}
	
	
	public static URL[] getFileUrls(String folder, String extension) throws MalformedURLException
	{
		File f = new File(folder);
		List<URL> urls = new ArrayList<>();
		File[] files = f.listFiles( new FilenameExtensionFilter( extension ) );
		if (files != null)
		{
			for (File file : files)
			{
				urls.add( file.toURI().toURL() );
			}
		}
		return urls.toArray( new URL[urls.size()] );
	}
	
	private static class FilenameExtensionFilter implements FilenameFilter
	{
		private String extension;
		
		public FilenameExtensionFilter(String extension){
			this.extension = extension;	
		}
		
		public boolean accept( File dir, String name )
		{
			return name.toLowerCase().endsWith( this.extension );
		}
	}
}
