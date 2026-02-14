import java.util.Scanner;

public class MudurManager {
    private Mudur mudur;
    private final String DOSYA = "mudur.bin";
    transient Scanner scanner = new Scanner(System.in);

    public MudurManager() {
        this.mudur = DosyaIslemleri.yukle(DOSYA);
    }

    public boolean sistemGiris(RafManager rafManager) {
        if (this.mudur == null) {
            return mudurKayit(rafManager);
        } else {
            return mudurGiris();
        }
    }

    private boolean mudurKayit(RafManager rafManager) {
        System.out.println("-Kayıt sistemine Hoşgeldiniz-");

        System.out.print("Kullanıcı adı belirleyiniz:");
        String kullaniciAdi = scanner.nextLine();

        System.out.print("4 basamaklı bir şifre belirleyiniz:");
        while(!scanner.hasNextInt()){
            System.out.print("Lütfen geçerli bir sayı giriniz:");
            scanner.next();
        }
        int sifre = scanner.nextInt();
        while(sifre<1000 || sifre>9999){
            System.out.print("HATA !Lutfen 4 basamaklı bir sifre giriniz:");
            while(!scanner.hasNextInt()){
                System.out.println("Lütfen geçerli bir sayı giriniz");
                scanner.next();
            }
            sifre = scanner.nextInt();
        }

        // ilk kurulumda raf bilgisi istenir
        rafManager.ilkKurulum();

        this.mudur = new Mudur(kullaniciAdi, sifre);
        DosyaIslemleri.kaydet(mudur, DOSYA);
        System.out.println("Kaydınız başarıyla yapıldı");
        return true;
    }

    private boolean mudurGiris() {
        System.out.println("-Giriş Bölümüne Hoşgeldiniz-");
        System.out.print("Kullanıcı Adı:");
        String girilenKAdi = scanner.nextLine();

        System.out.print("Şifre:");
        while(!scanner.hasNextInt()){
            System.out.print("Lütfen 4 basamaklı şifrenizi giriniz:");
            scanner.next();
        }
        int girilenSifre = scanner.nextInt();
        scanner.nextLine();

        if (!(mudur.getKullaniciAdi().equals(girilenKAdi) && mudur.getSifre() == girilenSifre)) {
            int sayac = 2;
            while (sayac > 0) {
                System.out.println("HATALI GİRİŞ TEKRAR DENEYİNİZ!");
                System.out.println("kalan hak " + sayac);
                System.out.print("Kullanıcı Adı:");
                girilenKAdi = scanner.nextLine();
                System.out.print("Şifre:");
                while(!scanner.hasNextInt()){
                    System.out.print("Lütfen 4 basamaklı şifrenizi giriniz:");
                    scanner.next();
                }
                girilenSifre = scanner.nextInt();
                scanner.nextLine();

                if (mudur.getKullaniciAdi().equals(girilenKAdi) && mudur.getSifre() == girilenSifre) {
                    System.out.println("Depo Yönetim Sistemine Hoşgeldiniz☺️");
                    return true;
                }
                sayac--;
            }
            System.out.println("SİSTEM GÜVENLİK SEBEBİ İLE KAPATILIYOR");
            return false;
        } else {
            System.out.println("Depo Yönetim Sistemine Hoşgeldiniz☺️");
            return true;
        }
    }
}
