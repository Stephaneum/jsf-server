package theme;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.themeswitcher.ThemeSwitcher;

@ViewScoped
@ManagedBean
public class ThemeSwitcherView {
	
	/*
	 * wird bei Konfiguration benötigt: Themes ändern
	 */
	
    private List<Theme> themes;
     
    @ManagedProperty("#{themeService}")
    private ThemeService service;
 
    @PostConstruct
    public void init() {
        themes = service.getThemes();
    }
     
    public List<Theme> getThemes() {
        return themes;
    }
    
    public void saveTheme(AjaxBehaviorEvent ajax) {
    	String selectedTheme = (String) ((ThemeSwitcher)ajax.getSource()).getValue();
        service.setSelectedTheme(selectedTheme);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"neues Design",selectedTheme) );
    }
    
    public String getTheme() {
    	return service.getSelectedTheme();
    }
    
    public void restoreTheme() {
    	service.setSelectedTheme(ThemeService.STANDARD_THEME);
    	service.restoreColors();
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Design wiederhergestellt",ThemeService.STANDARD_THEME) );
    }
    
    //ManagedProperty
    public void setService(ThemeService service) {
        this.service = service;
    }
}