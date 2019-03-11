package objects;

public class StaticFile {
	
	public static final int MODE_MIDDLE = 0, MODE_FULL_WIDTH = 1, MODE_FULL_SCREEN = 2;
	public static final String[] MODE_STRING = {"eingebettet, mittig", "eingebettet, voll", "unverändert"};
	
	private String path;
	private int mode;
	
	public StaticFile(String path, int mode) {
		this.path = path;
		this.mode = mode;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getMode() {
		return mode;
	}
	
	public String getModeString() {
		return MODE_STRING[mode];
	}

}
