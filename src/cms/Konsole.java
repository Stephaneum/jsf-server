package cms;

public class Konsole {
	
	final static boolean SHOW = false;
	
	static void info(String s) {
		if(SHOW)
			System.out.println("cms:    [INFO] "+s);
	}
	
	static void error(String s) {
		if(SHOW)
			System.err.println("cms:    [ERROR] "+s);
	}

}
