import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // 1. Arayüz Temasını
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Tema yüklenemedi: " + ex.getMessage());
        }

        // Main.java içindeki run() metodu şöyle olmalı
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // AnaEkran yerine LoginEkran'ı başlatıyoruz
                LoginEkran login = new LoginEkran();
                login.setVisible(true);
            }
        });

    }
}