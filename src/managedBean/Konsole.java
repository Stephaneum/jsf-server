package managedBean;

public class Konsole {
	
	final static boolean SHOW_INFO = false, SHOW_ERROR = false;

	static void warn(String warnung) {
		if(SHOW_ERROR)
			System.err.println("managedBean:      [WARNUNG] "+warnung);
	}
	
	static void method(String methode) {
		if(SHOW_INFO)
			System.out.println("managedBean:   [METHODE] "+methode);
	}
	
	static void antwort(String antwort) {
		if(SHOW_INFO)
			System.out.println("managedBean:      [ANTWORT] "+antwort);
	}

}
