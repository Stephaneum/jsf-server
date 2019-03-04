package managedBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import mysql.Sicherung;
import objects.Datei;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class BackupBean {

	private static String title, titleShort;
	private static boolean redirect = false;
	
	private Datei[] backups;

	@PostConstruct
	public void init() {
		String backupDir = Datenbank.getBackupDir();
		
		if(backupDir == null)
			return;
		
		File folder = new File(backupDir);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles == null)
			return;
		
		int amount = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				amount++;
			}
		}
		
		backups = new Datei[amount];
		int curr = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				backups[curr] = new Datei(listOfFiles[i].getPath().replace("\\","/"),listOfFiles[i].length());
				curr++;
			}
		}
		
		insertionSort(backups);
	}
	
	public static Datei[] insertionSort(Datei[] sortieren) {
		Datei temp;
		for (int i = 1; i < sortieren.length; i++) {
			temp = sortieren[i];
			int j = i;
			while (j > 0 && sortieren[j - 1].getDatei_name().compareTo(temp.getDatei_name()) < 0) {
				sortieren[j] = sortieren[j - 1];
				j--;
			}
			sortieren[j] = temp;
		}
		return sortieren;
	}
	
	//admin_backup.xhtml
	
	public String getNextBackup() {
		return Sicherung.getNextBackup();
	}

	public void backup() {

		title = "Ein Backup wird durchgeführt.";
		titleShort = "Backup";
		redirect = false;

		Sicherung.startBackup(false); // Backup starten

		new Thread(new Runnable() {

			@Override
			public void run() {
				do {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (Sicherung.getResult() != null) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						redirect = true;
					}
				} while (!redirect);
			}
		}).start();

		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.LOG_SICHERUNG + ".xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void restore(Datei datei) {
		restore(datei.getPfad(), Datenbank.getBackupDir(), Datenbank.getSpeicherort());
	}
	
	public static void restore(String zipFile, String backupDir, String speicherort) {
		title = "Eine Wiederherstellung wird durchgeführt.";
		titleShort = "Wiederherstellung";
		redirect = false;

		Sicherung.startRestore(zipFile, backupDir, speicherort); // Restore starten

		new Thread(new Runnable() {

			@Override
			public void run() {
				do {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (Sicherung.getResult() != null) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						redirect = Sicherung.getResult(); // only redirect if success
						return;
					}
				} while (true);
			}
		}).start();

		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.LOG_SICHERUNG + ".xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Datei[] getBackups() {
		return backups;
	}
	
	public String getPath() {
		return Datenbank.getBackupDir();
	}
	
	public StreamedContent download(Datei datei) {
		try {
	        return new DefaultStreamedContent(new FileInputStream(datei.getPfad()), "application/zip", datei.getDatei_name());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void delete(Datei datei) {
		try {
			Files.delete(Paths.get(datei.getPfad()));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Backup gelöscht",datei.getDatei_name()) );
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void upload(FileUploadEvent event) {
		
		UploadedFile file = event.getFile();
		
		InputStream inputStream;
		OutputStream outputStream;
		try {
			inputStream = file.getInputstream();
			outputStream = new FileOutputStream(new File(Datenbank.getBackupDir()+"/"+file.getFileName()));
			
			int read = 0;
			byte[] bytes = new byte[1024];
			
			//neue Datei schreiben
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Datei hochgeladen",file.getFileName()) );
			init();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	//log_sicherung.xhtml

	public String getTitle() {
		return title;
	}

	public String getTitleShort() {
		return titleShort;
	}

	public String getLog() {
		return Sicherung.getLog();
	}

	public void poll() {
		if (redirect) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.BACKUP + ".xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
