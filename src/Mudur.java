import java.io.Serializable;

public class Mudur implements Serializable {
    private String kullaniciAdi;
    private int sifre;

    public Mudur(String kullaniciAdi, int sifre) {
        this.kullaniciAdi = kullaniciAdi;
        this.sifre = sifre;
    }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public int getSifre() { return sifre; }
}