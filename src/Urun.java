import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Urun {
    private int id;
    private String ad;
    private String seriNo;
    private int miktar;
    private int rafIndex;
    private String eklenmeTarihi;

    // Ürünün hangi rafta kaç adet bulunduğunu takip eden harita
    private Map<Integer, Integer> rafDagilimi;

    // 1. CONSTRUCTOR: Sisteme yeni bir ürün eklenirken kullanılır (Tarih otomatik atanır)
    public Urun(String ad, String seriNo, int miktar, int rafIndex) {
        this.ad = ad;
        this.seriNo = seriNo;
        this.miktar = miktar;
        this.rafIndex = rafIndex;
        this.rafDagilimi = new LinkedHashMap<>();

        if (miktar > 0) {
            this.rafDagilimi.put(rafIndex, miktar);
        }

        //Ürün oluşturulduğu anki saati ve tarihi alıp sisteme işliyor
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        this.eklenmeTarihi = LocalDateTime.now().format(dtf);
    }

    // 2. CONSTRUCTOR: Veritabanından (MySQL) ürün okunurken kullanılır (Tarih DAO'dan set edilir)
    public Urun(int id, String ad, String seriNo, int miktar, int rafIndex) {
        this.id = id;
        this.ad = ad;
        this.seriNo = seriNo;
        this.miktar = miktar;
        this.rafIndex = rafIndex;
        this.rafDagilimi = new LinkedHashMap<>();
    }

    // RAF VE MİKTAR İŞLEMLERİ

    public void rafVeMiktarEkle(int rIndex, int eklenecekMiktar) {
        this.rafDagilimi.put(rIndex, this.rafDagilimi.getOrDefault(rIndex, 0) + eklenecekMiktar);
    }

    public void raftanUrunEksilt(int rIndex, int eksilenMiktar) {
        if (this.rafDagilimi.containsKey(rIndex)) {
            int mevcut = this.rafDagilimi.get(rIndex);
            int kalan = mevcut - eksilenMiktar;
            if (kalan <= 0) {
                this.rafDagilimi.remove(rIndex);
            } else {
                this.rafDagilimi.put(rIndex, kalan);
            }
        }
    }

    public void rafTasimaGuncellemesi(int eskiRaf, int yeniRaf, int tasinacakMiktar) {
        raftanUrunEksilt(eskiRaf, tasinacakMiktar);
        rafVeMiktarEkle(yeniRaf, tasinacakMiktar);
    }

    // Envanter Tablosunda "Raf Dağılımı" sütununu şık göstermek için
    public String getRafKodlariString() {
        if (rafDagilimi == null || rafDagilimi.isEmpty()) return "Raf Yok";
        return rafDagilimi.entrySet().stream()
                .map(e -> (e.getKey() + 1) + ". Raf (" + e.getValue() + " ad.)")
                .collect(Collectors.joining(", "));
    }

    // Raf silindiğinde sağdaki rafların indekslerini sola kaydırır
    public void rafIndexleriniKaydir(int silinenRafIndex) {
        Map<Integer, Integer> yeniDagilim = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> entry : this.rafDagilimi.entrySet()) {
            int mevcutRafIndex = entry.getKey();
            int miktar = entry.getValue();

            if (mevcutRafIndex < silinenRafIndex) {
                // Silinen raftan öncekiler aynen kalır
                yeniDagilim.put(mevcutRafIndex, miktar);
            } else if (mevcutRafIndex > silinenRafIndex) {
                // Silinen raftan sonrakilerin indeksi 1 azalır (sola kayar)
                yeniDagilim.put(mevcutRafIndex - 1, miktar);
            }
        }

        this.rafDagilimi = yeniDagilim;

        // Eğer ürünün ana raf indeksi de kaydıysa onu da güncelle
        if (this.rafIndex > silinenRafIndex) {
            this.rafIndex--;
        }
    }

    // GETTER VE SETTER METOTLARI

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSeriNo() {
        return seriNo;
    }

    public void setSeriNo(String seriNo) {
        this.seriNo = seriNo;
    }

    public int getMiktar() {
        return miktar;
    }

    public void setMiktar(int miktar) {
        this.miktar = miktar;
    }

    public int getRafIndex() {
        return rafIndex;
    }

    public void setRafIndex(int rafIndex) {
        this.rafIndex = rafIndex;
    }

    public Map<Integer, Integer> getRafDagilimi() {
        return rafDagilimi;
    }

    public String getEklenmeTarihi() {
        return eklenmeTarihi != null ? eklenmeTarihi : "-";
    }

    public void setEklenmeTarihi(String eklenmeTarihi) {
        this.eklenmeTarihi = eklenmeTarihi;
    }
}