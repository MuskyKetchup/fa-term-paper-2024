package com.example.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JTextField serverField;
    private JTextField portField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Вход в чат");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Создаем компоненты интерфейса
        JLabel userLabel = new JLabel("Имя пользователя:");
        JLabel passLabel = new JLabel("Пароль:");
        JLabel serverLabel = new JLabel("Адрес сервера:");
        JLabel portLabel = new JLabel("Порт:");
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        serverField = new JTextField("localhost", 15);
        portField = new JTextField("12345", 5);
        loginButton = new JButton("Войти");
        // Панель с сеткой 4x2 для полей ввода и надписей
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        fieldsPanel.add(userLabel);
        fieldsPanel.add(userField);
        fieldsPanel.add(passLabel);
        fieldsPanel.add(passField);
        fieldsPanel.add(serverLabel);
        fieldsPanel.add(serverField);
        fieldsPanel.add(portLabel);
        fieldsPanel.add(portField);
        // Панель для кнопки (вправо)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        // Основная панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(fieldsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        // Кнопка входа по умолчанию (Enter)
        getRootPane().setDefaultButton(loginButton);

        // Обработчик нажатия на "Войти"
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    /**
     * Выполняет подключение к серверу и отправку данных для входа.
     * При успехе – открытие окна чата, при ошибке – отображение сообщения.
     */
    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String serverAddress = serverField.getText().trim();
        String portText = portField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || serverAddress.isEmpty() || portText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.", 
                                          "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный номер порта.", 
                                          "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            // Подключаемся к серверу
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // Отправляем зашифрованное сообщение логина
            String loginMessage = "LOGIN|" + username + "|" + password;
            String encrypted = CryptoUtil.encrypt(loginMessage);
            out.println(encrypted);
            // Ждем ответ от сервера
            String responseEncrypted = in.readLine();
            if (responseEncrypted == null) {
                throw new IOException("Сервер разорвал соединение");
            }
            String response = CryptoUtil.decrypt(responseEncrypted);
            if ("LOGIN_OK".equals(response)) {
                // Успешный вход – открываем окно чата
                ChatFrame chatFrame = new ChatFrame(username, socket, in, out);
                chatFrame.setVisible(true);
                this.dispose();
            } else if ("LOGIN_FAIL".equals(response)) {
                // Неуспешный вход
                JOptionPane.showMessageDialog(this, "Неверное имя пользователя или пароль.", 
                                              "Ошибка входа", JOptionPane.ERROR_MESSAGE);
                try { socket.close(); } catch (IOException ex) { /* ignore */ }
            } else {
                // Неожиданный ответ
                JOptionPane.showMessageDialog(this, "Не удалось выполнить вход. Ответ: " + response, 
                                              "Ошибка входа", JOptionPane.ERROR_MESSAGE);
                try { socket.close(); } catch (IOException ex) { /* ignore */ }
            }
        } catch (IOException ex) {
            if (socket != null) {
                try { socket.close(); } catch (IOException exc) { /* ignore */ }
            }
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу: " + ex.getMessage(),
                                          "Сеть недоступна", JOptionPane.ERROR_MESSAGE);
        }
    }
}
