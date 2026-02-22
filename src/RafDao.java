import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RafDao {
    private DBHelper dbHelper = new DBHelper();

    public List<Raf> tumRaflariGetir() {
        List<Raf> raflar = new ArrayList<>();
        String sql = "SELECT * FROM raflar";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Veritabanından id ve kapasiteyi alıp listeye ekliyor
                Raf raf = new Raf(rs.getInt("id"), rs.getInt("kapasite"));
                raflar.add(raf);
            }
        } catch (SQLException e) {
            System.out.println("Raf Listeleme Hatası: " + e.getMessage());
        }
        return raflar;
    }

    public void rafEkle(Raf raf) {
        String sql = "INSERT INTO raflar (kapasite) VALUES (?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, raf.getKapasite());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    raf.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Raf Ekleme Hatası: " + e.getMessage());
        }
    }

    public void rafGuncelle(Raf raf) {
        String sql = "UPDATE raflar SET kapasite = ? WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, raf.getKapasite());
            pstmt.setInt(2, raf.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Raf Güncelleme Hatası: " + e.getMessage());
        }
    }

    public void rafSil(int id) {
        String sql = "DELETE FROM raflar WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Raf Silme Hatası: " + e.getMessage());
        }
    }


}