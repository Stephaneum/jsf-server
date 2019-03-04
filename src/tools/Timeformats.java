package tools;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

public class Timeformats {
	
	//Da SimpleDateFormat nicht thread-safe ist, wird jedes mal eine neue Instanz erzeugt
	
	//DateTimeFormatter ist hingegen schon thread-safe
	final static public DateTimeFormatter backup = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	final static public DateTimeFormatter dateFull = DateTimeFormatter.ofPattern("EEEE, dd.MMMM yyyy", Locale.GERMANY);
	final static public DateTimeFormatter complete = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");

	/**
	 *  <h1>Sun, 25 Mar 2018 21:54:03 GMT</h1>
	 *  
	 *  fürs HTTP-Protokoll
	 */
	public static SimpleDateFormat http() {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return format;
	}
	
	
	/**
	 *  <h1>2018-03-25</h1>
	 *  
	 *  bestimmte APIs verlangen dieses Schema
	 */
	public static SimpleDateFormat normed() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
	
	/**
	 *  <h1>25.03.2018 22:54:03</h1>
	 *  
	 */
	public static SimpleDateFormat complete() {
		return new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
	}
	
	
	/**
	 *  <h1>25.03.2018 22:54</h1>
	 *  
	 */
	public static SimpleDateFormat completeNoSeconds() {
		return new SimpleDateFormat("dd.MM.yyyy, HH:mm");
	}
	
	/**
	 *  <h1>25.03.2018</h1>
	 *  
	 */
	public static SimpleDateFormat dateOnly() {
		return new SimpleDateFormat("dd.MM.yyyy");
	}
	
	/**
	 *  <h1>22:54</h1>
	 *  
	 */
	public static SimpleDateFormat timeOnly() {
		return new SimpleDateFormat("HH:mm");
	}

}
