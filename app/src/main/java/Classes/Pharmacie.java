package Classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by farouk on 14-04-2016.
 */
public class Pharmacie implements Parcelable {
    private String nomPharmacie;
    private String addressPharmacie;
    private String villePharmacie;
    private String codePostalPharmacie;
    private String TelephonPharmacie;
    private String heureOuverture;

    private String heureFermeture;
    public String lat;
    public String lang;
    private float rating;
    private int activation;
    public String getHeureOuverture() {
        return heureOuverture;
    }

    public void setHeureOuverture(String heureOuverture) {
        this.heureOuverture = heureOuverture;
    }

    public void setHeureFermeture(String heureFermeture) {
        this.heureFermeture = heureFermeture;
    }

    public String getHeureFermeture() {
        return heureFermeture;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    private double distance;

    public Pharmacie() {
    }

    //parcel part
    public Pharmacie(Parcel in) {
        String[] data = new String[12];

        in.readStringArray(data);
        this.nomPharmacie = data[0];
        this.addressPharmacie = data[1];
        this.villePharmacie = data[2];
        this.codePostalPharmacie = data[3];
        this.TelephonPharmacie = data[4];
        this.heureOuverture = data[5];
        this.heureFermeture = data[6];
        this.lat = data[7];
        this.lang = data[8];
        this.rating = Float.parseFloat(data[9]);
        this.activation = Integer.parseInt(data[10]);
        this.distance = Double.parseDouble(data[11]);


    }

    public String getAddressPharmacie() {
        return addressPharmacie;

    }

    public void setAddressPharmacie(String addressPharmacie) {
        this.addressPharmacie = addressPharmacie;
    }

    public String getCodePostalPharmacie() {
        return codePostalPharmacie;
    }

    public void setCodePostalPharmacie(String codePostalPharmacie) {
        this.codePostalPharmacie = codePostalPharmacie;
    }

    public String getNomPharmacie() {
        return nomPharmacie;
    }

    public void setNomPharmacie(String nomPharmacie) {
        this.nomPharmacie = nomPharmacie;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getTelephonPharmacie() {
        return TelephonPharmacie;
    }

    public void setTelephonPharmacie(String telephonPharmacie) {
        TelephonPharmacie = telephonPharmacie;
    }

    public String getVillePharmacie() {
        return villePharmacie;
    }

    public void setVillePharmacie(String villePharmacie) {
        this.villePharmacie = villePharmacie;
    }

    public int getActivation() {

        return activation;
    }

    public void setActivation(int activation) {
        this.activation = activation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.nomPharmacie, this.addressPharmacie, this.villePharmacie, this.codePostalPharmacie, this.TelephonPharmacie, this.heureOuverture,this.heureFermeture, String.valueOf(this.lat), String.valueOf(this.lang), String.valueOf(this.rating), String.valueOf(this.activation), String.valueOf(this.distance)});
    }

    public static final Parcelable.Creator<Pharmacie> CREATOR = new Parcelable.Creator<Pharmacie>() {

        @Override
        public Pharmacie createFromParcel(Parcel source) {
// TODO Auto-generated method stub
            return new Pharmacie(source);  //using parcelable constructor
        }

        @Override
        public Pharmacie[] newArray(int size) {
// TODO Auto-generated method stub
            return new Pharmacie[size];
        }
    };


}

