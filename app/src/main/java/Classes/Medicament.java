package Classes;

/**
 * Created by farouk on 15-04-2016.
 */
public class Medicament {
    private int IdMed;
    private String nomMed;
    private int reduction;
    private String type;

    public void setIdMed(int idMed) {
        IdMed = idMed;
    }

    public int getIdMed() {

        return IdMed;
    }

    public String getNomMed() {
        return nomMed;
    }

    public void setNomMed(String nomMed) {
        this.nomMed = nomMed;
    }

    public int getReduction() {
        return reduction;
    }

    public void setReduction(int reduction) {
        this.reduction = reduction;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
