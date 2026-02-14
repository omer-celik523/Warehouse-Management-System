import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RafManager {
    private List<Raf> raflar;
    private final String DOSYA = "raflar.bin";
    transient Scanner scanner = new Scanner(System.in);

    public RafManager() {
        raflar = DosyaIslemleri.yukle(DOSYA);
        if (raflar == null)
            raflar = new ArrayList<>();
    }

    // Hak kontrolü
    private boolean hakTukendiMi(int[] hak) {
        if (hak[0] <= 0) {
            System.out.println("\n");
            System.out.println("Çok fazla hatalı deneme yaptınız");
            System.out.println("-Ana menüye dönülüyor-");
            return true;
        }
        return false;
    }

    // Güvenli sayı alma
    private Integer sayiIstegi(String soruMetni, int[] hak) {
        while (!scanner.hasNextInt()) {
            hak[0]--; // Harf girerse hak düşer
            if (hakTukendiMi(hak)) return null;

            System.out.println("Lütfen geçerli bir sayı giriniz");
            System.out.print(soruMetni); // Hangi soruda kaldıysa onu tekrar basar
            scanner.next(); // Buffer temizle
        }
        return scanner.nextInt();
    }

    public void ilkKurulum() {

        System.out.print("Depoda kaç raf olacağını giriniz:");
        while (!scanner.hasNextInt()) {
            System.out.println("Lütfen geçerli bir sayı giriniz");
            System.out.println("Depoda kaç raf olacağını giriniz:");
            scanner.next();

        }
        int rafSayisi = scanner.nextInt();
        raflar.clear();
        for (int i = 0; i < rafSayisi; i++) {
            System.out.print((i + 1) + " nolu rafın kapasitesi: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Lütfen geçerli bir sayı giriniz");
                System.out.print((i + 1) + " nolu rafın kapasitesi: ");
                scanner.next();
            }
            int kapasite = scanner.nextInt();
            while (kapasite <= 0) {
                System.out.println("Lütfen pozitif bir değer giriniz");
                System.out.print((i + 1) + " nolu rafın kapasitesi: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("Lütfen geçerli bir sayı giriniz");
                    System.out.print((i + 1) + " nolu rafın kapasitesi: ");
                    scanner.next();
                }
                kapasite = scanner.nextInt();
            }
            raflar.add(new Raf(kapasite));
        }
        DosyaIslemleri.kaydet(raflar, DOSYA);
        scanner.nextLine();
    }

    public void yeniRafEkleme() {
        int[] hak = {5}; // Hak sayacı

        System.out.print("Eklenecek rafların sayısını giriniz:");
        Integer eklenecekSayi = sayiIstegi("Eklenecek rafların sayısını giriniz:", hak);
        if (eklenecekSayi == null)
            return;

        int mevcutRafSayisi = raflar.size();

        for (int i = 0; i < eklenecekSayi; i++) {
            hak[0] = 5;

            String soru = (mevcutRafSayisi + i + 1) + " nolu rafın kapasitesi:";
            System.out.print(soru);

            Integer kapasite = sayiIstegi(soru, hak);
            if (kapasite == null) return;

            while (kapasite < 0) {
                hak[0]--;
                if (hakTukendiMi(hak)) return;
                System.out.println("Kapasite negatif olamaz.");
                System.out.print(soru);
                kapasite = sayiIstegi(soru, hak);
                if (kapasite == null) return;
            }

            raflar.add(new Raf(kapasite));
        }
        DosyaIslemleri.kaydet(raflar, DOSYA);
        System.out.println("Raflar başarılı bir şekilde eklendi");
        scanner.nextLine();
    }

    public void rafCikarma(UrunManager urunManager) {
        if (raflar.isEmpty()) {
            System.out.println("HATA! Depoda çıkarılacak bir raf bulunmamaktadır");
            System.out.println("-Ana menüye yönlendiriliyorsunuz-");
            return;
        }

        int[] hak = {5}; // Hak sayacı

        System.out.print("Depodan kaç adet raf çıkarılacağını giriniz: ");
        Integer secim = sayiIstegi("Lütfen geçerli bir sayı giriniz:", hak);
        if (secim == null)
            return;

        if (secim > raflar.size()) {
            System.out.println("HATA! Depoda sadece " + raflar.size() + " adet raf var");
            System.out.println("-Ana menüye yönlendiriliyorsunuz-");
            return;
        }

        int basariylaSilinen=0; // Kaç rafın silindiğini takip eden sayaç

        islemdongusu:
        for (int i = 0; i < secim; i++) {
            boolean gecerliSilme = false;

            hak[0] = 5;

            while (!gecerliSilme) {
                String soru = (i + 1) + ".Silmek istediğiniz rafın kodunu giriniz (1-" + raflar.size() + "): ";
                System.out.print(soru);

                Integer silmeIndexInput = sayiIstegi(soru, hak); // sayı mı diye bakar
                if (silmeIndexInput == null)
                    return;

                int silmeIndex = silmeIndexInput - 1;

                if (silmeIndex >= raflar.size() || silmeIndex < 0) {
                    hak[0]--; // Mantıksal hata -> Hak düş
                    if (hakTukendiMi(hak))
                        return;

                    System.out.println("HATA! Geçersiz raf numarası. Tekrar deneyin");
                    continue;
                }

                if (urunManager.rafDoluMu(silmeIndex)) {
                    hak[0]--; // Dolu rafı silmeye çalışmak da bir hatadır -> Hak düş
                    if (hakTukendiMi(hak))
                        return;
                    System.out.println("HATA! " + (silmeIndex + 1) + " nolu raf dolu. Önce içindeki ürünleri taşıyın veya boşaltın.");
                    if (basariylaSilinen > 0) {
                        System.out.println("BİLGİ: Dolu rafa gelene kadar " + basariylaSilinen + " adet raf başarıyla silindi ve kaydedildi.");
                    } else {
                        System.out.println("BİLGİ: Hiçbir raf silinemedi.");
                    }
                    System.out.println("-Ana menüye yönlendiriliyorsunuz-");
                    return;
                }

                raflar.remove(silmeIndex);
                urunManager.rafSilindiktenSonraGuncelle(silmeIndex);
                DosyaIslemleri.kaydet(raflar, DOSYA);
                System.out.println("Raf başarıyla silindi.");
                basariylaSilinen++;
                gecerliSilme = true;
            }
        }
        System.out.println("Tüm silme işlemleri tamamlandı.");
        scanner.nextLine();
    }

    // metodlar
    public int getToplamKapasite() {
        int toplam = 0;
        for (Raf r : raflar) toplam += r.getKapasite();
        return toplam;
    }

    public int getRafSayisi() {
        return raflar.size();
    }

    public int getRafKapasitesi(int index) {
        return raflar.get(index).getKapasite();
    }

    public void kapasiteGuncelle(int index, int miktarDegisimi) {
        int mevcut = raflar.get(index).getKapasite();
        raflar.get(index).setKapasite(mevcut + miktarDegisimi);
        DosyaIslemleri.kaydet(raflar, DOSYA);
    }
}
