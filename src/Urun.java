import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Urun implements Serializable {
    private String ad; //Ürünün adı
    private String seriNo; //Ürünün seri numarası
    private int miktar; //Ürünün miktarı
    private int rafIndex; //Ürünün son işlem gördüğü raf

    // YENİ EKLENEN: Ürünün bulunduğu tüm rafları tutan liste
    private List<Integer> rafListesi;

    public Urun(String ad, String seriNo, int miktar, int rafIndex) {
        this.ad = ad;
        this.seriNo = seriNo;
        this.miktar = miktar;
        this.rafIndex = rafIndex;

        // Liste başlatılıyor ve ilk raf ekleniyor
        this.rafListesi = new ArrayList<>();
        this.rafListesi.add(rafIndex);
    }

    public void rafEkle(int yeniRafIndex) {
        if (this.rafListesi == null) {
            this.rafListesi = new ArrayList<>();
        }
        if (!this.rafListesi.contains(yeniRafIndex)) {
            this.rafListesi.add(yeniRafIndex);
        }
    }

    // Bir raf silindiğinde, ondan sonra gelen raf numaralarını günceller.
    public void rafIndexleriniKaydir(int silinenRafIndex) {
        if (this.rafListesi != null) {
            for (int i = 0; i < rafListesi.size(); i++) {
                int mevcutRaf = rafListesi.get(i);

                // Eğer ürünün bulunduğu raf, silinen raftan sonraysa (indeksi büyükse)
                // O rafın numarasını 1 azaltmalıyız.
                if (mevcutRaf > silinenRafIndex) {
                    rafListesi.set(i, mevcutRaf - 1);
                }
            }
        }

        // Ana raf indexini de güncellemeyi unutma
        if (this.rafIndex > silinenRafIndex) {
            this.rafIndex--;
        }
    }

    public void rafTasimaGuncellemesi(int eskiRafIndex, int yeniRafIndex) {
        if (this.rafListesi != null) {
            // Eski rafı listeden siliyoruz
            this.rafListesi.remove(Integer.valueOf(eskiRafIndex));
        }
        // Yeni rafı ekliyoruz
        rafEkle(yeniRafIndex);
        // Ana raf indexini güncelliyoruz
        this.rafIndex = yeniRafIndex;
    }

    public String getRafKodlariString() {
        if (this.rafListesi == null || this.rafListesi.isEmpty()) {
            return String.valueOf(rafIndex + 1);
        }
        return rafListesi.stream()
                .sorted() // Küçükten büyüğe sıralar
                .map(r -> String.valueOf(r + 1)) // İndexi (0,1) -> Raf Koduna (1,2) çevirir
                .collect(Collectors.joining(", ")); // Virgülle birleştirir
    }

    // Getter ve Setter
    public String getAd() { return ad; }
    public String getSeriNo() { return seriNo; }
    public int getMiktar() { return miktar; }
    public void setMiktar(int miktar) { this.miktar = miktar; }
    public int getRafIndex() { return rafIndex; }

    //Raf set edilirken listeye de otomatik ekleniyor
    public void setRafIndex(int rafIndex) {
        this.rafIndex = rafIndex;
        rafEkle(rafIndex);
    }
}
