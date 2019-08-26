package theme;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import managedBean.Statistiken;

//eager bedeutet, dass diese Klasse beim Programmstart sofort instanziert wird
@ManagedBean(name="themeService", eager = true)
@ApplicationScoped
public class ThemeService {
     
    private List<Theme> themes;
    final static String STANDARD_THEME = "bootstrap";
    private String selectedTheme = STANDARD_THEME;
    
    //Farben
    final static private String STANDARD_BACKGROUND_COLOR = "#eaf2e6",
    		STANDARD_PANEL_COLOR = "#faf8eb",
    		
    		//Materialize
    		STANDARD_BACKGROUND_PUBLIC_COLOR = "#f0f0f0",
    		STANDARD_NAVBAR_COLOR = "#ffffff",
    		STANDARD_NAVBAR_TEXT_COLOR = "#1b5e20",
    		STANDARD_NAVBAR_DROPDOWN_COLOR = "#1b5e20",
    		STANDARD_BEITRAG_LIST_COLOR = "#ffffff",
    		STANDARD_BEITRAG_TEXT_COLOR = "#000",
			STANDARD_LEFT_BUTTON_COLOR = "#1b5e20",
    		STANDARD_LEFT_BUTTON_COLOR_ON = "#2e7d32",
    		STANDARD_BUTTON_COLOR = "#1b5e20",
    		STANDARD_FOOTER_COLOR = "#2a4c2c",
    		STANDARD_DIAGRAM_COLOR = "#85b56e";
    		
    final static private boolean STANDARD_LOGO_COLORED = true,
    							STANDARD_LOGIN_BUTTON_COLORED = true;
    private String backgroundColor = STANDARD_BACKGROUND_COLOR;
    private String panelColor = STANDARD_PANEL_COLOR;
    
    //Materialize
    private String backgroundPublicColor = STANDARD_BACKGROUND_PUBLIC_COLOR;
    private String navbarColor = STANDARD_NAVBAR_COLOR;
    private String navbarTextColor = STANDARD_NAVBAR_TEXT_COLOR;
    private String navbarDropdownColor = STANDARD_NAVBAR_DROPDOWN_COLOR;
    private String beitragListColor = STANDARD_BEITRAG_LIST_COLOR;
    private String beitragTextColor = STANDARD_BEITRAG_TEXT_COLOR;
    private String leftButtonColor = STANDARD_LEFT_BUTTON_COLOR;
    private String leftButtonColorOn = STANDARD_LEFT_BUTTON_COLOR_ON;
    private String buttonColor = STANDARD_BUTTON_COLOR;
    private String footerColor = STANDARD_FOOTER_COLOR;
    
    private boolean logoColored = STANDARD_LOGO_COLORED;
    private boolean loginButtonColored = STANDARD_LOGIN_BUTTON_COLORED;
    
    private static String diagramColor = STANDARD_DIAGRAM_COLOR;
     
    @PostConstruct
    public void init() {
        themes = new ArrayList<Theme>();
        themes.add(new Theme("afterdark"));
        themes.add(new Theme("afternoon"));
        themes.add(new Theme("afterwork"));
        themes.add(new Theme("aristo"));
        themes.add(new Theme("black-tie"));
        themes.add(new Theme("blitzer"));
        themes.add(new Theme("bluesky"));
        themes.add(new Theme("bootstrap"));
        themes.add(new Theme("casablanca"));
        themes.add(new Theme("cupertino"));
        themes.add(new Theme("cruze"));
        themes.add(new Theme("dark-hive"));
        themes.add(new Theme("delta"));
        themes.add(new Theme("dot-luv"));
        themes.add(new Theme("eggplant"));
        themes.add(new Theme("excite-bike"));
        themes.add(new Theme("flick"));
        themes.add(new Theme("glass-x"));
        themes.add(new Theme("home"));
        themes.add(new Theme("hot-sneaks"));
        themes.add(new Theme("humanity"));
        themes.add(new Theme("le-frog"));
        themes.add(new Theme("midnight"));
        themes.add(new Theme("mint-choc"));
        themes.add(new Theme("overcast"));
        themes.add(new Theme("pepper-grinder"));
        themes.add(new Theme("redmond"));
        themes.add(new Theme("rocket"));
        themes.add(new Theme("sam"));
        themes.add(new Theme("smoothness"));
        themes.add(new Theme("south-street"));
        themes.add(new Theme("start"));
        themes.add(new Theme("sunny"));
        themes.add(new Theme("swanky-purse"));
        themes.add(new Theme("trontastic"));
        themes.add(new Theme("ui-darkness"));
        themes.add(new Theme("ui-lightness"));
        themes.add(new Theme("vader"));
    }
     
    public List<Theme> getThemes() {
        return themes;
    }
    
    public void setSelectedTheme(String selectedTheme) {
		this.selectedTheme = selectedTheme;
	}
    
    public String getSelectedTheme() {
		return selectedTheme;
	}
    
    public String getBackgroundColor() {
		return backgroundColor;
	}
    
