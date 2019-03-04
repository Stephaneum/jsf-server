package cms;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ViewScoped
@ManagedBean
public class PostManagerJS {
	
	/*
	 * liefert Variablen für Front-End
	 * global: alle Modis!
	 */
	
	private boolean scrollUp; //Info für Javascript: Scroll to top
	
	public void setScrollUp(boolean scrollUp) {
		this.scrollUp = scrollUp;
	}
	
	public boolean isScrollUp() {
		return scrollUp;
	}

}
