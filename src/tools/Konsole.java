package tools;

public class Konsole {
	
	final static boolean SHOW_INFO = false, SHOW_ERROR = true;
	
	static void antwort(String message) {
		if(SHOW_INFO)
			System.out.println("tools:      [ANTWORT] "+message);
	}
	
	static void status(int zaehler, int nenner, String message) {
		if(SHOW_INFO)
			System.out.println("tools:      ["+zaehler+"/"+nenner+"] "+message);
	}
	
	static void warn(String warnung) {
		if(SHOW_ERROR)
			System.err.println("mysql:      [WARNUNG] "+warnung);
	}
	
	static void method(String methode) {
		if(SHOW_INFO)
			System.out.println("mysql:   [METHODE] "+methode);
	}

}
