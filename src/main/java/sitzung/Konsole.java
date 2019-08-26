package sitzung;

public class Konsole {
	
	final static boolean SHOW = false, COUNTER = true;
	
	static void info(String s) {
		if(SHOW)
			System.out.println("sitzung:    [INFO] "+s);
	}
	
	static void set(int nutzer_id, String variable, String wert) {
		if(SHOW)
			System.out.println("sitzung:    [SET] [NutzerID = "+nutzer_id+"]: "+variable+" = '"+wert+"'");
	}
	
	static void counter(String s) {
		if(COUNTER)
			System.out.println("sitzung:    [COUNTER] "+s);
	}

}
