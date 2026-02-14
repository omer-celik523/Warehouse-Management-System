import java.io.Serializable;

public class Raf implements Serializable {
    private int kapasite; // O anki boş kapasite

    public Raf(int kapasite) {
        this.kapasite = kapasite;
    }

    public int getKapasite() { return kapasite; }
    public void setKapasite(int kapasite) { this.kapasite = kapasite; }
}