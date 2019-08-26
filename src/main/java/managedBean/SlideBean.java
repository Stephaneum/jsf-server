package managedBean;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import objects.Slide;

@ViewScoped
@ManagedBean
public class SlideBean {

	private ArrayList<Slide> slides;

	@PostConstruct
	public void init() {
		slides = Datenbank.getSlides();
	}

	public ArrayList<Slide> getSlides() {
		return slides;
	}

	public void upload(FileUploadEvent event) {

		UploadedFile file = event.getFile();

		if (file == null || file.getFileName().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Datei auswählen."));
			return;
		}

		String filename = file.getFileName().toLowerCase();
		if (!filename.endsWith(".jpg") && !filename.endsWith("jpeg")) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nur JPG-Dateien erlaubt."));
			return;
		}
		
		ArrayList<Slide> temp = Datenbank.getSlides();
		int max = 0;
		for(Slide curr : temp) {
			if(curr.getIndex() > max)
				max = curr.getIndex();
		}
		
		Datenbank.addSlide(file, max+1, "", "", Slide.DIRECTION_STANDARD);
		init();
	}

	public void changeDirection(Slide slide) {
		slide.changeDirection();
		Datenbank.modifySlide(slide.getIndex(), slide.getTitle(), slide.getSub(), slide.getDirection());
	}

	public void delete(Slide slide) {
		Datenbank.deleteSlide(slide.getIndex());
		init();
	}

	public void moveUp(Slide slide) {
		Slide last = null;
		for (Slide curr : slides) {
			if (curr == slide) {
				if (last != null) {
					Datenbank.swapSlides(last, curr);
					init();
				}
				break;
			}

			last = curr;
		}
		init();
	}

	public void moveDown(Slide slide) {
		boolean found = false;
		for (Slide curr : slides) {

			if (found) {
				Datenbank.swapSlides(curr, slide);
				init();
				break;
			}

			if (curr == slide) {
				found = true; // current slide found, wait for next iteration
			}
		}
		init();
	}
	
	public void save() {
		for(Slide slide : slides) {
			Datenbank.modifySlide(slide.getIndex(), slide.getTitle(), slide.getSub(), slide.getDirection());
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderungen gespeichert", slides.size()+" Bilder") );
	}

}
