package Client.inventory;

import Database.DatabaseLoader.DatabaseConnectionEx;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class MapleInventoryIdentifier implements Serializable {

    private static final long serialVersionUID = 21830921831301L;
    private static final MapleInventoryIdentifier instance = new MapleInventoryIdentifier();
    private final AtomicInteger runningUID = new AtomicInteger(0);

    public static int getInstance() {
        return instance.getNextUniqueId();
    }

    public int getNextUniqueId() {
        if (runningUID.get() <= 0) {
            runningUID.set(initUID());
        } else {
            runningUID.set(runningUID.get() + 1);
        }
        return runningUID.get();
    }

    public int initUID() {
        int ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int[] ids = new int[5];
            PreparedStatement ps = con.prepareStatement("SELECT MAX(sn) FROM inventoryitems WHERE sn > 0");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ids[0] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(petid) FROM pets");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[1] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(ringid) FROM rings");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[2] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(partnerringid) FROM rings");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[3] = rs.getInt(1) + 1; //biggest pl0x. but if this happens -> o_O
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(uniqueid) FROM androids");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[4] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            for (int id : ids) {
                if (id > ret) {
                    ret = id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
