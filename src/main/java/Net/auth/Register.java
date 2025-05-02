package Net.auth;

import Net.auth.client.AuthServer;
import Net.auth.packet.AuthPacket;

import java.util.Scanner;

class Register {

    private static final Scanner scanner = new Scanner(System.in);

    Register(AuthServer c) {
        System.err.println("當前機器未註冊，請先進行註冊");
        register(c);
    }

    private void register(AuthServer c) {
        String user = needUser();
        String sn = needSN();
        c.announce(AuthPacket.authChangeRequest(c.getTick(), user, sn));
    }

    private String needUser() {
        System.err.print("請輸入用戶名:");
        String user = scanner.next();
        if (user.length() < 5) {
            System.err.println("用戶名長度不足");
            return needUser();
        } else if (!user.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("用戶名無效");
            return needUser();
        }
        return user;
    }

    private String needSN() {
        System.err.print("請輸入序列號:");
        String sn = scanner.next();
        if (!sn.matches("^[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}$")) {
            System.err.println("序列號無效");
            return needSN();
        }
        return sn;
    }
}
