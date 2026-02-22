import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MudurDao {
    private DBHelper dbHelper = new DBHelper();

    // Sistemde kayıtlı hiç müdür var mı? (İlk kurulum/Yönlendirme kontrolü için)
    public boolean mudurVarMi() {
        String sql = "SELECT COUNT(*) FROM kullanicilar";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası (mudurVarMi): " + e.getMessage());
        }
        return false;
    }

    // Aynı kullanıcı adıyla başka biri kayıtlı mı? (Kritik Güvenlik)
    public boolean kullaniciAdiDahaOnceAlinmisMi(String kullaniciAdi) {
        String sql = "SELECT COUNT(*) FROM kullanicilar WHERE kullanici_adi = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kullaniciAdi);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // 0'dan büyükse bu isim alınmış demektir!
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası (kullaniciAdiDahaOnceAlinmisMi): " + e.getMessage());
        }
        return false;
    }

    // Yeni müdürü veritabanına kaydetme
    public boolean mudurKaydet(Mudur mudur) {
        String sql = "INSERT INTO kullanicilar (kullanici_adi, sifre) VALUES (?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mudur.getKullaniciAdi());
            pstmt.setString(2, mudur.getSifre());

            int etkilenenSatir = pstmt.executeUpdate();
            return etkilenenSatir > 0;

        } catch (SQLException e) {
            System.out.println("SQL Hatası (mudurKaydet): " + e.getMessage());
            return false;
        }
    }

    // Kullanıcı adı ve şifre ile giriş kontrolü
    public boolean girisYap(String kullaniciAdi, String sifre) {
        String sql = "SELECT * FROM kullanicilar WHERE kullanici_adi = ? AND sifre = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kullaniciAdi);
            pstmt.setString(2, sifre);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Eğer eşleşen kayıt bulursa giriş başarılıdır
            }

        } catch (SQLException e) {
            System.out.println("SQL Hatası (girisYap): " + e.getMessage());
            return false;
        }
    }
}