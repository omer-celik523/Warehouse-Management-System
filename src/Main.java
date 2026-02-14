import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //Managerları Yükleme
        RafManager rafManager = new RafManager();
        MudurManager mudurManager = new MudurManager();

        //Giriş Kontrolü
        if (!mudurManager.sistemGiris(rafManager)) {
            return; // Giriş başarısızsa kapat
        }

        //Ürün Yönetimi
        UrunManager urunManager = new UrunManager(rafManager);
        Listeleme listeleme = new Listeleme();

        // MENÜ
        while (true) {
            System.out.println("\n");
            System.out.println("1-Yeni ürün kaydı ekleme");
            System.out.println("2-Ürün stok güncelleme(ekleme)");
            System.out.println("3-Ürün çıkarma");
            System.out.println("4-Ürün taşıma");
            System.out.println("5-İstenilen ürünün bilgilerini görüntüleme");
            System.out.println("6-Tüm depodaki ürünlerin bilgilerini görüntüleme");
            System.out.println("7-Depoya yeni raf ekleme");
            System.out.println("8-Depodan raf çıkarma");
            System.out.println("9-Çıkış");
            System.out.print("Seçiminiz: ");

            while(!scanner.hasNextInt()){
                System.out.println("Lütfen geçerli bir sayı giriniz");
                System.out.print("Seçiminiz:");
                scanner.nextLine();
            }

            int secim = scanner.nextInt();
            scanner.nextLine();

            switch (secim) {
                case 1: urunManager.urunKayitEkle(); break;
                case 2: urunManager.urunEkleme(); break;
                case 3: urunManager.urunCikarma(); break;
                case 4: urunManager.urunTasima(); break;
                case 5: listeleme.listedeArama(urunManager.getUrunler()); break;
                case 6: Listeleme.tumListeYazdir(urunManager.getUrunler()); break;
                case 7: rafManager.yeniRafEkleme(); break;
                case 8: rafManager.rafCikarma(urunManager); break;
                case 9:
                    System.out.println("Çıkış yapılıyor");
                    System.out.println("İyi günler dileriz ☺️");
                    System.exit(0);
                    break;
                default: System.out.println("Geçersiz işlem!");

            }
        }
    }
}
