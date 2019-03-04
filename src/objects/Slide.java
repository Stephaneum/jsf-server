package objects;

public class Slide {
	
	private static final String DIRECTION_LEFT = "left-align", DIRECTION_TOP = "center-align", DIRECTION_RIGHT = "right-align";
	public static final String DIRECTION_STANDARD = DIRECTION_RIGHT;
	
	private int index;
	private String path, title, sub, direction;
	private String fileName;
	
	public Slide(int index, String path, String title, String sub, String direction) {
		this.index = index;
		this.path = path;
		this.title = title;
		this.sub = sub;
		this.direction = direction;
		this.fileName = path.substring(path.lastIndexOf('/')+1);
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSub() {
		return sub;
	}
	
	public void setSub(String sub) {
		this.sub = sub;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public String getDirectionReadable() {
		switch(direction) {
		case DIRECTION_LEFT:
			return "von links";
		case DIRECTION_TOP:
			return "von oben";
		case DIRECTION_RIGHT:
			return "von rechts";
		default:
			return "von links";
		}
	}
	
	public void changeDirection() {
		if(direction.equals(DIRECTION_LEFT))
			direction = DIRECTION_TOP;
		else if(direction.equals(DIRECTION_TOP))
			direction = DIRECTION_RIGHT;
		else
			direction = DIRECTION_LEFT;
	}

}
