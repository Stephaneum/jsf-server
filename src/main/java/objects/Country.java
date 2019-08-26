package objects;

public class Country {
	
	private final String name, info, url;
	
	public Country(String name, String info, String url) {
		this.name = name;
		this.info = info;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getInfo() {
		return info;
	}
	
	public String getUrl() {
		return url;
	}
}
