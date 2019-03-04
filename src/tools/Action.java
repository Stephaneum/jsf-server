package tools;

public interface Action {
	
	/*
	 * wird von Countdown aufgerufen, sobald die Zeit abgelaufen ist
	 * 
	 * - Verwendung #1 : E-Mail-Timeout
	 * - Verwendung #2 : temporäre URLs: Vorschau
	 * 
	 */
	
	void startAction();

}
