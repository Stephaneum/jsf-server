package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {
	
	public static void main(String[] args) throws IOException {
        //zip("/home/tien/backup/backup","/home/tien/backup/backup.zip");
        //unzip("/home/tien/backup/backup.zip","/home/tien/backup/test");
    }
	
	public static void zip(String fileToZip, String destination) throws IOException {

        FileOutputStream fos = new FileOutputStream(destination);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File file = new File(fileToZip);
 
        _zip(file, null, zipOut);
        zipOut.close();
        fos.close();
        
        Konsole.antwort("zip fertig");
	}
	
	//Rekursive Funktion
    private static void _zip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    	
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
        	Konsole.antwort("Betrete Ordner \""+fileName+"\"");
            File[] children = fileToZip.listFiles();
            for (int i = 0; i < children.length; i++) {
            	
            	File childFile = children[i];
            	
            	String current;
            	if(fileName != null)
            		current = fileName + "/" + childFile.getName();
            	else
            		current = childFile.getName();
            	
            	Konsole.antwort("["+(i+1)+"/"+children.length+"] "+current);
            	
                _zip(childFile, current, zipOut);
            }
            return;
        }
        
        if(fileName != null) {
        	FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
    }
    
    public static void unzip(String fileToUnzip, String destination) throws IOException {
    	
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileToUnzip));
        ZipEntry zipEntry = zis.getNextEntry();
        
        while(zipEntry != null){
        	
            String fileName = zipEntry.getName(); 						// z.B. dateien/23_Bild.png
            String filePath = destination + "/" + fileName; 			// z.B. /home/projekt/dateien/23_Bild.png
            
            File newFile = new File(filePath);
            
            Konsole.antwort(filePath);
            
            newFile.getParentFile().mkdirs(); //fehlende Elternordner erstellen
            
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        
        Konsole.antwort("unzip fertig");
    }
    

}
