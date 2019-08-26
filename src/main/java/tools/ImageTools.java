package tools;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import mysql.Datenbank;
import objects.Datei;

public class ImageTools {
	
	/**
	 * 
	 * @param path
	 * @param maxWidth
	 * @param maxHeight
	 * @return Datei with new path (.jpg) and size of file
	 */
	public static Datei resize(String path, int maxWidth, int maxHeight) {
		try {
    		String oldPath = path;
    		
            BufferedImage image = ImageIO.read(new File(oldPath));
            
            int scaledWidth = image.getWidth(null);
    	    int scaledHeight = image.getHeight(null);
    	    
            //Breite anpassen, dann Höhe an die Proportionen anpassen
    	    if (scaledWidth > maxWidth) {
    	    	
    	        scaledWidth = maxWidth;
    	        scaledHeight = (scaledWidth * image.getHeight(null)) / image.getWidth(null);
    	    }

    	    //Höhe anpassen, falls sie zu groß ist, dann Breite an die Proportionen anpassen
    	    if (scaledHeight > maxHeight) {
    	    	
    	        scaledHeight = maxHeight;
    	        scaledWidth = (scaledHeight * image.getWidth(null)) / image.getHeight(null);
    	    }
			
			Image tmp = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
	        BufferedImage resized = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2d = resized.createGraphics();
	        g2d.drawImage(tmp, 0, 0, null);
	        g2d.dispose();
	        
	        Files.delete(Paths.get(oldPath)); //altes Bild löschen
	        
	        String newPath;
	        if(oldPath.toLowerCase().endsWith(".jpg") || oldPath.toLowerCase().endsWith(".jpeg")) {
	        	newPath = oldPath; //Endung beibehalten
	        } else {
	        	String withoutEndung = oldPath.substring(0, oldPath.lastIndexOf("."));
	        	newPath = withoutEndung+".jpg"; //Endung in .jpg umändern
	        }
	        
	        File output = new File(newPath);
	        ImageIO.write(resized, "jpg", output);
	        return new Datei(newPath, output.length());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Verkleinert die Größe des Bildes.
	 * Falls Bild bereits kleiner ist, dann wird ggf. nur in .jpg umgewandelt
	 * 
	 * @param datei Datei-Objekt; muss Pfad und ID besitzen
	 * @param maxWidth Breite
	 * @param maxHeight Höhe
	 */
    public static void resize(Datei datei, int maxWidth, int maxHeight) {
    	
    	Datei result = resize(datei.getPfad(), maxWidth, maxHeight);
    	Datenbank.updateFileSizeAndPath(datei.getDatei_id(), (int) result.getSizeInt(), result.getPfad()); //Änderungen in der Datenbank speichern
    }

}
