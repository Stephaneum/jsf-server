package objects;

public class Klasse {
	
	final private int id;
	final private String string; //12-2
	
	final private int stufe; //12
	final private String suffix; //-2
	
	public Klasse(int id, int stufe, String suffix, String string) {
		this.id = id;
		this.stufe = stufe;
		this.suffix = suffix;
		this.string = string;
	}
	
	public int getId() {
		return id;
	}
	
	public String getString() {
		return string;
	}
	
	public int getStufe() {
		return stufe;
	}
	
	public String getSuffix() {
		return suffix;
	}

}
