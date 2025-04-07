package wang.jinjing.editor.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashUtils {


    /**
     * 计算SHA256哈希值
     * @param filePath 文件路径
     * @return 字节数组
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException NoSearch算法异常
     */
    public static byte[] calculateSHA256(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (
                FileInputStream fis = new FileInputStream(filePath);
                FileChannel channel = fis.getChannel();
                DigestInputStream dis = new DigestInputStream(fis, digest)) {
            ByteBuffer buffer = ByteBuffer.allocate(8192); // 8 KB buffer
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
            return digest.digest();
        }
    }

    public static byte[] calculateSHA256(FileInputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (
                FileInputStream fis = inputStream;
                FileChannel channel = fis.getChannel();
                DigestInputStream dis = new DigestInputStream(fis, digest)) {
            ByteBuffer buffer = ByteBuffer.allocate(8192); // 8 KB buffer
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
            return digest.digest();
        }
    }

    public static String calculateMD5(InputStream inputStream) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            try (DigestInputStream dis = new DigestInputStream(inputStream, md5)) {
                // 完全读取流以确保计算完整摘要
                while (dis.read(buffer) != -1) {
                    // 仅读取数据，DigestInputStream会自动更新摘要
                    continue;
                }
            }
            return bytesToHex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            // MD5算法在所有Java实现中必须存在，此异常理论上不会发生
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * 将字节数组转换为String类型哈希值
     * @param bytes 字节数组
     * @return 哈希值
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF; // 处理负数值
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
