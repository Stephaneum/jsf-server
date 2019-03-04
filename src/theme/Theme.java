package theme;

public class Theme {
    
    private String name;
     
    public Theme() {}
 
    public Theme(String name) {
        this.name = name;
    }
     
    @Override
    public String toString() {
        return name;
    }

}
