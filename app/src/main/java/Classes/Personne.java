package Classes;

import android.graphics.Picture;

/**
 * Created by farouk on 15-04-2016.
 */
public class Personne {
    private String fullName;
    private String pseudo;
    private String password;
    private Picture photo;
    private String dateInscript;

    public String getDateInscript() {
        return dateInscript;
    }

    public void setDateInscript(String dateInscript) {
        this.dateInscript = dateInscript;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Picture getPhoto() {
        return photo;
    }

    public void setPhoto(Picture photo) {
        this.photo = photo;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}