    public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = "#"+backgroundColor;
	}
    
    public String getPanelColor() {
		return panelColor;
	}
    
    public void setPanelColor(String panelColor) {
		this.panelColor = "#"+panelColor;
	}
    
    public String getBackgroundPublicColor() {
		return backgroundPublicColor;
	}
    
    public void setBackgroundPublicColor(String backgroundPublicColor) {
		this.backgroundPublicColor = "#"+backgroundPublicColor;
	}
    
    public String getNavbarColor() {
		return navbarColor;
	}
    
    public void setNavbarColor(String navbarColor) {
		this.navbarColor = "#"+navbarColor;
	}
    
    public String getNavbarTextColor() {
		return navbarTextColor;
	}
    
    public void setNavbarTextColor(String navbarTextColor) {
		this.navbarTextColor = "#"+navbarTextColor;
	}
    
    public void setNavbarDropdownColor(String navbarDropdownColor) {
		this.navbarDropdownColor = "#"+navbarDropdownColor;
	}
    
    public String getNavbarDropdownColor() {
		return navbarDropdownColor;
	}
    
    public String getBeitragListColor() {
		return beitragListColor;
	}
    
    public void setBeitragListColor(String beitragListColor) {
		this.beitragListColor = "#"+beitragListColor;
	}
    
    public String getBeitragTextColor() {
		return beitragTextColor;
	}
    
    public void setBeitragTextColor(String beitragTextColor) {
		this.beitragTextColor = "#"+beitragTextColor;
	}
    
    public String getLeftButtonColor() {
		return leftButtonColor;
	}
    
    public void setLeftButtonColor(String leftButtonColor) {
		this.leftButtonColor = "#"+leftButtonColor;
	}
    
    public String getLeftButtonColorOn() {
		return leftButtonColorOn;
	}
    
    public void setLeftButtonColorOn(String leftButtonColorOn) {
		this.leftButtonColorOn = "#"+leftButtonColorOn;
	}
    
    public String getButtonColor() {
		return buttonColor;
	}
    
    public void setButtonColor(String buttonColor) {
		this.buttonColor = "#"+buttonColor;
	}
    
    public String getFooterColor() {
		return footerColor;
	}
    
    public void setFooterColor(String footerColor) {
		this.footerColor = "#"+footerColor;
	}
    
    public boolean isLogoColored() {
		return logoColored;
	}
    
    public String getLogo() {
    	return logoColored ? "logo_banner_green.png" : "logo_banner.png";
    }
    
    public void setLogoColored(boolean logoColored) {
		this.logoColored = logoColored;
		String sub = logoColored ? "grünes Logo" : "weißes Logo";
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gespeichert",sub) );
	}
    
    public boolean isLoginButtonColored() {
		return loginButtonColored;
	}
    
    public void setLoginButtonColored(boolean loginButtonColored) {
		this.loginButtonColored = loginButtonColored;
		String sub = logoColored ? "farbiges Login-Button" : "weißes Login-Button";
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gespeichert",sub) );
	}
    
    public String getDiagramColor() {
		return diagramColor;
	}
    
    public void setDiagramColor(String diagramColor) {
		ThemeService.diagramColor = "#"+diagramColor;
		Statistiken.initData();
	}
    
    //damit von Informationen zugegriffen werden kann
    public static String getDiagramColorStatic() {
		return diagramColor;
	}
    
    public void applyWhiteTheme() {
    	restoreColors(); // white theme == default theme
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderungen gespeichert","weißes Design") );
    }
    
    public void applyGreenTheme() {
    	
    	backgroundColor = "#eaf2e6";
        panelColor = "#faf8eb";
        
        backgroundPublicColor = "#eaf2e6";
        navbarColor = "#2e7d32";
        navbarTextColor = "#ffffff";
        navbarDropdownColor = "#1b5e20";
        beitragListColor = "#417744";
        beitragTextColor = "#ffffff";
        leftButtonColor = "#546e7a";
        leftButtonColorOn = "#607d8b";
        buttonColor = "#1b5e20";
        footerColor = "#2a4c2c";
        diagramColor = "#85b56e";
        logoColored = false;
        loginButtonColored = false;
        Statistiken.initData(); //damit die Farbe übernommen wird (braucht man seit neustem nicht mehr)
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderungen gespeichert","grünes Design") );
    }
    
    public void restoreColors() {
    	backgroundColor = STANDARD_BACKGROUND_COLOR;
        panelColor = STANDARD_PANEL_COLOR;
        
        backgroundPublicColor = STANDARD_BACKGROUND_PUBLIC_COLOR;
        navbarColor = STANDARD_NAVBAR_COLOR;
        navbarTextColor = STANDARD_NAVBAR_TEXT_COLOR;
        navbarDropdownColor = STANDARD_NAVBAR_DROPDOWN_COLOR;
        beitragListColor = STANDARD_BEITRAG_LIST_COLOR;
        beitragTextColor = STANDARD_BEITRAG_TEXT_COLOR;
        leftButtonColor = STANDARD_LEFT_BUTTON_COLOR;
        leftButtonColorOn = STANDARD_LEFT_BUTTON_COLOR_ON;
        buttonColor = STANDARD_BUTTON_COLOR;
        footerColor = STANDARD_FOOTER_COLOR;
        diagramColor = STANDARD_DIAGRAM_COLOR;
        logoColored = STANDARD_LOGO_COLORED;
        loginButtonColored = STANDARD_LOGIN_BUTTON_COLORED;
        Statistiken.initData(); //damit die Farbe übernommen wird (braucht man seit neustem nicht mehr)
    }
    
    public void save() {
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Farbe geändert",null) );
    }
}