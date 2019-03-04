package tools;

import java.io.File;

public class FileTools {
	
	public static void deleteFolder(File folder, boolean deleteParent) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f, true);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    
	    if(deleteParent)
	    	folder.delete();
	}

}
