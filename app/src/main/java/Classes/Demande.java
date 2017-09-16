package Classes;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by farouk on 15-04-2016.
 */
public class Demande {
    private Utilsateur user ;
    private String date ;
    private LatLng position;
    private Medicament med ;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Medicament getMed() {
        return med;
    }

    public void setMed(Medicament med) {
        this.med = med;
    }

    public  LatLng getPosition() {
        return position;
    }

    public void setPosition( LatLng position) {
        this.position = position;
    }

    public Utilsateur getUser() {
        return user;
    }

    public void setUser(Utilsateur user) {
        this.user = user;
    }
}
