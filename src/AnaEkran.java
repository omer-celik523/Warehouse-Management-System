import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnaEkran extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContent;
    private List<JButton> menuButtons = new ArrayList<>();

    private RafManager rafManager;
    private UrunManager urunManager;
    private UrunDao urunDao;

    private DefaultTableModel tabloModeli;
    private JLabel lblToplamUrun, lblToplamRaf, lblBosKapasite;

    private final Color MAIN_BG = new Color(245, 248, 250);
    private final Color SIDEBAR_BG = new Color(255, 255, 255);
    private final Color ACTIVE_BTN_BG = new Color(52, 152, 219);
    private final Color INACTIVE_BTN_BG = new Color(255, 255, 255);
    private final Color TEXT_COLOR = new Color(80, 90, 100);
    private final Color HEADER_TEXT_COLOR = new Color(44, 62, 80);

    private final String INPUT_STYLE = "arc: 10; padding: 5,10,5,10; background: #FFFFFF; foreground: #000000; borderColor: #BDC3C7; focusedBorderColor: #3498DB;";
    private final String COMBO_STYLE = INPUT_STYLE + " buttonBackground: #FFFFFF; buttonArrowColor: #2C3E50; buttonHoverArrowColor: #3498DB;";

    public AnaEkran() {
        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);
        this.urunDao = new UrunDao();

        setTitle("WMS Pro - Tam Kapsamlı Depo Yönetimi");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(330, 0));
        sidebar.setBorder(new EmptyBorder(40, 20, 30, 20));

        JLabel logo = new JLabel("📦 WMS Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(new Color(41, 128, 185));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton btnDashboard = createMenuButton("📊 Dashboard", "DASHBOARD_SAYFASI");
        JButton btnUrunler = createMenuButton("🛒 Ürün İşlemleri", "URUNLER_SAYFASI");
        JButton btnListeleme = createMenuButton("📋 Envanter Listesi", "LISTELEME_SAYFASI");
        JButton btnRaflar = createMenuButton("🗄️ Raf İşlemleri", "RAFLAR_SAYFASI");
        JButton btnCikis = createMenuButton("🚪 Çıkış Yap", null);

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnUrunler);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnListeleme);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnRaflar);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnCikis);

        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)),
                new EmptyBorder(40, 25, 30, 25)));

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(MAIN_BG);

        mainContent.add(createDashboardPanel(), "DASHBOARD_SAYFASI");
        mainContent.add(createUrunlerPanel(), "URUNLER_SAYFASI");
        mainContent.add(createListelemePanel(), "LISTELEME_SAYFASI");
        mainContent.add(createRaflarPanel(), "RAFLAR_SAYFASI");

        add(mainContent, BorderLayout.CENTER);
        setActiveButton(btnDashboard);

        btnCikis.addActionListener(e -> {
            int cevap = JOptionPane.showConfirmDialog(this, "Çıkmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (cevap == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginEkran().setVisible(true);
            }
        });

        dashboardGuncelle();
        tabloyuGuncelle();
    }

    // DİNAMİK BAŞLIKLI MODERN INPUT METOTLARI

    private String modernInputAl(String baslik, String mesaj) {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("<html><body style='width: 400px; color: #2C3E50; font-family: sans-serif; font-size: 14px; font-weight: bold;'>" + mesaj.replace("\n", "<br>") + "</body></html>");

        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txt.setPreferredSize(new Dimension(400, 45));
        txt.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txt.setBackground(Color.WHITE);
        txt.setForeground(Color.BLACK);
        txt.setCaretColor(Color.BLACK);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(txt, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(this, panel, baslik,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Onayla", "İptal"}, "Onayla");

        if (result == JOptionPane.OK_OPTION && !txt.getText().trim().isEmpty()) {
            return txt.getText().trim();
        }
        return null;
    }

    private String modernSecimAl(String baslik, String mesaj, Object[] secenekler) {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("<html><body style='width: 400px; color: #2C3E50; font-family: sans-serif; font-size: 14px; font-weight: bold;'>" + mesaj.replace("\n", "<br>") + "</body></html>");

        JComboBox<Object> cb = new JComboBox<>(secenekler);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cb.setPreferredSize(new Dimension(400, 45));
        cb.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(cb, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(this, panel, baslik,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Seç", "İptal"}, "Seç");

        if (result == JOptionPane.OK_OPTION && cb.getSelectedItem() != null) {
            return cb.getSelectedItem().toString();
        }
        return null;
    }


    // 7 HATA HAKKI KONTROL METOTLARI

    private Integer getGecerliPozitifSayi(String baslik, String mesaj, Integer maxKapasite) {
        int hak = 7;
        while(hak > 0) {
            String input = modernInputAl(baslik, mesaj);
            if(input == null) return null;
            try {
                int sayi = Integer.parseInt(input);
                if(sayi <= 0) {
                    JOptionPane.showMessageDialog(this, "Lütfen 0'dan büyük bir sayı giriniz!", "Hatalı Değer", JOptionPane.ERROR_MESSAGE);
                } else if (maxKapasite != null && sayi > maxKapasite) {
                    JOptionPane.showMessageDialog(this, "Girdiğiniz miktar maksimum sınırı (" + maxKapasite + ") aşıyor!", "Sınır Aşıldı", JOptionPane.ERROR_MESSAGE);
                } else {
                    return sayi;
                }
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı giriniz!", "Geçersiz Format", JOptionPane.ERROR_MESSAGE);
            }
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private Integer getGecerliRafIndex(String baslik, String mesaj) {
        int hak = 7;
        while(hak > 0) {
            String input = modernInputAl(baslik, mesaj);
            if(input == null) return null;
            try {
                int rafSira = Integer.parseInt(input);
                if(rafSira >= 1 && rafSira <= rafManager.getRafSayisi()) {
                    return rafSira - 1;
                }
                JOptionPane.showMessageDialog(this, "HATA! Lütfen 1 ile " + rafManager.getRafSayisi() + " arasında geçerli bir raf numarası giriniz.", "Geçersiz Raf", JOptionPane.ERROR_MESSAGE);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı giriniz!", "Geçersiz Format", JOptionPane.ERROR_MESSAGE);
            }
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private Urun getGecerliUrun(String baslik, String mesaj) {
        int hak = 7;
        while(hak > 0) {
            String seriNo = modernInputAl(baslik, mesaj);
            if(seriNo == null) return null;
            Urun u = urunBul(seriNo);
            if(u != null) return u;
            JOptionPane.showMessageDialog(this, "Ürün bulunamadı! Lütfen geçerli bir seri numarası giriniz.", "Bulunamadı", JOptionPane.ERROR_MESSAGE);
            hak--;
        }
        JOptionPane.showMessageDialog(this, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    // UI VE EKRAN OLUŞTURMA KISIMLARI

    private void setActiveButton(JButton activeButton) {
        for (JButton btn : menuButtons) {
            btn.setBackground(INACTIVE_BTN_BG);
            btn.setForeground(TEXT_COLOR);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        }
        activeButton.setBackground(ACTIVE_BTN_BG);
        activeButton.setForeground(Color.WHITE);
        activeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel baslik = new JLabel("Genel Bakış");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 32));
        baslik.setForeground(HEADER_TEXT_COLOR);
        panel.add(baslik, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        lblToplamUrun = new JLabel("0 Adet");
        lblToplamRaf = new JLabel("0 Raf");
        lblBosKapasite = new JLabel("0 Birim");

        statsPanel.add(new GradientCard("Sistemdeki Ürün Çeşidi", lblToplamUrun, new Color(52, 152, 219), new Color(116, 185, 255)));
        statsPanel.add(new GradientCard("Toplam Raf Sayısı", lblToplamRaf, new Color(39, 174, 96), new Color(85, 239, 196)));
        statsPanel.add(new GradientCard("Toplam Boş Kapasite", lblBosKapasite, new Color(231, 76, 60), new Color(255, 118, 117)));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(statsPanel, BorderLayout.NORTH);
        panel.add(centerWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUrunlerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel baslik = new JLabel("Ürün Operasyon Merkezi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        panel.add(baslik, BorderLayout.NORTH);

        JPanel islemGrid = new JPanel(new GridLayout(2, 2, 30, 30));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(50, 50, 50, 50));

        JButton btnYeniKayit = createBigActionButton("➕ Yeni Ürün Kaydı Ekle", "Depoya yepyeni bir ürün tanımlayın", new Color(46, 204, 113));
        JButton btnStokEkle = createBigActionButton("📦 Mevcut Stoğa Ekle", "Sistemde var olan ürünün adetini artırın", new Color(52, 152, 219));
        JButton btnUrunCikar = createBigActionButton("➖ Ürün Çıkar / Sil", "Depodan ürün çıkışı yapın", new Color(231, 76, 60));
        JButton btnUrunTasi = createBigActionButton("🔄 Ürün Taşı", "Ürünleri raflar arasında transfer edin", new Color(241, 196, 15));

        btnYeniKayit.addActionListener(e -> {
            if (rafManager.getRafSayisi() == 0) {
                JOptionPane.showMessageDialog(this, "Depoda hiç raf yok! Lütfen Raf İşlemleri menüsünden raf ekleyiniz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            } else {
                yeniKayitFormuAc();
            }
        });

        btnStokEkle.addActionListener(e -> mevcutStogaEkleFormuAc());
        btnUrunCikar.addActionListener(e -> urunCikarFormuAc());
        btnUrunTasi.addActionListener(e -> urunTasiFormuAc());

        islemGrid.add(btnYeniKayit);
        islemGrid.add(btnStokEkle);
        islemGrid.add(btnUrunCikar);
        islemGrid.add(btnUrunTasi);

        panel.add(islemGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createListelemePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel baslik = new JLabel("Envanter Listesi");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        topPanel.add(baslik, BorderLayout.WEST);

        JPanel aramaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        aramaPanel.setOpaque(false);

        JTextField txtArama = new JTextField(15);
        txtArama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Seri No / Ürün Adı Ara...");
        txtArama.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtArama.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtArama.setBackground(Color.WHITE);
        txtArama.setForeground(Color.BLACK);
        txtArama.setCaretColor(Color.BLACK);
        txtArama.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtArama.setPreferredSize(new Dimension(250, 40));

        JButton btnAra = new JButton("Ara");
        btnAra.setBackground(new Color(52, 152, 219));
        btnAra.setForeground(Color.WHITE);
        btnAra.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAra.setPreferredSize(new Dimension(100, 40));
        btnAra.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);

        btnAra.addActionListener(e -> {
            String kelime = txtArama.getText().toLowerCase().trim();
            tabloModeli.setRowCount(0);
            for (Urun u : urunManager.getUrunler()) {
                if (u.getSeriNo().equalsIgnoreCase(kelime) || u.getAd().toLowerCase().contains(kelime)) {
                    tabloModeli.addRow(new Object[]{u.getAd(), u.getSeriNo(), u.getMiktar(), u.getRafKodlariString(), u.getEklenmeTarihi()});
                }
            }
        });

        JButton btnTumunuGoster = new JButton("📋 Tümünü Listele");
        btnTumunuGoster.setBackground(new Color(155, 89, 182));
        btnTumunuGoster.setForeground(Color.WHITE);
        btnTumunuGoster.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTumunuGoster.setPreferredSize(new Dimension(170, 40));
        btnTumunuGoster.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnTumunuGoster.addActionListener(e -> tabloyuGuncelle());

        aramaPanel.add(txtArama);
        aramaPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        aramaPanel.add(btnAra);
        aramaPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        aramaPanel.add(btnTumunuGoster);

        topPanel.add(aramaPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        String[] kolonlar = {"Ürün Adı", "Seri Numarası", "Stok Miktarı", "Raf Dağılımı", "Kayıt Tarihi"};
        tabloModeli = new DefaultTableModel(null, kolonlar);
        JTable tablo = new JTable(tabloModeli);
        tablo.setRowHeight(40);
        tablo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        tablo.setBackground(Color.WHITE);
        tablo.setForeground(HEADER_TEXT_COLOR);
        tablo.setGridColor(new Color(230, 240, 250));
        tablo.setSelectionBackground(new Color(214, 234, 248));
        tablo.setSelectionForeground(Color.BLACK);

        tablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tablo.getTableHeader().setBackground(new Color(52, 152, 219));
        tablo.getTableHeader().setForeground(Color.WHITE);

        tablo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablo.getColumnModel().getColumn(0).setPreferredWidth(210); // Ürün Adı
        tablo.getColumnModel().getColumn(1).setPreferredWidth(140); // Seri Numarası
        tablo.getColumnModel().getColumn(2).setPreferredWidth(110); // Stok Miktarı
        tablo.getColumnModel().getColumn(3).setPreferredWidth(360); // Raf Dağılımı
        tablo.getColumnModel().getColumn(4).setPreferredWidth(170); // YENİ: Kayıt Tarihi

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablo.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablo.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tablo.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Tarih ortalandı

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRaflarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel baslik = new JLabel("Raf Düzeni ve Ayarları");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(HEADER_TEXT_COLOR);
        panel.add(baslik, BorderLayout.NORTH);

        JPanel islemGrid = new JPanel(new GridLayout(1, 2, 40, 40));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(100, 50, 150, 50));

        JButton btnRafEkle = createBigActionButton("🗄️ Yeni Raf Ekle", "Depoya yeni kapasite alanları tanımlayın", new Color(46, 204, 113));
        JButton btnRafSil = createBigActionButton("🗑️ Raf Sil", "Mevcut boş rafları sistemden kaldırın", new Color(231, 76, 60));

        btnRafEkle.addActionListener(e -> cokluRafEkleFormuAc());
        btnRafSil.addActionListener(e -> rafSilFormuAc());

        islemGrid.add(btnRafEkle);
        islemGrid.add(btnRafSil);

        panel.add(islemGrid, BorderLayout.CENTER);

        return panel;
    }

    // OPERASYON METOTLARI

    private void rafSilFormuAc() {
        if(rafManager.getRafSayisi() == 0) {
            JOptionPane.showMessageDialog(this, "Depoda silinecek raf yok!"); return;
        }

        Integer silinecekAdet = getGecerliPozitifSayi("Raf Sil", "Kaç adet raf silmek istiyorsunuz?\n(Mevcut: " + rafManager.getRafSayisi() + ")", rafManager.getRafSayisi());
        if (silinecekAdet == null) return;

        int basariylaSilinen = 0;

        for (int i = 0; i < silinecekAdet; i++) {
            Object[] mevcutRaflar = new Object[rafManager.getRafSayisi()];
            for(int j = 0; j < rafManager.getRafSayisi(); j++) {
                mevcutRaflar[j] = (j + 1) + ". Raf (Boş: " + rafManager.getRafKapasitesi(j) + ")";
            }

            String secilenRafStr = modernSecimAl("Raf Sil", (i + 1) + ". Silmek istediğiniz rafı seçiniz:", mevcutRaflar);

            if (secilenRafStr == null) {
                JOptionPane.showMessageDialog(this, "İşlem iptal edildi. Toplam " + basariylaSilinen + " adet raf silindi.");
                return;
            }

            int silinecekIndex = Integer.parseInt(secilenRafStr.split("\\.")[0]) - 1;

            if (urunManager.rafDoluMu(silinecekIndex)) {
                JOptionPane.showMessageDialog(this, "HATA! Seçtiğiniz " + (silinecekIndex + 1) + ". Raf DOLU.\nÖnce içindeki ürünleri taşıyın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (rafManager.rafSil(silinecekIndex)) {
                urunManager.rafSilindiktenSonraGuncelle(silinecekIndex);
                sistemiYenile();
                basariylaSilinen++;
            }
        }

        if (basariylaSilinen > 0) {
            JOptionPane.showMessageDialog(this, basariylaSilinen + " adet raf başarıyla silindi ve ürün indeksleri kaydırıldı!");
        }
    }

    private void cokluRafEkleFormuAc() {
        Integer eklenecekSayi = getGecerliPozitifSayi("Yeni Raf Ekle", "Kaç adet raf eklemek istiyorsunuz?", null);
        if(eklenecekSayi == null) return;

        RafDao rDao = new RafDao();
        int mevcutSayi = rafManager.getRafSayisi();

        for (int i = 0; i < eklenecekSayi; i++) {
            Integer kap = getGecerliPozitifSayi("Yeni Raf Ekle", (mevcutSayi + i + 1) + ". Rafın kapasitesini giriniz:", null);
            if(kap == null) return;
            rDao.rafEkle(new Raf(kap));
        }

        sistemiYenile();
        JOptionPane.showMessageDialog(this, eklenecekSayi + " adet yeni raf başarıyla eklendi!");
    }

    private void yeniKayitFormuAc() {
        JDialog dialog = new JDialog(this, "📦 Yeni Ürün Kaydı", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);

        JTextField txtAd = new JTextField();
        txtAd.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtAd.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtAd.setBackground(Color.WHITE);
        txtAd.setForeground(Color.BLACK);
        txtAd.setCaretColor(Color.BLACK);

        JTextField txtSeriNo = new JTextField();
        txtSeriNo.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtSeriNo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSeriNo.setBackground(Color.WHITE);
        txtSeriNo.setForeground(Color.BLACK);
        txtSeriNo.setCaretColor(Color.BLACK);

        JTextField txtMiktar = new JTextField();
        txtMiktar.putClientProperty(FlatClientProperties.STYLE, INPUT_STYLE);
        txtMiktar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMiktar.setBackground(Color.WHITE);
        txtMiktar.setForeground(Color.BLACK);
        txtMiktar.setCaretColor(Color.BLACK);

        JComboBox<String> cmbRaflar = new JComboBox<>();
        cmbRaflar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbRaflar.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);
        for (int i = 0; i < rafManager.getRafSayisi(); i++) {
            cmbRaflar.addItem((i + 1) + ". Raf (Boş: " + rafManager.getRafKapasitesi(i) + ")");
        }

        JLabel lblAd = new JLabel("Ürün Adı:"); lblAd.setForeground(HEADER_TEXT_COLOR); lblAd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblSeri = new JLabel("Seri Numarası:"); lblSeri.setForeground(HEADER_TEXT_COLOR); lblSeri.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblMiktar = new JLabel("Miktar:"); lblMiktar.setForeground(HEADER_TEXT_COLOR); lblMiktar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblRaf = new JLabel("Hedef Raf:"); lblRaf.setForeground(HEADER_TEXT_COLOR); lblRaf.setFont(new Font("Segoe UI", Font.BOLD, 13));

        formPanel.add(lblAd); formPanel.add(txtAd);
        formPanel.add(lblSeri); formPanel.add(txtSeriNo);
        formPanel.add(lblMiktar); formPanel.add(txtMiktar);
        formPanel.add(lblRaf); formPanel.add(cmbRaflar);

        JButton btnKaydet = new JButton("Kaydet");
        btnKaydet.setBackground(new Color(46, 204, 113));
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnKaydet.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btnKaydet.setPreferredSize(new Dimension(300, 45));

        final int[] hataSayaci = {0};

        btnKaydet.addActionListener(e -> {
            try {
                String ad = txtAd.getText();
                String seriNo = txtSeriNo.getText();

                if (ad.isEmpty() || seriNo.isEmpty()) throw new Exception("Ürün adı ve Seri No boş bırakılamaz!");

                int miktar;
                try {
                    miktar = Integer.parseInt(txtMiktar.getText());
                    if (miktar <= 0) throw new Exception("Miktar 0'dan büyük olmalı!");
                } catch(NumberFormatException ex) {
                    throw new Exception("Lütfen miktar için geçerli bir sayı giriniz!");
                }

                int rafIndex = cmbRaflar.getSelectedIndex();
                if (urunBul(seriNo) != null) throw new Exception("Bu seri numarası zaten var!");
                if (miktar > rafManager.getToplamKapasite()) throw new Exception("Depoda bu kadar boş yer yok!");

                int rafKapasite = rafManager.getRafKapasitesi(rafIndex);

                if (miktar > rafKapasite) {
                    int cevap = JOptionPane.showConfirmDialog(dialog,
                            "Seçilen rafta yeterli yer yok!\nBu rafta: " + rafKapasite + " boş yer var.\nÜrünü parça parça dağıtmak ister misiniz?",
                            "Kapasite Yetersiz", JOptionPane.YES_NO_OPTION);

                    if (cevap == JOptionPane.YES_OPTION) {
                        dialog.dispose();
                        parcaliEklemeBaslat("Yeni Ürün Kaydı", ad, seriNo, miktar, null);
                    }
                    return;
                }

                Urun yeniUrun = new Urun(ad, seriNo, miktar, rafIndex);
                urunManager.getUrunler().add(yeniUrun);
                rafManager.kapasiteGuncelle(rafIndex, -miktar);

                urunDao.veritabaniniGuncelle(urunManager.getUrunler());
                sistemiYenile();

                JOptionPane.showMessageDialog(this, "Ürün Başarıyla Eklendi!");
                dialog.dispose();

            } catch (Exception ex) {
                hataSayaci[0]++;
                if(hataSayaci[0] >= 7) {
                    JOptionPane.showMessageDialog(dialog, "Çok fazla hatalı deneme yaptınız!\nİşlem iptal edildi.", "Güvenlik İptali", JOptionPane.WARNING_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel altPanel = new JPanel();
        altPanel.setBackground(Color.WHITE);
        altPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        altPanel.add(btnKaydet);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(altPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void parcaliEklemeBaslat(String islemBasligi, String ad, String seriNo, int toplamMiktar, Urun mevcutUrun) {
        int kalan = toplamMiktar;
        Urun islemUrunu = mevcutUrun;

        if (islemUrunu == null) {
            islemUrunu = new Urun(ad, seriNo, toplamMiktar, 0);
            islemUrunu.getRafDagilimi().clear();
        } else {
            islemUrunu.setMiktar(islemUrunu.getMiktar() + toplamMiktar);
        }

        while (kalan > 0) {
            Integer secilenRaf = getGecerliRafIndex(islemBasligi, "Dağıtılacak Toplam: " + toplamMiktar + " | Kalan: " + kalan +
                    "\n\nHangi rafa ekleyeceksiniz? (1-" + rafManager.getRafSayisi() + "):");
            if (secilenRaf == null) break;

            int bosYer = rafManager.getRafKapasitesi(secilenRaf);

            if (bosYer <= 0) {
                JOptionPane.showMessageDialog(this, "Bu raf tamamen dolu! Lütfen başka bir raf seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Integer eklenecek = getGecerliPozitifSayi(islemBasligi, (secilenRaf + 1) + ". Raf Boş Kapasite: " + bosYer + "\nKaç adet koyacaksınız?", Math.min(bosYer, kalan));
            if (eklenecek == null) break;

            rafManager.kapasiteGuncelle(secilenRaf, -eklenecek);
            islemUrunu.rafVeMiktarEkle(secilenRaf, eklenecek);

            kalan -= eklenecek;
        }

        if (mevcutUrun == null && !islemUrunu.getRafDagilimi().isEmpty()) {
            islemUrunu.setRafIndex(islemUrunu.getRafDagilimi().keySet().iterator().next());
            urunManager.getUrunler().add(islemUrunu);
        }

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
    }

    private void mevcutStogaEkleFormuAc() {
        Urun u = getGecerliUrun("Mevcut Stoğa Ekle", "Stok eklenecek ürünün Seri Numarasını giriniz:");
        if (u == null) return;

        Integer miktar = getGecerliPozitifSayi("Mevcut Stoğa Ekle", "Ürüne kaç adet eklenecek?", null);
        if (miktar == null) return;

        Integer rafIndex = getGecerliRafIndex("Mevcut Stoğa Ekle", "Hangi rafa eklenecek? (1-" + rafManager.getRafSayisi() + "):");
        if (rafIndex == null) return;

        if (miktar > rafManager.getRafKapasitesi(rafIndex)) {
            int cevap = JOptionPane.showConfirmDialog(this, "Seçilen rafta yer yok! Parçalı eklemek ister misiniz?", "Kapasite Yetersiz", JOptionPane.YES_NO_OPTION);
            if(cevap == JOptionPane.YES_OPTION) parcaliEklemeBaslat("Mevcut Stoğa Ekle", u.getAd(), u.getSeriNo(), miktar, u);
            return;
        }

        rafManager.kapasiteGuncelle(rafIndex, -miktar);
        u.setMiktar(u.getMiktar() + miktar);
        u.rafVeMiktarEkle(rafIndex, miktar);

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Stok başarıyla güncellendi!");
    }

    private void urunCikarFormuAc() {
        Urun u = getGecerliUrun("Ürün Çıkar / Sil", "Çıkarılacak ürünün Seri Numarasını giriniz:");
        if(u == null) return;

        if(u.getRafDagilimi().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sistem hatası: Bu ürünün kayıtlı olduğu bir raf bulunamadı."); return;
        }

        Object[] mevcutRaflar = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray();
        String secilenRafStr = modernSecimAl("Ürün Çıkar / Sil", "Hangi raftan ürün çıkaracaksınız?\n(Mevcut Dağılım: " + u.getRafKodlariString() + ")", mevcutRaflar);

        if (secilenRafStr == null) return;
        int secilenRaf = Integer.parseInt(secilenRafStr.split("\\.")[0]) - 1;

        int raftakiMiktar = u.getRafDagilimi().get(secilenRaf);
        Integer miktar = getGecerliPozitifSayi("Ürün Çıkar / Sil", "Bu rafta " + raftakiMiktar + " adet var.\nKaç adet çıkarılacak?", raftakiMiktar);
        if(miktar == null) return;

        rafManager.kapasiteGuncelle(secilenRaf, +miktar);
        u.raftanUrunEksilt(secilenRaf, miktar);
        u.setMiktar(u.getMiktar() - miktar);

        if (u.getMiktar() == 0) {
            urunManager.getUrunler().remove(u);
            JOptionPane.showMessageDialog(this, "Ürün stoğu tamamen bittiği için sistemden silindi.");
        } else {
            JOptionPane.showMessageDialog(this, "Ürün çıkışı başarılı! Kapasiteler iade edildi.");
        }

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
    }

    private void urunTasiFormuAc() {
        Urun u = getGecerliUrun("Ürün Taşı", "Taşınacak ürünün Seri Numarasını giriniz:");
        if(u == null) return;

        if(u.getRafDagilimi().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sistem hatası: Bu ürünün kayıtlı olduğu bir raf bulunamadı."); return;
        }

        Object[] mevcutRaflar = u.getRafDagilimi().keySet().stream().map(r -> (r + 1) + ". Raf").toArray();
        String eskiRafStr = modernSecimAl("Ürün Taşı", "Hangi raftaki ürünleri taşıyacaksınız?", mevcutRaflar);

        if (eskiRafStr == null) return;
        int eskiRaf = Integer.parseInt(eskiRafStr.split("\\.")[0]) - 1;

        int raftakiMiktar = u.getRafDagilimi().get(eskiRaf);
        Integer tasinacakMiktar = getGecerliPozitifSayi("Ürün Taşı", "Bu rafta " + raftakiMiktar + " adet var. Kaçını taşıyacaksınız?", raftakiMiktar);
        if (tasinacakMiktar == null) return;

        Integer yeniRaf = getGecerliRafIndex("Ürün Taşı", "Hedef raf numarası (1-" + rafManager.getRafSayisi() + "):");
        if (yeniRaf == null) return;

        if (rafManager.getRafKapasitesi(yeniRaf) < tasinacakMiktar) {
            JOptionPane.showMessageDialog(this, "Hedef rafta yeterli yer yok!", "Hata", JOptionPane.ERROR_MESSAGE); return;
        }

        rafManager.kapasiteGuncelle(eskiRaf, +tasinacakMiktar);
        rafManager.kapasiteGuncelle(yeniRaf, -tasinacakMiktar);
        u.rafTasimaGuncellemesi(eskiRaf, yeniRaf, tasinacakMiktar);

        urunDao.veritabaniniGuncelle(urunManager.getUrunler());
        sistemiYenile();
        JOptionPane.showMessageDialog(this, "Taşıma işlemi başarılı!\nYeni Dağılım: " + u.getRafKodlariString());
    }

    // YARDIMCI METOTLAR

    private Urun urunBul(String seriNo) {
        return urunManager.urunBul(seriNo);
    }

    private void sistemiYenile() {
        this.rafManager = new RafManager();
        this.urunManager = new UrunManager(this.rafManager);
        dashboardGuncelle();
        tabloyuGuncelle();
    }

    private void dashboardGuncelle() {
        lblToplamUrun.setText(urunManager.getUrunler().size() + " Adet");
        lblToplamRaf.setText(rafManager.getRafSayisi() + " Raf");
        lblBosKapasite.setText(rafManager.getToplamKapasite() + " Birim");
        lblToplamUrun.getParent().revalidate();
    }

    private void tabloyuGuncelle() {
        if (tabloModeli != null) {
            tabloModeli.setRowCount(0);
            for(Urun u : urunManager.getUrunler()) {
                // YENİ: Tabloya Eklenme Tarihini basıyoruz
                tabloModeli.addRow(new Object[]{u.getAd(), u.getSeriNo(), u.getMiktar(), u.getRafKodlariString(), u.getEklenmeTarihi()});
            }
        }
    }

    private JButton createMenuButton(String text, String pageName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(INACTIVE_BTN_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMargin(new Insets(10, 15, 10, 15));
        btn.setMaximumSize(new Dimension(300, 65));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        if (pageName != null) {
            btn.addActionListener(e -> { cardLayout.show(mainContent, pageName); setActiveButton(btn); });
            menuButtons.add(btn);
        }
        return btn;
    }

    private JButton createBigActionButton(String title, String subtitle, Color bg) {
        JButton btn = new JButton("<html><center><font size='5' color='#FFFFFF'>" + title + "</font><br><font size='3' color='#F0F0F0'>" + subtitle + "</font></center></html>");
        btn.setBackground(bg); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    private class GradientCard extends JPanel {
        private Color color1, color2;
        public GradientCard(String title, JLabel valueLabel, Color c1, Color c2) {
            this.color1 = c1; this.color2 = c2;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBorder(new EmptyBorder(25, 25, 25, 25)); setOpaque(false);
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitle.setForeground(new Color(255, 255, 255, 230));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); valueLabel.setForeground(Color.WHITE);
            add(lblTitle); add(Box.createRigidArea(new Dimension(0, 10))); add(valueLabel);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
            g2d.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
        }
    }
}