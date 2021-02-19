package model;

public class Proizvod {
    private int id;
    private String slika;
    private String ime;
    private double cena;
    private String opis;
    private boolean vidljivost;

    public Proizvod(int id, String slika, String ime, double cena, String opis) {
        this.id = id;
        this.slika = slika;
        this.ime = ime;
        this.cena = cena;
        this.opis = opis;
        this.vidljivost = true;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }


    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public boolean getVidljivost() {
        return vidljivost;
    }

    public void setVidljivost(boolean vidljivost) {
        this.vidljivost = vidljivost;
    }
}




