import java.util.Random;
import java.util.Scanner;

public class PasswordManager {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==============================");
        System.out.println("欢迎使用密码管理系统");
        System.out.println("==============================");

        int choice;
        do {
            displayMenu();
            choice = scanner.nextInt();
            scanner.nextLine(); // 消费掉换行符

            switch (choice) {
                case 1:
                    System.out.println("请输入要加密的密码：");
                    String plainPassword = scanner.nextLine();
                    String encryptedPassword = encryptPassword(plainPassword);
                    System.out.println("加密后的密码为：" + encryptedPassword);
                    break;
                case 2:
                    System.out.println("请输入要解密的密码：");
                    String encryptedPassword2 = scanner.nextLine();
                    String decryptedPassword = decryptPassword(encryptedPassword2);
                    System.out.println("解密后的密码为：" + decryptedPassword);
                    break;
                case 3:
                    System.out.println("请输入要判断强度的密码：");
                    String password = scanner.nextLine();
                    String strength = getPasswordStrength(password);
                    System.out.println("密码强度为：" + strength);
                    break;
                case 4:
                    System.out.println("请输入生成密码的长度：");
                    int length = scanner.nextInt();
                    scanner.nextLine(); // 消费掉换行符
                    String generatedPassword = generatePassword(length);
                    System.out.println("生成的密码为：" + generatedPassword);
                    break;
                case 5:
                    System.out.println("感谢使用密码管理系统！");
                    break;
                default:
                    System.out.println("无效的选择");
                    break;
            }

            System.out.println("==============================");
        } while (choice != 5);

        scanner.close();
    }

    public static void displayMenu() {
        System.out.println("请选择操作：");
        System.out.println("1. 加密");
        System.out.println("2. 解密");
        System.out.println("3. 判断密码强度");
        System.out.println("4. 密码生成");
        System.out.println("5. 退出");
    }

    public static String encryptPassword(String plainPassword) {
        StringBuilder encryptedPassword = new StringBuilder();
        int offset = 3;

        // 将每个字符的ASCII码加上它在字符串中的位置(1开始)和偏移值3
        for (int i = 0; i < plainPassword.length(); i++) {
            char ch = plainPassword.charAt(i);
            int ascii = (int) ch;
            int newPosition = ascii + i + 1 + offset;
            encryptedPassword.append((char) newPosition);
        }

        // 将字符串的第一位和最后一位调换顺序
        char firstChar = encryptedPassword.charAt(0);
        char lastChar = encryptedPassword.charAt(encryptedPassword.length() - 1);
        encryptedPassword.setCharAt(0, lastChar);
        encryptedPassword.setCharAt(encryptedPassword.length() - 1, firstChar);

        // 将字符串反转
        encryptedPassword.reverse();

        return encryptedPassword.toString();
    }

    public static String decryptPassword(String encryptedPassword) {
        StringBuilder decryptedPassword = new StringBuilder();

        // 将字符串反转
        encryptedPassword = new StringBuilder(encryptedPassword).reverse().toString();

        // 将字符串的第一位和最后一位调换顺序
        char firstChar = encryptedPassword.charAt(0);
        char lastChar = encryptedPassword.charAt(encryptedPassword.length() - 1);
        encryptedPassword = encryptedPassword.substring(1, encryptedPassword.length() - 1);
        decryptedPassword.append(lastChar).append(encryptedPassword).append(firstChar);

        int offset = 3;

        // 将每个字符的ASCII码减去它在字符串中的位置(1开始)和偏移值3
        for (int i = 0; i < decryptedPassword.length(); i++) {
            char ch = decryptedPassword.charAt(i);
            int ascii = (int) ch;
            int newPosition = ascii - i - 1 - offset;
            decryptedPassword.setCharAt(i, (char) newPosition);
        }

        return decryptedPassword.toString();
    }

    public static String getPasswordStrength(String password) {
        int length = password.length();
        boolean hasNumber = false;
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;

        // 检查密码中每个字符的类型
        for (int i = 0; i < length; i++) {
            char ch = password.charAt(i);
            if (Character.isDigit(ch)) {
                hasNumber = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            } else if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            }
        }

        // 判断密码强度
        if (length < 8 || (length >= 8 && !hasNumber && !hasLowerCase && !hasUpperCase)) {
            return "弱强度";
        } else if (hasNumber && (hasLowerCase || hasUpperCase)) {
            return "中强度";
        } else if (hasNumber && hasLowerCase && hasUpperCase) {
            return "高强度";
        } else {
            return "未知";
        }
    }

    public static String generatePassword(int length) {
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";

        String availableChars = upperCaseChars + lowerCaseChars + numberChars;
        StringBuilder generatedPassword = new StringBuilder();
        Random random = new Random();

        // 随机生成密码
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(availableChars.length());
            char randomChar = availableChars.charAt(randomIndex);
            generatedPassword.append(randomChar);
        }

        return generatedPassword.toString();
    }
}
