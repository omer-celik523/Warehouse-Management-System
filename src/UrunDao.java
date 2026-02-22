import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UrunDao {
    private DBHelper dbHelper = new DBHelper();
    private RafDao rafDao = new RafDao();

    public List<Urun> tumUrunleriGetir() {
        List<Urun> urunler = new ArrayList<>();
        List<Raf> sistemdekiRaflar = rafDao.tumRaflariGetir();

        String sqlUrun = "SELECT * FROM urunler";
        String sqlKonum = "SELECT * FROM urun_konumlari WHERE urun_id = ?";

        try (Connection conn = dbHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsUrun = stmt.executeQuery(sqlUrun)) {

            while (rsUrun.next()) {
                int urunId = rsUrun.getInt("id");
                Urun u = new Urun(urunId, rsUrun.getString("urun_adi"), rsUrun.getString("seri_no"), rsUrun.getInt("toplam_miktar"), 0);
                String dbTarih = rsUrun.getString("eklenme_tarihi");
                u.setEklenmeTarihi(dbTarih != null ? dbTarih : "Bilinmiyor");

                u.getRafDagilimi().clear();

                try (PreparedStatement pstmtKonum = conn.prepareStatement(sqlKonum)) {
                    pstmtKonum.setInt(1, urunId);
                    ResultSet rsKonum = pstmtKonum.executeQuery();
                    while (rsKonum.next()) {
                        int dbRafId = rsKonum.getInt("raf_id");
                        int miktar = rsKonum.getInt("miktar");

                        int gercekIndex = -1;
                        for (int i = 0; i < sistemdekiRaflar.size(); i++) {
                            if (sistemdekiRaflar.get(i).getId() == dbRafId) {
                                gercekIndex = i;
                                break;
                            }
                        }

                        if (gercekIndex != -1) {
                            u.rafVeMiktarEkle(gercekIndex, miktar);
                        }
                    }
                }

                if(!u.getRafDagilimi().isEmpty()) {
                    u.setRafIndex(u.getRafDagilimi().keySet().iterator().next());
                }

                urunler.add(u);
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası (tumUrunleriGetir): " + e.getMessage());
        }
        return urunler;
    }

    public void veritabaniniGuncelle(List<Urun> urunler) {
        List<Raf> sistemdekiRaflar = rafDao.tumRaflariGetir();

        String sqlDeleteKonum = "DELETE FROM urun_konumlari WHERE urun_id = ?";
        String sqlInsertUrun = "INSERT INTO urunler (urun_adi, seri_no, toplam_miktar, eklenme_tarihi) VALUES (?, ?, ?, ?)";
        String sqlUpdateUrun = "UPDATE urunler SET urun_adi = ?, seri_no = ?, toplam_miktar = ?, eklenme_tarihi = ? WHERE id = ?";
        String sqlInsertKonum = "INSERT INTO urun_konumlari (urun_id, raf_id, miktar) VALUES (?, ?, ?)";

        try (Connection conn = dbHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psInsertUrun = conn.prepareStatement(sqlInsertUrun, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdateUrun = conn.prepareStatement(sqlUpdateUrun);
                 PreparedStatement psDeleteKonum = conn.prepareStatement(sqlDeleteKonum);
                 PreparedStatement psInsertKonum = conn.prepareStatement(sqlInsertKonum)) {

                List<Integer> islemGorenIdler = new ArrayList<>();

                for (Urun u : urunler) {
                    if (u.getId() > 0) {
                        psUpdateUrun.setString(1, u.getAd());
                        psUpdateUrun.setString(2, u.getSeriNo());
                        psUpdateUrun.setInt(3, u.getMiktar());
                        psUpdateUrun.setString(4, u.getEklenmeTarihi()); // YENİ EKLENDİ
                        psUpdateUrun.setInt(5, u.getId());
                        psUpdateUrun.executeUpdate();
                        islemGorenIdler.add(u.getId());

                        psDeleteKonum.setInt(1, u.getId());
                        psDeleteKonum.executeUpdate();
                    } else {
                        psInsertUrun.setString(1, u.getAd());
                        psInsertUrun.setString(2, u.getSeriNo());
                        psInsertUrun.setInt(3, u.getMiktar());
                        psInsertUrun.setString(4, u.getEklenmeTarihi()); // YENİ EKLENDİ
                        psInsertUrun.executeUpdate();

                        try (ResultSet rs = psInsertUrun.getGeneratedKeys()) {
                            if (rs.next()) {
                                u.setId(rs.getInt(1));
                                islemGorenIdler.add(u.getId());
                            }
                        }
                    }

                    for (Map.Entry<Integer, Integer> entry : u.getRafDagilimi().entrySet()) {
                        int uiIndex = entry.getKey();

                        if (uiIndex >= 0 && uiIndex < sistemdekiRaflar.size()) {
                            int gercekDbRafId = sistemdekiRaflar.get(uiIndex).getId();

                            psInsertKonum.setInt(1, u.getId());
                            psInsertKonum.setInt(2, gercekDbRafId);
                            psInsertKonum.setInt(3, entry.getValue());
                            psInsertKonum.executeUpdate();
                        }
                    }
                }

                kalintilariTemizle(conn, islemGorenIdler);
                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Veritabanı Güncelleme Hatası:\n" + ex.getMessage(), "Kritik SQL Hatası", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Veritabanına bağlanılamadı:\n" + e.getMessage(), "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kalintilariTemizle(Connection conn, List<Integer> islemGorenIdler) throws SQLException {
        String sqlGetDbIds = "SELECT id FROM urunler";
        String sqlDeleteUrunKonum = "DELETE FROM urun_konumlari WHERE urun_id = ?";
        String sqlDeleteUrun = "DELETE FROM urunler WHERE id = ?";

        List<Integer> dbUrunIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlGetDbIds)) {
            while (rs.next()) {
                dbUrunIds.add(rs.getInt("id"));
            }
        }

        try (PreparedStatement psDelKonum = conn.prepareStatement(sqlDeleteUrunKonum);
             PreparedStatement psDelUrun = conn.prepareStatement(sqlDeleteUrun)) {

            for (int dbId : dbUrunIds) {
                if (!islemGorenIdler.contains(dbId)) {
                    psDelKonum.setInt(1, dbId);
                    psDelKonum.executeUpdate();
                    psDelUrun.setInt(1, dbId);
                    psDelUrun.executeUpdate();
                }
            }
        }
    }
}