package objects;

import java.text.DecimalFormat;

import tools.URLManager;

public class Datei {
	
	public enum Mode {
		PRIVATSPEICHER,
		KLASSENSPEICHER,
		PROJEKTSPEICHER,
		RUBRIK,
		LEHRERCHAT
	}
	
	final static public int SIZE_B = 0, SIZE_KB = 1, SIZE_MB = 2, SIZE_GB = 3, SIZE_TB = 4;
	final static public int ORDNER_HOME = -1;
	final static public int DEFAULT_MAX_WIDTH = 1000, DEFAULT_MAX_HEIGHT = 1000;
	final static public String DATEI_VERTRETUNG = "vertretungsplan.pdf";
	
	final static private DecimalFormat formater = new DecimalFormat("#.#");
	
	private String datei_name_mit_id; //32_hallo.png
	private String datei_name; //hallo.png
	private String datei_name_ohne_endung; //hallo
	private String datum;
	private String size;
	private long sizeInt;
	private String eigtum;
	private int eigtum_nutzer_id;
	private String mime;
	private int index;
	private int datei_id; //MySQL.datei.id
	private String pfad;
	
	private boolean isPublic;
	
	//Ordner
	private boolean ordner = false;
	private Datei[] kinder = null;
	
	public Datei(int index, int datei_id, String pfad, String datum, int size, String datei_name, String mime, int eigtum_nutzer_id, String eigtum, boolean isPublic) {
		this.index = index;
		this.datei_id = datei_id;
		this.pfad = pfad;
		this.datum = datum;
		this.size = convertSizeToString(size);
		this.sizeInt = size;
		this.datei_name = datei_name;
		this.mime = mime;
		this.eigtum_nutzer_id = eigtum_nutzer_id;
		this.eigtum = eigtum;
		this.datei_name_mit_id = pfad.substring(pfad.lastIndexOf("/")+1);
		this.datei_name_ohne_endung = datei_name.substring(0, datei_name.lastIndexOf("."));
		this.isPublic = isPublic;
	}
	
	//ordner
	public Datei(int ordner_id, String name, Datei[] kinder, long size, int eigtum_nutzer_id, String eigtum) {
		this.index = -1;
		this.datei_id = ordner_id;
		this.pfad = "-";
		this.datum = "-";
		this.size = convertSizeToString(size);
		this.sizeInt = size;
		this.datei_name = name;
		this.mime = null;
		this.eigtum_nutzer_id = eigtum_nutzer_id;
		this.eigtum = eigtum;
		this.datei_name_mit_id = "-";
		this.datei_name_ohne_endung = name;
		this.isPublic = false;
		
		this.ordner = true;
		this.kinder = kinder;
	}
	
	//Konstruktor für addFile (für cms)
	public Datei(int datei_id) {
		this.datei_id = datei_id;
	}
	
	//Konstruktor für Backups, ImageTools
	public Datei(String path, long size) {
		this.pfad = path;
		this.datei_name = path.substring(path.lastIndexOf('/')+1);
		this.size = convertSizeToString(size);
		this.sizeInt = size;
	}
	
	public static String convertSizeToString(long bytes) {
		if(bytes < 1024) {
			return bytes+" Bytes";
		} else if(bytes < 1024*1024) {
			float size_kb = (float)bytes/1024;
			return formater.format(size_kb)+" KB";
		} else if(bytes < 1024*1024*1024) {
			float size_mb = (float)bytes/(1024*1024);
			return formater.format(size_mb)+" MB";
		} else {
			float size_gb = (float)bytes/(1024*1024*1024);
			return formater.format(size_gb)+" GB";
		}
	}
	
	public static int convertToBytes(double input, int startType) {
		for(int i = startType; i > 0; i--) {
			input = input * 1024;
		}
		
		return (int) input;
	}
	
	public static double convertByteTo(double input, int finalType) {
		for(int i = 0; i < finalType; i++) {
			input = input / 1024;
		}
		
		return input;
	}
	
	public String getDatei_name() {
		return datei_name;
	}
	
	public String getDatum() {
		return datum;
	}
	
	public String getEigtum() {
		return eigtum;
	}
	
	public String getSize() {
		return size;
	}
	
	public long getSizeInt() {
		return sizeInt;
	}
	
