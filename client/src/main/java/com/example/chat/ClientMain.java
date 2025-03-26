package com.example.chat;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class ClientMain {
    public static void main(String[] args) {
        // Устанавливаем системный стиль оформления для Swing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Если не удалось установить Look&Feel, продолжим с дефолтным
        }
        // Запускаем окно логина в потоке EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
