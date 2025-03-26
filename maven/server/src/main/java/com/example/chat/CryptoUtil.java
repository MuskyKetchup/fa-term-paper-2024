package com.example.chat;

/**
 * Утилитный класс для шифрования и дешифрования сообщений.
 * Использует собственный лёгкий алгоритм на основе XOR для транспортного шифрования.
 * Примечание: алгоритм упрощён (не соответствует ГОСТ 28147-89, но демонстрирует 
 * принцип симметричного шифрования без использования готовых библиотек).
 */
import java.nio.charset.StandardCharsets;

public class CryptoUtil {
    // Секретный ключ для симметричного шифрования (задан статически)
    private static final String SECRET_KEY = "mysecretkey";

    /**
     * Шифрует заданный открытый текст с помощью простого алгоритма 
     * XOR на основе секретного ключа. Результат возвращается 
     * в виде шестнадцатеричной строки.
     *
     * @param plaintext Открытый текст
     * @return Зашифрованный текст в виде строки (hex)
     */
    public static String encrypt(String plaintext) {
        byte[] messageBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = new byte[messageBytes.length];
        // Применяем XOR побайтно
        for (int i = 0; i < messageBytes.length; i++) {
            encryptedBytes[i] = (byte) (messageBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        // Переводим в шестнадцатеричную строку для удобства передачи
        StringBuilder hexString = new StringBuilder();
        for (byte b : encryptedBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Расшифровывает заданную строку (hex-кодированное шифрованное сообщение) 
     * обратно в открытый текст с помощью алгоритма XOR и секретного ключа.
     *
     * @param cipherHex Зашифрованный текст в виде шестнадцатеричной строки
     * @return Расшифрованный открытый текст
     */
    public static String decrypt(String cipherHex) {
        // Преобразуем hex-строку в байты
        int length = cipherHex.length();
        byte[] encryptedBytes = new byte[length / 2];
        for (int i = 0; i < encryptedBytes.length; i++) {
            int j = Integer.parseInt(cipherHex.substring(2 * i, 2 * i + 2), 16);
            encryptedBytes[i] = (byte) j;
        }
        // Применяем XOR с тем же ключом для получения исходного текста
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedBytes = new byte[encryptedBytes.length];
        for (int i = 0; i < encryptedBytes.length; i++) {
            decryptedBytes[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
