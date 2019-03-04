package objects;

public class BildReihe {
	
	/*
	 * Eine Instanz repräsentiert eine Zeile von Bildern bei "Beiträge/Bilder (Bildermodus)"
	 * 
	 */
	
	private Datei[] bilder;
	
	public BildReihe(Datei[] bilder) {
		this.bilder = bilder;
	}
	
	public Datei[] getBilder() {
		return bilder;
	}

}
