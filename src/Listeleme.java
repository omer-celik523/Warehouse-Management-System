import java.util.List;
import java.util.Scanner;

public class Listeleme {
    transient Scanner scanner = new Scanner(System.in);

    public static void tumListeYazdir(List<Urun> urunler) {
        if(urunler.isEmpty()){
            System.out.println("HATA! Depoda herhangi bir ürün bulunmamaktadır");
            return;
        }
        System.out.println("-Tüm liste yazdırılıyor-");
        for (Urun u : urunler) {
            System.out.println("Ürün adı:" + u.getAd() + " ,Seri numarası:" + u.getSeriNo() + " ,Miktar:" + u.getMiktar() + " ,Raf kodu:" + u.getRafKodlariString());
        }
    }

    public void listedeArama(List<Urun> urunler) {
        if(urunler.isEmpty()){
            System.out.println("HATA! Depoda herhangi bir ürün bulunmamaktadır");
            return;
        }

        System.out.print("Aramak istediğiniz ürünün seri numarasını giriniz: ");
        String seriNo = scanner.nextLine();

        Urun bulunanUrun = null;
        int hak = 5; // 5 deneme hakkı

        while (bulunanUrun == null) {
            // Listede ara
            for(Urun u : urunler) {
                if(u.getSeriNo().equals(seriNo)) {
                    bulunanUrun = u;
                    break;
                }
            }

            // Bulunamadıysa
            if (bulunanUrun == null) {
                hak--; // Hakkı bir azalt

                if (hak == 0) {
                    System.out.println("\n");
                    System.out.println("Çok fazla hatalı deneme yaptınız!");
                    System.out.println("-Ana menüye yönlendiriliyorsunuz-");
                    return;
                }

                System.out.println("Girdiğiniz seri numarası hatalıdır.Lütfen tekrar deneyiniz!");
                System.out.print("Aramak istediğiniz ürünün seri numarasını giriniz: ");
                seriNo = scanner.nextLine();
            }
        }

        // Bulunduysa yazdır
        System.out.println("Ürün bilgileri-> Ürün adı:"+ bulunanUrun.getAd()+" ,Seri numarası:"+ bulunanUrun.getSeriNo()+" ,Stok miktarı:"+ bulunanUrun.getMiktar()+" ,Raf kodu:"+ bulunanUrun.getRafKodlariString());
    }
}
