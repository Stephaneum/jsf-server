package managedBean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import objects.Datei;
import objects.Nutzer;
import servlet.DynamicPreviewServlet;
import sitzung.Sitzung;
import tools.URLManager;

@ManagedBean
@ViewScoped
public class Preview {

	/*
	 * Vorschau einer Datei
	 */

	private String key;
	private Datei datei;
	private String plainText;
	private boolean accepted;
	private boolean displayAsTextAllowed;

	final static private String ENCODE_UTF8 = "UTF-8", ENCODE_ANSI = "ANSI";
	final static private String[] encodes = { ENCODE_UTF8, ENCODE_ANSI };
	private boolean forceTextFile, showEncoding;
	private String encode;

	public void prepare(Datei datei) {
		
		if(datei == null)
			return;

		Nutzer nutzer = Sitzung.getNutzer();
		if (nutzer != null) {
			this.key = nutzer.generateKey();
			this.datei = datei;
			this.plainText = null;
			this.accepted = false;

			this.forceTextFile = false;
			this.showEncoding = false;
			this.encode = ENCODE_UTF8;
			
			this.displayAsTextAllowed = true;
			String endung = datei.calcEndung();
			if(endung.equals("zip") ||
					endung.equals("msi") ||
					endung.equals("exe")) {
				displayAsTextAllowed = false;
			}
		}
	}

	// Spezialfall: Office, timeLimited
	public void accept() {
		DynamicPreviewServlet.addDatei(datei);
		accepted = true;
	}

	public String getPreviewAddress() {
		return URLManager.convertUTF8(URLManager.getMainURL(null), URLManager.CONVERT_TO_URL) + "%2Fpreview%2F%3Fkey%3D"
				+ DynamicPreviewServlet.KEY + "%26file%3D" + datei.getDatei_id();
	}

	public boolean isAccepted() {
		return accepted;
	}

	public String getKey() {
		return key;
	}

	public Datei getDatei() {
		return datei;
	}

	// Text

	public String getPlainText() {
		if (plainText == null) {
			
			InputStream is = null;
			try {
				is = new FileInputStream(datei.getPfad());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			String sEncode;
			if (encode.equals(ENCODE_UTF8))
				sEncode = "UTF8";
			else
				sEncode = "Cp1252"; // ANSI

			BufferedReader buf;
			try {

				buf = new BufferedReader(new InputStreamReader(is, sEncode));

				String line = null;
				try {
					line = buf.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				StringBuilder sb = new StringBuilder();

				while (line != null) {

					sb.append(line).append("\n");
					try {
						line = buf.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					buf.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				plainText = sb.toString();

			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		return plainText;
	}

	public String[] getEncodes() {
		return encodes;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getEncode() {
		return encode;
	}
	
	public boolean isShowEncoding() {
		return showEncoding;
	}
	
	public void showEncoding() {
		showEncoding = true;
	}

	public void clearText() {
		plainText = null; // damit es beim nächsten Mal erneut geladen wird
	}
	
	public boolean isDisplayAsTextAllowed() {
		return displayAsTextAllowed;
	}

	public void forceTextFile() {
		forceTextFile = true;
	}

	public boolean isForceTextFile() {
		return forceTextFile;
	}
}
