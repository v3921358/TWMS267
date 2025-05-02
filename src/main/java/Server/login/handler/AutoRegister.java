package Server.login.handler;

import Client.LoginCrypto;
import Database.DatabaseLoader.DatabaseConnectionEx;
import tools.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AutoRegister {

    public static boolean createAccount(String login, String pwd) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, login);
                ps.setString(2, LoginCrypto.hexSha1(pwd));
                ps.setString(3, "autoregister@mail.com");
                ps.setString(4, DateUtil.getCurrentDate("yyyy-MM-dd"));
                ps.setString(5, "00-00-00-00-00-00");
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            System.out.println("Ex:" + ex);
        }
        return false;
    }
}
