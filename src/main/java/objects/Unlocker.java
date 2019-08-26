package objects;

import java.util.ArrayList;
import java.util.List;

public class Unlocker {
	
	/*
	 * wird von einem Nutzer-Objekt verwaltet
	 * gibt an, welche Gruppen / Beiträge der Nutzer bereits freigeschaltet hat
	 */
	
	private List<Integer> listGruppe, listBeitrag;
	
	public boolean isGruppeUnlocked(int id) {
		return listGruppe != null && listGruppe.contains(id);
	}
	
	public boolean isBeitragUnlocked(int id) {
		return listBeitrag != null && listBeitrag.contains(id);
	}
	
	public void unlockGruppe(int id) {
		if(listGruppe == null)
			listGruppe = new ArrayList<Integer>();
		
		if(!listGruppe.contains(id)) {
			listGruppe.add(id);
		}
	}
	
	public void unlockBeitrag(int id) {
		if(listBeitrag == null)
			listBeitrag = new ArrayList<Integer>();
		
		if(!listBeitrag.contains(id)) {
			listBeitrag.add(id);
		}
	}

}
