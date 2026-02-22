public class Raf {
    private int id;
    private int kapasite;

    // Yeni raf oluştururken
    public Raf(int kapasite) {
        this.kapasite = kapasite;
    }

    // Veritabanından raf okurken
    public Raf(int id, int kapasite) {
        this.id = id;
        this.kapasite = kapasite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKapasite() {
        return kapasite;
    }

    public void setKapasite(int kapasite) {
        this.kapasite = kapasite;
    }
}