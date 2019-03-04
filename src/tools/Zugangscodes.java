package tools;

import java.util.Random;

public class Zugangscodes {
	
	final public static int STANDARD_LENGTH = 10;
	
	static private Random randm = new Random();
	static private char[] pool = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	//Generieren von Zugangscodes, alle einzigartig
	static public String[] generateZugangscodes(int anzahl, int laenge, String[] codes_alt) {
		
		
		//bereits existierende Codes mit dem neuen leeren array fusionieren
		String[] codes_all = new String[codes_alt.length+anzahl]; //ALLE Zugangscodes
		for(int i = 0; i < codes_alt.length; i++) {
			codes_all[i] = codes_alt[i];
		}
		
		String currentCode;
		
		for(int i = codes_alt.length; i < codes_all.length; i++) { //neue Codes generieren
			do {
				currentCode = generateZugangscode(laenge);
			} while(!checkUnique(codes_all, currentCode)); //Prüfen, ob neuer Code einzigartig ist
			codes_all[i] = currentCode;
		}
		
		//die neuen codes von den alten wieder trennen
		String[] codes_new = new String[anzahl];
		for(int i = 0; i < codes_new.length; i++) {
			codes_new[i] = codes_all[i+codes_alt.length];
		}
		return codes_new;
	}
	
	//Generieren eines Codes, unabhängig von Einzigartigkeit
	static public String generateZugangscode(int laenge) {
		
		char[] temp = new char[laenge];
		for(int i = 0; i < laenge; i++) {
			temp[i] = pool[randm.nextInt(pool.length)];
		}
		return String.valueOf(temp);
	}
	
	//prüfen, ob der neue String (s2) bereits in s1[] vorhanden ist
	static private boolean checkUnique(String[] s1, String s2) {
		for(int i = 0; i < s1.length; i++) {
			if(s1[i] == null)
				return true;
			if(s1[i].equals(s2))
				return false;
		}
		return true;
	}

}
