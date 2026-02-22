import java.util.List;
import java.util.Scanner;

public class Listeleme {
    transient Scanner scanner = new Scanner(System.in);

    public void tumListeYazdir(List<Urun> urunler) {
        if (urunler == null || urunler.isEmpty()) {
            System.out.println("HATA! Depoda herhangi bir ürün bulunmamaktadır.");
            return;
        }

        System.out.println("\n====================== TÜM ÜRÜN ENVANTERİ ======================");
        // Konsolda çok şık, hizalı bir tablo görünümü için "printf" (formatlı yazdırma) kullanıyoruz
        System.out.printf("%-22s | %-15s | %-8s | %-30s\n", "ÜRÜN ADI", "SERİ NO", "STOK", "BULUNDUĞU RAFLAR (DAĞILIM)");
        System.out.println("----------------------------------------------------------------------------------");

        for (Urun u : urunler) {
            System.out.printf("%-22s | %-15s | %-8d | %-30s\n",
                    u.getAd(), u.getSeriNo(), u.getMiktar(), u.getRafKodlariString());
        }
        System.out.println("==================================================================================\n");
    }

    public void listedeArama(List<Urun> urunler) {
        if (urunler == null || urunler.isEmpty()) {
            System.out.println("HATA! Depoda aranacak herhangi bir ürün bulunmamaktadır.");
            return;
        }

        int hak = 5;
        System.out.print("Aramak istediğiniz ürünün seri numarasını giriniz: ");
        String seriNo = scanner.nextLine();

        while (hak > 0) {
            Urun bulunanUrun = null;

            // Ürünü listede ara
            for (Urun u : urunler) {
                // equalsIgnoreCase ile büyük/küçük harf duyarlılığını kaldırdık (Kullanıcı dostu)
                if (u.getSeriNo().equalsIgnoreCase(seriNo.trim())) {
                    bulunanUrun = u;
                    break;
                }
            }

            if (bulunanUrun != null) {
                // Bulunduysa şık bir "Kart" formatında yazdır ve metottan çık
                System.out.println("\n✅ ÜRÜN BAŞARIYLA BULUNDU!");
                System.out.println("--------------------------------------------------");
                System.out.println("Ürün Adı      : " + bulunanUrun.getAd());
                System.out.println("Seri Numarası : " + bulunanUrun.getSeriNo());
                System.out.println("Toplam Stok   : " + bulunanUrun.getMiktar() + " Adet");
                System.out.println("Raf Dağılımı  : " + bulunanUrun.getRafKodlariString());
                System.out.println("--------------------------------------------------\n");
                return;
            }

            // Bulunamadıysa
            hak--;
            if (hak == 0) {
                System.out.println("\n❌ Çok fazla hatalı deneme yaptınız!");
                System.out.println("- Ana menüye yönlendiriliyorsunuz -");
                return;
            }

            System.out.println("HATA! Girdiğiniz seri numarası (" + seriNo + ") sistemde yok. Kalan hakkınız: " + hak);
            System.out.print("Aramak istediğiniz ürünün seri numarasını tekrar giriniz: ");
            seriNo = scanner.nextLine();
        }
    }
}