	//nächsten beiden Methoden für MySQLManager.kumulativSizes
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public void setSizeInt(long sizeInt) {
		this.sizeInt = sizeInt;
	}

	public int getIndex() {
		return index;
	}
	
	public String getPfad() {
		return pfad;
	}
	
	public String getMime() {
		return mime;
	}
	
	public int getDatei_id() {
		return datei_id;
	}
	
	public int getEigtum_nutzer_id() {
		return eigtum_nutzer_id;
	}
	
	public String getDatei_name_mit_id() {
		return datei_name_mit_id;
	}
	
	public String getDatei_name_ohne_endung() {
		return datei_name_ohne_endung;
	}
	
	public String getPublicLink() {
		return "/public/?file="+URLManager.convertUTF8(datei_name_mit_id, URLManager.CONVERT_TO_URL);
	}
	
	public boolean isPublicity() {
		return isPublic;
	}
	
	public void setPublicity(boolean isPublic) {
		this.isPublic = isPublic;
	}
	
	public String getPublicityIcon() {
		if(isPublic)
    		return "fa fa-unlock-alt";
    	else
    		return "fa fa-lock";
	}
	
	public String calcEndung() {
		int index = datei_name.lastIndexOf(".");
		if(index != -1)
			return datei_name.substring(index+1);
		else
			return datei_name;
	}
	
	//Ordner
	
	public boolean isOrdner() {
		return ordner;
	}
	
	public Datei[] getKinder() {
		return kinder;
	}
	
	@Override
	public String toString() {
		if(ordner)
			return "[Ordner] "+datei_name_ohne_endung;
		else
			return "[Datei] "+datei_name_ohne_endung;
	}
	
	public boolean isImage() {
		return isImage(mime);
	}
	
	public boolean isPdf() {
		return isPdf(mime);
	}
	
	public boolean isText() {
		return isText(mime);
	}
	
	public boolean isVideo() {
		return isVideo(mime);
	}
	
	public boolean isAudio() {
		return isAudio(mime);
	}
	
	public boolean isOffice() {
		return isOffice(mime) || datei_name.endsWith(".docx") || datei_name.endsWith(".pptx") || datei_name.endsWith(".xlsx");
	}
	
	public static String toModeString(Mode mode) {
		switch(mode) {
		case PRIVATSPEICHER:
			return "Privatspeicher";
		case KLASSENSPEICHER:
			return "Klassenspeicher";
		case PROJEKTSPEICHER:
			return "Projektspeicher";
		case RUBRIK:
			return "Rubrik-Bild";
		case LEHRERCHAT:
			return "Lehrer-Chat";
		default:
			return "unbekannter Speicher";
		}
	}
	
	public static String toMime(String endung) {
		switch(endung) {
		case "pdf":
			return "application/pdf";
		case "docx":
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		case "xlsx":
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		case "pptx":
			return "application/mspowerpoint";
		case "xml":
			return "application/xml";
		case "png":
			return "image/png";
		case "jpg":
			return "image/jpeg";
		case "jpeg":
			return "image/jpeg";
		case "jpe":
			return "image/jpeg";
		case "gif":
			return "image/gif";
		case "mp4":
			return "video/mp4";
		case "zip":
			return "application/zip";
		case "wav":
			return "audio/x-wav";
		case "htm":
		case "html":
			return "text/html";
		case "js":
			return "text/javascript";
		case "css":
			return "text/css";
		default:
			return null;
		}
	}
	
	public static boolean isImage(String mime) {
		if(mime != null) {
			return mime.startsWith("image");
		} else {
			return false;
		}
	}
	
	public static boolean isPdf(String mime) {
		if(mime != null) {
			return mime.equals("application/pdf");
		} else {
			return false;
		}
	}
	
	public static boolean isText(String mime) {
		if(mime != null) {
			return mime.startsWith("text");
		} else {
			return false;
		}
	}
	
	public static boolean isVideo(String mime) {
		if(mime != null) {
			return mime.startsWith("video");
		} else {
			return false;
		}
	}
	
	public static boolean isAudio(String mime) {
		if(mime != null) {
			return mime.startsWith("audio");
		} else {
			return false;
		}
	}
	
	public static boolean isOffice(String mime) {
		if(mime != null) {
			return mime.startsWith("application/vnd.openxmlformats") || mime.equals("application/msword") || mime.startsWith("application/vnd.ms");
		} else {
			return false;
		}
	}

}
