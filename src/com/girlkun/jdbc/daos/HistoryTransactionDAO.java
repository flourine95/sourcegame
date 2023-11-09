package com.girlkun.jdbc.daos;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.item.Item;
import com.girlkun.models.player.Player;
import com.girlkun.utils.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistoryTransactionDAO {
    public static void insert(Player pl1, Player pl2, int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2, List<Item> bag1Before, List<Item> bag2Before, List<Item> bag1After, List<Item> bag2After) {
        String player1 = pl1.name + " (" + pl1.id + ")";
        String player2 = pl2.name + " (" + pl2.id + ")";
        StringBuilder itemPlayer1 = new StringBuilder("Gold: " + goldP1 + ", ");
        StringBuilder itemPlayer2 = new StringBuilder("Gold: " + goldP2 + ", ");
        List<Item> doGD1 = new ArrayList<>();
        List<Item> doGD2 = new ArrayList<>();
        addItemGD(itemP1, doGD1);
        addItemGD(itemP2, doGD2);

        for (Item item : doGD1) {
            if (item.isNotNullItem()) {
                itemPlayer1.append(item.template.name).append(" (x").append(item.quantityGD).append("),");
            }
        }
        for (Item item : doGD2) {
            if (item.isNotNullItem()) {
                itemPlayer2.append(item.template.name).append(" (x").append(item.quantityGD).append("),");
            }
        }
        StringBuilder beforeTran1 = new StringBuilder();
        StringBuilder beforeTran2 = new StringBuilder();
        for (Item item : bag1Before) {
            if (item.isNotNullItem()) {
                beforeTran1.append(item.template.name).append(" (x").append(item.quantity).append("),");
            }
        }
        for (Item item : bag2Before) {
            if (item.isNotNullItem()) {
                beforeTran2.append(item.template.name).append(" (x").append(item.quantity).append("),");
            }
        }
        StringBuilder afterTran1 = new StringBuilder();
        StringBuilder afterTran2 = new StringBuilder();
        for (Item item : bag1After) {
            if (item.isNotNullItem()) {
                afterTran1.append(item.template.name).append(" (x").append(item.quantity).append("),");
            }
        }
        for (Item item : bag2After) {
            if (item.isNotNullItem()) {
                afterTran2.append(item.template.name).append(" (x").append(item.quantity).append("),");
            }
        }
        try {
            GirlkunDB.executeUpdate("insert into history_transaction values()", player1, player2, itemPlayer1.toString(), itemPlayer2.toString(), beforeTran1.toString(), beforeTran2.toString(), afterTran1.toString(), afterTran2.toString(), new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            Logger.logException(HistoryTransactionDAO.class, e);
        }
    }

    private static void addItemGD(List<Item> itemP1, List<Item> doGD1) {
        for (Item item : itemP1) {
            if (item.isNotNullItem() && doGD1.stream().noneMatch(item1 -> item1.template.id == item.template.id)) {
                doGD1.add(item);
            } else if (item.isNotNullItem()) {
                doGD1.stream().filter(item1 -> item1.template.id == item.template.id).findFirst().get().quantityGD += item.quantityGD;
            }
        }
    }

}
