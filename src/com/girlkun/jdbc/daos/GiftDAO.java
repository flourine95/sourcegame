package com.girlkun.jdbc.daos;

import com.girlkun.models.player.Player;
import com.girlkun.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class GiftDAO {

    public static void createGiftPlayer(Connection con, int playerId) {
        try {
            PreparedStatement ps = con.prepareStatement("insert into gift (player_id) values (?)");
            ps.setInt(1, playerId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Logger.logException(GiftDAO.class, e);
                System.out.println("        loi 22");
        }
    }

    public static void loadGiftPlayer(Connection con, Player player) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             ps = con.prepareStatement("select * from gift where player_id = ?");
            ps.setInt(1, (int) player.id);
             rs = ps.executeQuery();
            if (rs.first()) {
                player.gift.goldTanThu = rs.getBoolean("gold_tan_thu");
                player.gift.gemTanThu = rs.getBoolean("gem_tan_thu");
            } else {
                createGiftPlayer(con, (int) player.id);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(GiftDAO.class, e);
                System.out.println("        loi 23");
        } finally{
            
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public static void saveGiftPlayer(Connection con, Player player) {
        try {
            PreparedStatement ps = con.prepareStatement("update gift set gold_tan_thu = ?, gem_tan_thu = ? where player_id = ?");
            ps.setBoolean(1, player.gift.goldTanThu);
            ps.setBoolean(2, player.gift.gemTanThu);
            ps.setInt(3, (int) player.id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            Logger.logException(GiftDAO.class, e);
                System.out.println("        loi 24");
        }
    }

}
