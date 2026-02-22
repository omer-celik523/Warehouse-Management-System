import java.util.ArrayList;
import java.util.List;

public class RafManager {
    private List<Raf> raflar;
    private RafDao rafDao;

    public RafManager() {
        this.rafDao = new RafDao();

        // Program açıldığında rafları sıfırdan oluşturmak yerine veritabanından çekiyoruz
        this.raflar = rafDao.tumRaflariGetir();

        if (this.raflar == null) {
            this.raflar = new ArrayList<>();
        }
    }

    public int getRafSayisi() {
        return raflar.size();
    }

    public int getRafKapasitesi(int index) {
        if (index >= 0 && index < raflar.size()) {
            return raflar.get(index).getKapasite();
        }
        return 0;
    }

    public void kapasiteGuncelle(int index, int degisim) {
        if (index >= 0 && index < raflar.size()) {
            Raf raf = raflar.get(index);
            raf.setKapasite(raf.getKapasite() + degisim);

            // Kapasite değiştiğinde veritabanını da güncelliyoruz ki kapatıp açınca bozulmasın
            rafDao.rafGuncelle(raf);
        }
    }

    public int getToplamKapasite() {
        int toplam = 0;
        for (Raf r : raflar) {
            toplam += r.getKapasite();
        }
        return toplam;
    }

    public boolean rafSil(int index) {
        if (index >= 0 && index < raflar.size()) {
            Raf silinecek = raflar.get(index);

            // Rafı sistemden silerken veritabanından da siliyoruz
            rafDao.rafSil(silinecek.getId());

            raflar.remove(index);
            return true;
        }
        return false;
    }

    public List<Raf> getRaflar() {
        return raflar;
    }
}