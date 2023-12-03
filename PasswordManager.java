import java.util.Scanner;
public class PasswordManager {

    public static String encrypt(String password) {
        StringBuilder encrypted_password = new StringBuilder();

        // 将每个字符的ASCII码加上它在字符串中的位置(1开始)和偏移值3
        for (int i = 0; i < password.length(); i++) {
            char ch = (char) (password.charAt(i) + i + 1 + 3);
            encrypted_password.append(ch);
        }

        // 将字符串的第一位和最后一位调换顺序
        char first_ch = encrypted_password.charAt(0);
        char last_ch = encrypted_password.charAt(encrypted_password.length() - 1);
        encrypted_password.setCharAt(0, last_ch);
        encrypted_password.setCharAt(encrypted_password.length() - 1, first_ch);

        // 将字符串反转
        return encrypted_password.reverse().toString();
    }

    // 解密功能
    public static String decrypt(String encrypted_password) {
        StringBuilder password = new StringBuilder(encrypted_password);

        // 将字符串反转
        password.reverse();

        // 将字符串的第一位和最后一位调换顺序
        char first_ch = password.charAt(0);
        char last_ch = password.charAt(password.length() - 1);
        password.setCharAt(0, last_ch);
        password.setCharAt(password.length() - 1, first_ch);

        // 将每个字符的ASCII码减去它在字符串中的位置(1开始)和偏移值3
        StringBuilder decrypted_password = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            char ch = (char) (password.charAt(i) - i - 1 - 3);
            decrypted_password.append(ch);
        }

        return decrypted_password.toString();
    }
    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println("欢迎使用密码管理系统");
        System.out.println("==============================");

        Scanner scanner = new Scanner(System.in); // 创建Scanner对象获取用户的输入

        while (true) {
            System.out.println("请选择操作：");
            System.out.println("1. 加密");
            System.out.println("2. 解密");

            String choice = scanner.next();

            if (choice.equals("1")) {
                System.out.print("请输入要加密的密码：");
                String password = scanner.next();
                String encrypted_password = encrypt(password);
                System.out.println("加密后的密码为：" + encrypted_password);
            } else if (choice.equals("2")) {
                System.out.print("请输入要解密的密码：");
                String encrypted_password = scanner.next();
                String decrypted_password = decrypt(encrypted_password);
                System.out.println("解密后的密码为：" + decrypted_password);
            } else {
                System.out.println("请输入有效的选项！");
            }
        }
    }

}
