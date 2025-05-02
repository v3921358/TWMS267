package Server.login;

import Client.MapleClient;
import Server.login.handler.LoginPasswordHandler;
import Server.world.World;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SpecLoginServer extends Thread {

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(18800);
            for (; ; ) {
                Socket socket = ss.accept();
                new Thread(() -> {
                    try {
                        //用於向客戶端發送數據的輸出流
                        BufferedWriter dos = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        //用於接收客戶端發來的數據的輸入流
                        BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String IP = "/" + socket.getInetAddress().getHostAddress();
                        String login = dis.readLine();
                        String pwd;
                        String port;
//                        if (Config.isDevelop()) {
//                            login = "admin";
//                            pwd = "admin";
//                        } else
                        if (login.split(" ").length > 1) {
                            pwd = login.split(" ")[1].split("/")[2];
                            login = login.split(" ")[1].split("/")[1];
                        } else {
                            pwd = dis.readLine();
                        }
                        System.out.println("接收到登入請求：" + IP + " 帳號：" + login + " 密碼：" + pwd);
                        boolean suc = false;
                        for (MapleClient c : World.Client.getClients()) {
                            if (c.isLoggedIn()) continue;
//                        for (Channel session : MapleServerHandler.CHANNEL_GROUP) {
//                            MapleClient c = session.attr(MapleClient.CLIENT_KEY).get();
                            String IP2 = c.getSessionIPAddress();
                            if (IP2.equals(IP)) {
                                //伺服器向客戶端發送連接成功確認信息
                                suc = true;
                            } else if (IP2.equals("/221.231.130.70") && IP.equals("/127.0.0.1")) {
                                suc = true;
                            }
                            if (suc) {
                                LoginPasswordHandler.specLogin(login, pwd, c);
                            }
                        }
                        dos.write(suc ? "Login Succeed" : "Login Failed");
                        dos.flush();
                        //不需要繼續使用此連接時，關閉連接
                        socket.close();
                    } catch (Throwable e) {
                        if (!(e instanceof ArrayIndexOutOfBoundsException)) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("綁定連接埠18800失敗");
            e.printStackTrace();
        }
    }
}
