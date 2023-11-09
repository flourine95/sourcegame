package com.girlkun.jdbc.daos;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.item.Item;
import com.girlkun.models.player.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class HistoryTransactionDAO {

    public static void insert(Player pl1, Player pl2,
                              int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
                              List<Item> bag1Before, List<Item> bag2Before,
                              List<Item> bag1After,
                              List<Item> bag2After,
                              long gold1Before, long gold2Before, long gold1After, long gold2After) {

        String player1 = pl1.name + " (" + pl1.id + ")";
        String player2 = pl2.name + " (" + pl2.id + ")";
        String itemPlayer1 = "Gold: " + goldP1 + ", ";
        String itemPlayer2 = "Gold: " + goldP2 + ", ";
        List<Item> doGD1 = new ArrayList<>();
        List<Item> doGD2 = new ArrayList<>();
        for (Item item : itemP1) {
            if (item.isNotNullItem() && doGD1.stream().noneMatch(item1 -> item1.template.id == item.template.id)) {
                doGD1.add(item);
            } else if (item.isNotNullItem()) {
                doGD1.stream().filter(item1 -> item1.template.id == item.template.id).findFirst().get().quantityGD += item.quantityGD;
            }
        }
        for (Item item : itemP2) {
            if (item.isNotNullItem() && doGD2.stream().noneMatch(item1 -> item1.template.id == item.template.id)) {
                doGD2.add(item);
            } else if (item.isNotNullItem()) {
                doGD2.stream().filter(item1 -> item1.template.id == item.template.id).findFirst().get().quantityGD += item.quantityGD;
            }
        }


        for (Item item : doGD1) {
            if (item.isNotNullItem()) {
                itemPlayer1 += item.template.name + " (x" + item.quantityGD + "),";
//                System.out.println(item.template.name + " (x" + item.quantityGD + "),");
            }
        }
        for (Item item : doGD2) {
            if (item.isNotNullItem()) {
                itemPlayer2 += item.template.name + " (x" + item.quantityGD + "),";
//                System.out.println(item.template.name + " (x" + item.quantityGD + "),");
            }
        }
        String beforeTran1 = "";
        String beforeTran2 = "";
        for (Item item : bag1Before) {
            if (item.isNotNullItem()) {
                beforeTran1 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        for (Item item : bag2Before) {
            if (item.isNotNullItem()) {
                beforeTran2 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        String afterTran1 = "";
        String afterTran2 = "";
        for (Item item : bag1After) {
            if (item.isNotNullItem()) {
                afterTran1 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        for (Item item : bag2After) {
            if (item.isNotNullItem()) {
                afterTran2 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        try {
            GirlkunDB.executeUpdate("insert into history_transaction values()", player1, player2,
                    itemPlayer1, itemPlayer2, beforeTran1, beforeTran2, afterTran1, afterTran2, new Timestamp(System.currentTimeMillis()));
        } catch (Exception ex) {
            ex.printStackTrace();
                System.out.println("loi ne  11      ClassCastException ");
        }
    }

    public static void deleteHistory() {
//        PreparedStatement ps = null;
//        try (Connection con = DBHika.getConnection();) {
//             ps = con.prepareStatement("delete from history_transaction where time_tran < '"
//                    + TimeUtil.getTimeBeforeCurrent(3 * 24 * 60 * 60 * 1000, "yyyy-MM-dd") + "'");
//            ps.executeUpdate();
//            ps.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally{
//            try {
//                ps.close();
//            } catch (SQLException ex) {
//                java.util.logging.Logger.getLogger(HistoryTransactionDAO.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

}
