import java.io.*;

public class DosyaIslemleri {
    public static void kaydet(Object veri, String dosyaAdi) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dosyaAdi))) {
            oos.writeObject(veri);
        } catch (IOException e) {
            // İlk kullanımda dosya yoksa hata vermesin
        }
    }

    public static <T> T yukle(String dosyaAdi) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dosyaAdi))) {
            return (T) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
