import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UrunManager {
    private List<Urun> urunler;
    private RafManager rafManager;
    private final String DOSYA = "urunler.bin";
    transient Scanner scanner = new Scanner(System.in);

    public UrunManager(RafManager rafManager) {
        this.rafManager = rafManager;
        this.urunler = DosyaIslemleri.yukle(DOSYA);
        if (this.urunler == null) this.urunler = new ArrayList<>();
    }

    //Hata yapma hakkı
    private boolean hakTukendiMi(int[] hak) {
        if (hak[0] <= 0) {
            System.out.println("\n");
            System.out.println("Çok fazla hatalı deneme yaptınız");
            System.out.println("-Ana menüye yönlendiriliyorsunuz-");
            return true; // Hak bitti, return et
        }
        return false; // Hala hak var
    }

    //geçerli bir sayı isteği
    private Integer sayiIstegi(String soruMetni, int[] hak) {
        while (!scanner.hasNextInt()) {
            hak[0]--; // Harf girdiği için hak düş
            if (hakTukendiMi(hak)) return null;

            System.out.println("Lütfen geçerli bir sayı giriniz!");
            System.out.print(soruMetni);
            scanner.next(); // Hatalı veriyi temizle
        }
        int girilenSayi = scanner.nextInt();
        scanner.nextLine();
        return girilenSayi;
    }

    //depoya yeni ürün kaydı ekler
    public void urunKayitEkle() {
        //GENEL DEPO KONTROLÜ
        if (!urunler.isEmpty() && rafManager.getToplamKapasite() == 0) {
            System.out.println("HATA! ÜRÜN EKLENEMİYOR\nDEPODA YER YOK");
            return;
        }
        //ÜRÜN BİLGİLERİ GİRİŞİ
        System.out.println("Eklemek istediğiniz ürünün bilgilerini giriniz");
        System.out.print("Ürünün Adı: ");
        String ad = scanner.nextLine();

        int[] hak = {5}; // hatalı giriş hakkı

        System.out.print("Seri numarası: ");
        String seriNo = scanner.nextLine();

        while (seriNoVarMi(seriNo)) {
            hak[0]--;
            if (hakTukendiMi(hak))
                return;

            System.out.println("Bir ürünün seri numarası başka bir ürüne verilemez");
            System.out.print("Seri numarası: ");
            seriNo = scanner.nextLine();
        }

        miktarBelirlemeBasi:
        while (true) {
            hak[0] = 5; // Hakları sıfırla

            // MİKTAR GİRİŞİ
            System.out.print("Eklenecek miktar: ");
            Integer miktar = sayiIstegi("Eklenecek miktar: ", hak);
            if (miktar == null)
                return;

            while (miktar <= 0) {
                hak[0]--;
                if (hakTukendiMi(hak))
                    return;

                System.out.println("Lütfen pozitif bir sayı giriniz!");
                System.out.print("Eklenecek miktar: ");
                miktar = sayiIstegi("Eklenecek miktar: ", hak);
                if (miktar == null)
                    return;
            }

            // Depo genel kapasite kontrolü
            if (miktar > rafManager.getToplamKapasite()) {
                System.out.println("Depoda eklemek istediğiniz miktar kadar yer bulunmamaktadır.");
                System.out.println("Depodaki boş miktar: " + rafManager.getToplamKapasite());
                System.out.println("-Ana menüye yönlendiriliyorsunuz-");
                return;
            }

            hak[0] = 5; // Hakları sıfırla

            //RAF SEÇİMİ
            int rafSayisi = rafManager.getRafSayisi();
            System.out.print("Ürünün ekleneceği rafın kodunu giriniz (1-" + rafSayisi + "): ");

            Integer rafGiris = sayiIstegi("Raf kodu: ", hak);
            if (rafGiris == null)
                return;
            int secilenRaf = rafGiris - 1;

            while (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                System.out.println("Geçerli bir raf kodu giriniz (1-" + rafSayisi + ")");
                System.out.print("Raf kodu: ");
                rafGiris = sayiIstegi("Raf kodu: ", hak);
                if (rafGiris == null)
                    return;
                secilenRaf = rafGiris - 1;

                if (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                    hak[0]--;
                    if (hakTukendiMi(hak))
                        return;
                }
            }

            int kalanKapasite = rafManager.getRafKapasitesi(secilenRaf);

            //KAPASİTE YETERSİZLİĞİ VE DAĞITIM MANTIĞI
            if (miktar > kalanKapasite) {

                // Eğer toplam depo kapasitesi kurtarıyorsa sor
                if (rafManager.getToplamKapasite() >= miktar) {
                    System.out.println("\nUYARI: SEÇİLEN RAFTA YETERLİ YER YOK");
                    System.out.println("Eklemek istediğiniz miktar: " + miktar + ", Bu raftaki boş yer: " + kalanKapasite);
                    System.out.println("Ancak depodaki diğer raflarda yeterli toplam alan mevcut.");

                    System.out.println("\nGüncel Raf Durumları");
                    for (int i = 0; i < rafSayisi; i++) {
                        System.out.println((i + 1) + ".Rafın Boş Kapasitesi: " + rafManager.getRafKapasitesi(i));
                    }

                    System.out.print("\nÜrünü parça parça farklı raflara dağıtarak eklemek ister misiniz? (Evet/Hayır): ");
                    String cevap = scanner.nextLine();

                    if (cevap.equalsIgnoreCase("Hayır")) {
                        System.out.println("İşlem iptal edildi");
                        System.out.println("-Ana menüye yönlendiriliyorsunuz");
                        return;
                    } else if (cevap.equalsIgnoreCase("Evet")) {
                        //PARÇALI EKLEME MODU
                        int[] dagitimHakki = {8}; // Özel hak sayısı
                        int kalanDagitilacak = miktar;

                        List<Integer> gerceklesenRaflar = new ArrayList<>();

                        System.out.println("\nParçalı Kayıt Modu Başlatıldı");

                        while (kalanDagitilacak > 0) {
                            System.out.println("\nKalan dağıtılacak miktar: " + kalanDagitilacak);
                            System.out.print("Hangi rafa ekleme yapmak istersiniz? (1-" + rafSayisi + "): ");

                            Integer parcaRafGiris = sayiIstegi("Raf kodu: ", dagitimHakki);
                            if (parcaRafGiris == null)
                                return;
                            int parcaRafIndex = parcaRafGiris - 1;

                            if (parcaRafIndex < 0 || parcaRafIndex >= rafSayisi) {
                                System.out.println("Geçersiz raf kodu!");
                                dagitimHakki[0]--;
                                if (hakTukendiMi(dagitimHakki))
                                    return;
                                continue;
                            }

                            System.out.print((parcaRafIndex + 1) + ". Rafa kaç adet ürün ekleyeceksiniz: ");
                            Integer parcaMiktar = sayiIstegi("Miktar: ", dagitimHakki);
                            if (parcaMiktar == null)
                                return;

                            // Kontroller
                            if (parcaMiktar <= 0) {
                                System.out.println("Miktar 0'dan büyük olmalı.");
                                continue;
                            }
                            if (parcaMiktar > kalanDagitilacak) {
                                System.out.println("Kalan miktardan (" + kalanDagitilacak + ") fazla ekleyemezsiniz.");
                                continue;
                            }
                            if (parcaMiktar > rafManager.getRafKapasitesi(parcaRafIndex)) {
                                System.out.println("Bu rafta o kadar yer yok! (Boş: " + rafManager.getRafKapasitesi(parcaRafIndex) + ")");
                                continue;
                            }

                            // Rafı güncelle
                            rafManager.kapasiteGuncelle(parcaRafIndex, -parcaMiktar);
                            kalanDagitilacak -= parcaMiktar;

                            // Başarılı işlem yapılan rafı listemize alıyoruz
                            if (!gerceklesenRaflar.contains(parcaRafIndex)) {
                                gerceklesenRaflar.add(parcaRafIndex);
                            }

                            System.out.println(parcaMiktar + " adet ürün +"+(parcaRafIndex + 1)+".Rafa ayrıldı.");
                        }

                        // İlk raf olarak listedeki son rafı baz alıyoruz
                        int anaRaf = gerceklesenRaflar.get(gerceklesenRaflar.size() - 1);
                        Urun yeniUrun = new Urun(ad, seriNo, miktar, anaRaf);

                        // Diğer tüm rafları da ürüne ekliyoruz
                        for(int r : gerceklesenRaflar) {
                            yeniUrun.rafEkle(r);
                        }

                        urunler.add(yeniUrun);
                        DosyaIslemleri.kaydet(urunler, DOSYA);

                        System.out.println("\nYENİ ÜRÜN KAYDI PARÇALI OLARAK TAMAMLANDI");
                        System.out.println("Bilgiler-> Ürün adı:" + ad + " ,Seri numarası:" + seriNo + " ,Toplam Miktar:" + miktar);
                        System.out.println("Ürünün bulunduğu Raflar:" + yeniUrun.getRafKodlariString());
                        return; // Çıkış
                    } else {
                        System.out.println("Geçersiz cevap! Miktar ekranına dönülüyor\n");
                        continue miktarBelirlemeBasi;
                    }
                }

                // Eğer toplam kapasite yetmiyor veya kullanıcı manuel devam etmek isterse
                while (miktar > kalanKapasite) {
                    System.out.println("Bu rafın kapasitesi yetersiz. Lütfen diğer raflara ürün eklemeyi deneyiniz");
                    System.out.print("Ürünün ekleneceği rafın kodunu giriniz (1-" + rafSayisi + "): ");

                    rafGiris = sayiIstegi("Raf kodu: ", hak);
                    if (rafGiris == null)
                        return;
                    secilenRaf = rafGiris - 1;

                    while (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                        System.out.print("Geçerli bir raf kodu giriniz: ");
                        rafGiris = sayiIstegi("Raf kodu: ", hak);
                        if (rafGiris == null)
                            return;
                        secilenRaf = rafGiris - 1;

                        if (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                            hak[0]--;
                            if (hakTukendiMi(hak))
                                return;
                        }
                    }
                    kalanKapasite = rafManager.getRafKapasitesi(secilenRaf);

                    if (miktar > kalanKapasite) {
                        hak[0]--;
                        if (hakTukendiMi(hak))
                            return;
                    }
                }
            }

            Urun yeniUrun = new Urun(ad, seriNo, miktar, secilenRaf);
            urunler.add(yeniUrun);
            rafManager.kapasiteGuncelle(secilenRaf, -miktar);
            DosyaIslemleri.kaydet(urunler, DOSYA);

            System.out.println("Ürün eklendi");
            System.out.println("Bilgiler-> Ürün adı:" + ad + " ,Seri numarası:" + seriNo + " ,Miktar:" + miktar + " ,Ürünün bulunduğu raflar:" + yeniUrun.getRafKodlariString());
            return; // İşlem bitti
        }
    }

    public void urunEkleme() { // Stok artırma

        // GENEL KAPASİTE KONTROLÜ
        if (rafManager.getToplamKapasite() == 0) {
            System.out.println("HATA! DEPODA YER YOK");
            return;
        }
        int[] hak = {5}; // Genel işlemler için hak

        // ÜRÜN BULMA İŞLEMİ
        System.out.print("Eklemek istediğiniz ürünün seri numarasını giriniz: ");
        String arananSeriNo = scanner.nextLine();
        Urun bulunanUrun = urunBul(arananSeriNo);

        while (bulunanUrun == null) {
            hak[0]--;
            if (hakTukendiMi(hak))
                return;

            System.out.println("Girdiğiniz seri numarası herhangi bir ürüne ait değildir. Lütfen tekrar deneyin");
            System.out.print("Eklemek istediğiniz ürünün seri numarasını giriniz: ");
            arananSeriNo = scanner.nextLine();
            bulunanUrun = urunBul(arananSeriNo);
        }

        //Kullanıcı 'Hayır' derse miktar belirleme adımına buradan döneceğiz
        miktarBelirlemeBasi:
        while (true) {
            hak[0] = 5; // Hakları sıfırla

            //MİKTAR BELİRLEME
            System.out.print("Eklemek istediğiniz miktar: ");
            Integer eklenecekMiktar = sayiIstegi("Eklenecek miktar: ", hak);
            if (eklenecekMiktar == null)
                return;

            while (eklenecekMiktar < 0) {
                hak[0]--;
                if (hakTukendiMi(hak))
                    return;

                System.out.println("Eklenecek miktar negatif olamaz. Lütfen geçerli bir sayı giriniz");
                System.out.print("Eklenecek miktar: ");
                eklenecekMiktar = sayiIstegi("Eklenecek miktar: ", hak);
                if (eklenecekMiktar == null)
                    return;
            }
            // Deponun toplam kapasitesi yetiyor mu
            if (eklenecekMiktar > rafManager.getToplamKapasite()) {
                System.out.println("Depoda eklemek istediğiniz miktar kadar yer bulunmamaktadır.");
                System.out.println("Depodaki boş miktar: " + rafManager.getToplamKapasite());
                System.out.println("-Ana menüye yönlendiriliyorsunuz-");
                return;
            }

            hak[0] = 5; // Hakları sıfırla

            // RAF SEÇİMİ
            int rafSayisi = rafManager.getRafSayisi();
            System.out.print("Eklenecek ürünün raf kodunu giriniz (1-" + rafSayisi + "): ");

            Integer rafGiris = sayiIstegi("Raf kodu: ", hak);
            if (rafGiris == null)
                return;
            int secilenRaf = rafGiris - 1;

            // Rafın geçerli olup olmadığını kontrol et
            while (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                System.out.println("Seçtiğiniz raf kodu depoda bulunmamaktadır. Lütfen geçerli bir raf seçiniz");
                System.out.print("Eklenecek ürünün raf kodunu giriniz (1-" + rafSayisi + "): ");

                rafGiris = sayiIstegi("Raf kodu: ", hak);
                if (rafGiris == null)
                    return;
                secilenRaf = rafGiris - 1;
            }

            int rafKalan = rafManager.getRafKapasitesi(secilenRaf);

            // KAPASİTE VE DAĞITIM KONTROLÜ
            // Seçilen rafta yer yoksa ama depoda toplam yer varsa
            if (eklenecekMiktar > rafKalan) {

                if (rafManager.getToplamKapasite() >= eklenecekMiktar) {
                    System.out.println("\nUYARI: SEÇİLEN RAFTA YETERLİ YER YOK");
                    System.out.println("Eklemek istediğiniz miktar: " + eklenecekMiktar + ", Bu raftaki boş yer: " + rafKalan);
                    System.out.println("Ancak depodaki diğer raflarda yeterli toplam alan mevcut");

                    System.out.println("\nGüncel Raf Durumları:");
                    for (int i = 0; i < rafSayisi; i++) {
                        System.out.println((i + 1) + ".rafın boş Kapasitesi: " + rafManager.getRafKapasitesi(i));
                    }

                    System.out.print("\nÜrünü parça parça farklı raflara dağıtarak eklemek ister misiniz? (Evet/Hayır): ");
                    String cevap = scanner.nextLine();

                    if (cevap.equalsIgnoreCase("Hayır")) {
                        System.out.println("İşlem iptal edildi.");
                        System.out.println("-Ana menüye yönlendiriliyorsunuz-");
                        return;
                    } else if (cevap.equalsIgnoreCase("Evet")) {
                        // PARÇA PARÇA EKLEME MODU
                        int[] dagitimHakki = {8};
                        int kalanDagitilacak = eklenecekMiktar;

                        System.out.println("\nParçalı Ekleme Modu Başlatıldı");
                        System.out.println("Toplam dağıtılacak miktar: " + kalanDagitilacak);

                        while (kalanDagitilacak > 0) {
                            System.out.println("\nKalan dağıtılacak miktar: " + kalanDagitilacak);
                            System.out.print("Hangi rafa ekleme yapmak istersiniz? (1-" + rafSayisi + "): ");

                            Integer parcaRafGiris = sayiIstegi("Raf kodu: ", dagitimHakki);
                            if (parcaRafGiris == null)
                                return; // Hak biterse çık
                            int parcaRafIndex = parcaRafGiris - 1;

                            // Raf index kontrolü
                            if (parcaRafIndex < 0 || parcaRafIndex >= rafSayisi) {
                                System.out.println("Geçersiz raf kodu!");
                                dagitimHakki[0]--;
                                if (hakTukendiMi(dagitimHakki))
                                    return;
                                continue;
                            }

                            System.out.print((parcaRafIndex + 1) + ". Rafa kaç adet ekleyeceksiniz: ");
                            Integer parcaMiktar = sayiIstegi("Miktar: ", dagitimHakki);
                            if (parcaMiktar == null)
                                return;

                            // Miktar kontrolleri
                            if (parcaMiktar <= 0) {
                                System.out.println("Miktar 0'dan büyük olmalı.");
                                continue;
                            }
                            if (parcaMiktar > kalanDagitilacak) {
                                System.out.println("Kalan miktardan (" + kalanDagitilacak + ") fazla ekleyemezsiniz.");
                                continue;
                            }
                            if (parcaMiktar > rafManager.getRafKapasitesi(parcaRafIndex)) {
                                System.out.println("Bu rafta o kadar yer yok! (Boş: " + rafManager.getRafKapasitesi(parcaRafIndex) + ")");
                                continue;
                            }

                            // RAF VE ÜRÜN GÜNCELLEME
                            rafManager.kapasiteGuncelle(parcaRafIndex, -parcaMiktar);

                            // Ürün miktarını artır
                            bulunanUrun.setMiktar(bulunanUrun.getMiktar() + parcaMiktar);
                            // Ürün son olarak hangi rafa konduysa o rafın kodu set ediliyor
                            // AYNI ZAMANDA LİSTEYE EKLİYOR
                            bulunanUrun.setRafIndex(parcaRafIndex);

                            kalanDagitilacak -= parcaMiktar;
                            System.out.println(parcaMiktar + " adet ürün"+ (parcaRafIndex + 1)+".Rafa Eklendi.");
                        }

                        // İşlem Bitti, Kaydet ve Çık
                        DosyaIslemleri.kaydet(urunler, DOSYA);
                        System.out.println("\nÜRÜN BAŞARIYLA DAĞITILDI VE EKLENDİ.");
                        System.out.println("Ürün adı:" + bulunanUrun.getAd() + " ,Seri numarası:"+bulunanUrun.getSeriNo()+"Güncel Toplam Stok:" + bulunanUrun.getMiktar() + " ,Ürünün bulunduğu Raflar:" + bulunanUrun.getRafKodlariString());
                        return;

                    } else {
                        // Evet veya Hayır dışında bir şey yazılırsa
                        System.out.println("Geçersiz cevap! Miktar belirleme ekranına dönülüyor\n");
                        continue miktarBelirlemeBasi;
                    }
                }

                // Eğer toplam kapasite de yetmiyorsa veya kullanıcı seçtiği rafta ısrar edecekse
                while (eklenecekMiktar > rafKalan) {
                    System.out.println("Rafın kapasitesi yetersiz. Lütfen başka raflara ürün eklemeyi deneyiniz");
                    System.out.print("Eklenecek ürünün raf kodunu giriniz (1-" + rafSayisi + "): ");

                    rafGiris = sayiIstegi("Raf kodu: ", hak);
                    if (rafGiris == null)
                        return;
                    secilenRaf = rafGiris - 1;

                    while (secilenRaf < 0 || secilenRaf >= rafSayisi) {
                        System.out.println("Seçtiğiniz raf kodu depoda bulunmamaktadır. Lütfen geçerli bir raf seçiniz");
                        System.out.print("Raf kodu: ");
                        rafGiris = sayiIstegi("Raf kodu: ", hak);
                        if (rafGiris == null)
                            return;
                        secilenRaf = rafGiris - 1;
                    }
                    rafKalan = rafManager.getRafKapasitesi(secilenRaf);

                    if (eklenecekMiktar > rafKalan) {
                        hak[0]--;
                        if (hakTukendiMi(hak))
                            return;
                    }
                }
            }

            //STANDART TEK SEFERDE EKLEME
            rafManager.kapasiteGuncelle(secilenRaf, -eklenecekMiktar);
            bulunanUrun.setMiktar(bulunanUrun.getMiktar() + eklenecekMiktar);
            bulunanUrun.setRafIndex(secilenRaf);
            DosyaIslemleri.kaydet(urunler, DOSYA);

            System.out.println("Ürün başarıyla eklendi");
            System.out.println("Bilgiler-> Ürün adı:" + bulunanUrun.getAd() + " ,Seri numarası:" + bulunanUrun.getSeriNo() + " ,Güncel miktar:" + bulunanUrun.getMiktar() + " ,Ürünün bulunduğu raflar:" + bulunanUrun.getRafKodlariString());
            return; // İşlem bitti, metoddan çık
        }
    }

    public void urunCikarma() {
        if (urunler.isEmpty()) { // Depoda urun yok ise hata mesajı verir
            System.out.println("HATA! Depoda herhangi bir ürün bulunmamaktadır");
            return;
        }

        int[] hak = {5};

        System.out.print("Çıkartmak istediğiniz ürünün seri numarasını giriniz: ");
        String seriNo = scanner.nextLine();
        Urun urun = urunBul(seriNo);

        while (urun == null) {
            hak[0]--;
            if (hakTukendiMi(hak))
                return;

            System.out.println("Girdiğiniz seri numarası herhangi bir ürünle eşleşmiyor. Tekrar deneyiniz");
            System.out.print("Seri numarası: ");
            seriNo = scanner.nextLine();
            urun = urunBul(seriNo);
        }

        hak[0] = 5; // RESET

        System.out.print("Çıkarmak istediğiniz miktarı giriniz: ");
        Integer cikarilacak = sayiIstegi("Miktar: ", hak);
        if (cikarilacak == null)
            return;

        while (cikarilacak < 0) {
            hak[0]--;
            if (hakTukendiMi(hak))
                return;

            System.out.println("Çıkarılacak miktar negatif olamaz. Lütfen geçerli bir sayı giriniz");
            System.out.print("Çıkarılacak miktar: ");
            cikarilacak = sayiIstegi("Çıkarılacak miktar: ", hak);
            if (cikarilacak == null)
                return;
        }

        if (urun.getMiktar() < cikarilacak) {
            System.out.println("Depoda bu ürünün çıkartmak istediğiniz kadar stok durumu bulunmamaktadır");
            System.out.println("Mevcut stok: " + urun.getMiktar());
            System.out.println("-Ana menüye yönlendiriliyorsunuz-");
            return;
        }

        // Stok ve raf güncellemesi
        urun.setMiktar(urun.getMiktar() - cikarilacak);
        rafManager.kapasiteGuncelle(urun.getRafIndex(), +cikarilacak);

        //Stok Sıfır Kontrolü ve Silme İşlemi
        if (urun.getMiktar() == 0) {
            urunler.remove(urun); // Listeden tamamen siler
            System.out.println("Bilgi: Ürün stoğu tükendiği için sistemden kaydı tamamen silinmiştir.");
        } else {
            if (urun.getMiktar() <= 10) {// Eğer ürün stok durumu 10 veya 10 dan daha azaldıysa bilgilendirme mesajı verir
                System.out.println("UYARI: BU ÜRÜNDEN DEPODA 10 ADETTEN AZ KALMIŞTIR!");
            }
            // DEĞİŞİKLİK: Çoklu raf kodları yazdırılıyor
            System.out.println("Güncel Durum -> Ürün: " + urun.getAd() + " ,Seri No: " + urun.getSeriNo() + " ,Kalan Miktar: " + urun.getMiktar() + " ,Ürünün bulunduğu raf: " + urun.getRafKodlariString());
        }

        // Değişiklikleri dosyaya kaydet
        DosyaIslemleri.kaydet(urunler, DOSYA);
        System.out.println("İşlem başarıyla tamamlandı.");
    }

    public void urunTasima() {
        if (urunler.isEmpty()) {
            System.out.println("HATA! Depoda herhangi bir ürün bulunmamaktadır");
            return;
        }

        int[] hak = {5};

        System.out.print("Taşımak istediğiniz ürünün seri numarasını giriniz: ");
        String seriNo = scanner.nextLine();
        Urun u = urunBul(seriNo);

        while (u == null) {
            hak[0]--;
            if (hakTukendiMi(hak)) return;

            System.out.print("Bu seri numarası herhangi bir ürünle eşleşmemektedir. Lütfen tekrar deneyiniz: ");
            seriNo = scanner.nextLine();
            u = urunBul(seriNo);
        }

        int eskiRaf = u.getRafIndex();
        int miktar = u.getMiktar();
        int rafSayisi = rafManager.getRafSayisi();

        hak[0] = 5;

        System.out.print("Ürünü taşımak istediğiniz rafın kodunu giriniz (1-" + rafSayisi + "): ");

        Integer rafGiris = sayiIstegi("Raf kodu: ", hak);
        if (rafGiris == null) return;
        int yeniRaf = rafGiris - 1;

        while (yeniRaf < 0 || yeniRaf >= rafSayisi) {
            System.out.println("Seçtiğiniz raf kodu depoda bulunmamaktadır. Lütfen geçerli bir raf seçiniz");
            System.out.print("Ürünü taşımak istediğiniz rafın kodunu giriniz (1-" + rafSayisi + "): ");

            rafGiris = sayiIstegi("Raf kodu: ", hak);
            if (rafGiris == null) return;
            yeniRaf = rafGiris - 1;

            if (yeniRaf < 0 || yeniRaf >= rafSayisi) {
                hak[0]--;
                if (hakTukendiMi(hak)) return;
            }
        }

        hak[0] = 5;

        int yeniRafKapasite = rafManager.getRafKapasitesi(yeniRaf);
        while (yeniRafKapasite < miktar) {
            System.out.println("Seçilen rafın kapasitesi yetersiz. Lütfen başka bir raf seçin");
            System.out.print("Ürünü taşımak istediğiniz rafın kodunu giriniz (1-" + rafSayisi + "): ");

            rafGiris = sayiIstegi("Raf kodu: ", hak);
            if (rafGiris == null) return;
            yeniRaf = rafGiris - 1;


            while (yeniRaf < 0 || yeniRaf >= rafSayisi) {
                System.out.print("Geçerli raf kodu giriniz: ");
                rafGiris = sayiIstegi("Raf kodu: ", hak);
                if (rafGiris == null) return;
                yeniRaf = rafGiris - 1;

                if (yeniRaf < 0 || yeniRaf >= rafSayisi) {
                    hak[0]--;
                    if (hakTukendiMi(hak)) return;
                }
            }
            yeniRafKapasite = rafManager.getRafKapasitesi(yeniRaf);

            if (yeniRafKapasite < miktar) {
                hak[0]--;
                if (hakTukendiMi(hak)) return;
            }
        }

        rafManager.kapasiteGuncelle(eskiRaf, +miktar);
        rafManager.kapasiteGuncelle(yeniRaf, -miktar);

        // setRafIndex yerine bunu kullanıyoruz ki eskisini silsin
        u.rafTasimaGuncellemesi(eskiRaf, yeniRaf);

        DosyaIslemleri.kaydet(urunler, DOSYA);

        System.out.println("Ürün başarıyla taşındı!");
        System.out.println("Bilgiler -> Ürün adı:" + u.getAd() + ", Seri No:" + u.getSeriNo() + " Ürün miktarı:" + u.getMiktar() + ", Yeni Konumlar:" + u.getRafKodlariString());
    }

    // metodlar

    //seri numarasının olup olmadığını kontrol eder
    private boolean seriNoVarMi(String seriNo) {
        for (Urun u : urunler)
            if (u.getSeriNo().equals(seriNo))
                return true;
        return false;
    }

    //seri numarası ile ürün arama işlemini yapar
    private Urun urunBul(String seriNo) {
        for (Urun u : urunler)
            if (u.getSeriNo().equals(seriNo))
                return u;
        return null;
    }

    //seçilen rafın dolu olup olmadığını kontrol eder
    public boolean rafDoluMu(int rafIndex) {
        for (Urun u : urunler)
            if (u.getRafIndex() == rafIndex)
                return true;
        return false;
    }

    //Raf silme işleminden sonra raf sayısını günceller
    public void rafSilindiktenSonraGuncelle(int silinenRafIndex) {
        for (Urun u : urunler) {
            if (u.getRafIndex() > silinenRafIndex) {
                u.setRafIndex(u.getRafIndex() - 1);
            }
        }
        DosyaIslemleri.kaydet(urunler, DOSYA);
    }

    public List<Urun> getUrunler() {
        return urunler;
    }
}