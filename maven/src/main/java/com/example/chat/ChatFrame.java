package com.example.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatFrame extends JFrame {
    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextField targetField;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    public ChatFrame(String username, Socket socket, BufferedReader in, PrintWriter out) {
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        setTitle("Чат - " + username);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Элементы интерфейса
        JLabel userLabel = new JLabel("Вы: " + username);
        JLabel targetLabel = new JLabel("Получатель:");
        targetField = new JTextField(10);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        messageField = new JTextField();
        sendButton = new JButton("Отправить");
        // Верхняя панель (инфо о себе и поле для получателя)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(userLabel);
        topPanel.add(Box.createHorizontalStrut(20)); // отступ
        topPanel.add(targetLabel);
        topPanel.add(targetField);
        // Нижняя панель (поле ввода + кнопка)
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        // Добавляем панели в окно
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(topPanel, BorderLayout.NORTH);
        cp.add(scrollPane, BorderLayout.CENTER);
        cp.add(inputPanel, BorderLayout.SOUTH);
        // Обработчик кнопки "Отправить"
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        // Отправка по Enter из поля ввода
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        // Поток для получения сообщений от сервера
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String encrypted;
                    while ((encrypted = in.readLine()) != null) {
                        String message = CryptoUtil.decrypt(encrypted);
                        if (message.startsWith("MSG|")) {
                            String[] parts = message.split("\\|", 3);
                            if (parts.length == 3) {
                                String sender = parts[1];
                                String msgText = parts[2];
                                chatArea.append(sender + ": " + msgText + "\n");
                                chatArea.setCaretPosition(chatArea.getDocument().getLength());
                            }
                        } else if (message.startsWith("ERROR|")) {
                            String[] parts = message.split("\\|", 2);
                            if (parts.length == 2) {
                                chatArea.append("[Система]: " + parts[1] + "\n");
                                chatArea.setCaretPosition(chatArea.getDocument().getLength());
                            }
                        }
                    }
                    // Соединение закрыто
                    chatArea.append("[Система]: Соединение закрыто.\n");
                } catch (IOException e) {
                    chatArea.append("[Система]: Ошибка получения данных.\n");
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Обработчик закрытия окна (для закрытия соединения)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException ex) {
                    // игнорируем
                }
            }
        });
    }

    /**
     * Отправляет сообщение, введенное пользователем, на сервер.
     */
    private void sendMessage() {
        String target = targetField.getText().trim();
        String msgText = messageField.getText().trim();
        if (target.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Укажите имя получателя.", 
                                          "Не указан получатель", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (msgText.isEmpty()) {
            return; // пустое сообщение не отправляем
        }
        // Шифруем и отправляем сообщение
        String plainMessage = "MSG|" + target + "|" + msgText;
        String encrypted = CryptoUtil.encrypt(plainMessage);
        out.println(encrypted);
        // Отображаем свое сообщение в окне чата
        chatArea.append("Вы: " + msgText + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        messageField.setText("");
    }
}
