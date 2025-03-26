package com.example.chat;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    // Параметры сервера и базы данных
    private static final int SERVER_PORT = 12345;
    private static final String DB_URL = "jdbc:mysql://localhost:3307/chatdb?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    // Соединение с базой данных
    private static Connection dbConnection;
    // Активные пользователи: имя пользователя -> обработчик соединения
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Запуск сервера чата...");
        // Подключение к базе данных
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Соединение с базой данных установлено.");
        } catch (Exception e) {
            System.err.println("Не удалось подключиться к базе данных: " + e.getMessage());
            return;
        }

        // Запуск серверного сокета
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Сервер запущен на порту " + SERVER_PORT + ", ожидаем подключения...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Новый клиент подключился
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start(); // запускаем поток обслуживания
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            // Закрытие соединения с БД при остановке сервера
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException e) {
                    /* ignore */ }
            }
        }
    }

    /**
     * Сохранение сообщения в базе данных (таблица messages).
     * Выполняется синхронно (блокирует на время вставки) для потокобезопасности.
     */
    public static synchronized void saveMessage(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO messages(sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении сообщения: " + e.getMessage());
        }
    }

    /**
     * Класс-поток для обслуживания одного клиента.
     * Принимает сообщения от клиента и отвечает ему.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String userName;
        private int userId;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            System.out.println("Клиент подключился: " + clientInfo);
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ожидаем первое сообщение с данными для логина
                String encrypted = in.readLine();
                if (encrypted == null) {
                    socket.close();
                    return;
                }
                String decrypted = CryptoUtil.decrypt(encrypted);
                if (decrypted.startsWith("LOGIN|")) {
                    // Парсим логин и пароль
                    String[] parts = decrypted.split("\\|", 3);
                    if (parts.length == 3) {
                        String login = parts[1];
                        String pass = parts[2];
                        // Проверка логина/пароля в базе
                        int dbId = authenticateUser(login, pass);
                        if (dbId != -1) {
                            if (clients.containsKey(login)) {
                                // Пользователь уже в сети - отказываем
                                out.println(CryptoUtil.encrypt("LOGIN_FAIL"));
                                socket.close();
                                return;
                            }
                            // Успешно: регистрируем пользователя
                            userName = login;
                            userId = dbId;
                            clients.put(userName, this);
                            System.out.println("Пользователь " + userName + " успешно вошел.");
                            out.println(CryptoUtil.encrypt("LOGIN_OK"));
                        } else {
                            // Неверные учетные данные
                            out.println(CryptoUtil.encrypt("LOGIN_FAIL"));
                            socket.close();
                            return;
                        }
                    } else {
                        // Неправильный формат логина
                        out.println(CryptoUtil.encrypt("LOGIN_FAIL"));
                        socket.close();
                        return;
                    }
                } else {
                    // Первое сообщение не LOGIN – завершаем соединение
                    socket.close();
                    return;
                }

                // Основной цикл: приём и обработка сообщений от клиента
                String encryptedMsg;
                while ((encryptedMsg = in.readLine()) != null) {
                    String plainMsg = CryptoUtil.decrypt(encryptedMsg);
                    if (plainMsg.startsWith("MSG|")) {
                        String[] parts = plainMsg.split("\\|", 3);
                        if (parts.length == 3) {
                            String targetUser = parts[1];
                            String messageText = parts[2];
                            ClientHandler targetHandler = clients.get(targetUser);
                            if (targetHandler != null) {
                                // Отправляем сообщение получателю
                                String forwardPlain = "MSG|" + userName + "|" + messageText;
                                targetHandler.out.println(CryptoUtil.encrypt(forwardPlain));
                                // Сохраняем сообщение в БД
                                saveMessage(this.userId, targetHandler.userId, messageText);
                            } else {
                                // Получатель не подключён
                                String errorPlain = "ERROR|Пользователь " + targetUser + " не в сети.";
                                out.println(CryptoUtil.encrypt(errorPlain));
                            }
                        }
                    } else {
                        // Если потребуется – обработка других типов сообщений
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка связи с клиентом: " + e.getMessage());
            } finally {
                // Отключение клиента
                if (userName != null) {
                    clients.remove(userName);
                    System.out.println("Пользователь " + userName + " отключился.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    /* ignore */ }
            }
        }

        /**
         * Проверка учетных данных пользователя по базе данных.
         * 
         * @return ID пользователя, если логин/пароль верны, или -1 при неудаче.
         */
        private int authenticateUser(String login, String pass) {
            String query = "SELECT id FROM users WHERE username=? AND password=?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
                stmt.setString(1, login);
                stmt.setString(2, pass);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при запросе к БД: " + e.getMessage());
            }
            return -1;
        }
    }
}
