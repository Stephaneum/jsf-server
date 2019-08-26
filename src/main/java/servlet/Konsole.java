package servlet;

public class Konsole {
	
	final static boolean SHOW = false;
	
	static void info(String s) {
		if(SHOW)
			System.out.println("servlet:    [INFO] "+s);
	}
	
	static void error(String s) {
		if(SHOW)
			System.err.println("servlet:    [ERROR] "+s);
	}

}
