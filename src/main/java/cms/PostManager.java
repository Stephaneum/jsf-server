package cms;

import sitzung.Sitzung;
import tools.Jwt;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;

@ViewScoped
@ManagedBean
public class PostManager {
	
	/*
	 * Koordiniert die Anzeige zwischen
	 * CreatePost (Admin) - EditPost - ApprovePost - CreatePost (NICHT-Admin) - EditUnapproved (NICHT-Admin) - EditImpressum
	 *      [0]               [1]          [2]            [3]                          [4]                    [5]
	 * 
	 */
	
	private int selectID = -1; //0 createAdmin, 1 edit, 2 approve, 3 createNoAdmin
	
	@ManagedProperty("#{editPost}")
	private EditPost editPost;
	
	@ManagedProperty("#{editImpressum}")
	private EditImpressum editImpressum;
	
	@ManagedProperty("#{approvePost}")
	private ApprovePost approvePost;
	
	@ManagedProperty("#{createPost}")
	private CreatePost createPost;
	
	@ManagedProperty("#{editUnapproved}")
	private EditUnapproved editUnapproved;
	
	
	@PostConstruct
	void init() {
		if(createPost.isAdminMode() || createPost.getRubrik() != null) {
			selectID = 0;
		} else {
			selectID = 3;
		}
	}
	
	public void select(int id) {
		
		selectID = id;
		
		//neu initialisieren
		switch(selectID) {
		case 0:
			createPost.init();
			break;
		case 1:
			editPost.init();
			break;
		case 2:
			approvePost.init();
			break;
		case 3:
			createPost.init();
			break;
		case 4:
			editUnapproved.init();
			break;
		case 5:
			editImpressum.init();
			break;
		}
	}
	
	public int getSelectID() {
		return selectID;
	}
	
	public void setApprovePost(ApprovePost approvePost) {
		this.approvePost = approvePost;
	}
	
	public void setCreatePost(CreatePost createPost) {
		this.createPost = createPost;
	}
	
	public void setEditPost(EditPost editPost) {
		this.editPost = editPost;
	}
	
	public void setEditImpressum(EditImpressum editImpressum) {
		this.editImpressum = editImpressum;
	}
	
	public void setEditUnapproved(EditUnapproved editUnapproved) {
		this.editUnapproved = editUnapproved;
	}

	public void toBeta() throws IOException {
		String token = Jwt.generateToken(Sitzung.getNutzer().getNutzer_id());
		FacesContext.getCurrentInstance().getExternalContext().redirect("beitrag-manager?key=" + token);
	}

}
