import java.io.Serializable;

public class Mudur implements Serializable {
    // Sürüm uyumluluğu için best-practice
    private static final long serialVersionUID = 1L;

    private int id; // Veritabanı ID'si
    private String kullaniciAdi;
    private String sifre;

    // Veritabanından veri çekerken kullanacağımız constructor
    public Mudur(int id, String kullaniciAdi, String sifre) {
        this.id = id;
        setKullaniciAdi(kullaniciAdi); // Doğrudan atama yerine güvenlik kalkanından geçiriyoruz
        setSifre(sifre);
    }

    // Yeni kayıt yaparken kullanacağımız constructor (ID'yi veritabanı verecek)
    public Mudur(String kullaniciAdi, String sifre) {
        setKullaniciAdi(kullaniciAdi);
        setSifre(sifre);
    }

    // GETTER & SETTER

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getKullaniciAdi() { return kullaniciAdi; }

    // Boş kullanıcı adı girilmesini engelliyoruz!
    public void setKullaniciAdi(String kullaniciAdi) {
        if (kullaniciAdi == null || kullaniciAdi.trim().isEmpty()) {
            this.kullaniciAdi = "GecersizKullanici"; // Veya hata fırlatılabilir
        } else {
            this.kullaniciAdi = kullaniciAdi.trim();
        }
    }

    public String getSifre() { return sifre; }

    // Boş şifre girilmesini engelliyoruz!
    public void setSifre(String sifre) {
        if (sifre == null || sifre.trim().isEmpty()) {
            this.sifre = "123456"; // Varsayılan acil durum şifresi
        } else {
            this.sifre = sifre.trim(); // Boşlukları (Space) temizle
        }
    }
}