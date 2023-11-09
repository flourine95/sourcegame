
package com.girlkun.models.player;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.item.Item;
import com.girlkun.result.GirlkunResultSet;
import com.girlkun.server.Client;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.time.Instant;
import java.util.Date;

public class Giftcode {
    private static Giftcode instance;

    public static Giftcode gI() {
        if (instance == null) {
            instance = new Giftcode();
        }
        return instance;
    }

    public String checkInfomationGiftCode() {
        StringBuilder textGift = new StringBuilder("DANH SÁCH GIFTCODE \b\b");
        try {
            GirlkunResultSet rs = GirlkunDB.executeQuery("SELECT * FROM giftcode WHERE amount > 0");
            while (rs.next()) {
                String code = rs.getString("code");
                int amount = rs.getInt("amount");
                textGift.append("Code: ").append(code).append("\bCòn: ").append(amount).append(" Lượt nhập\b\b");
            }
            rs.dispose();
        } catch (Exception e) {
            Logger.logException(Giftcode.class, e, "Error checkInfomationGiftCode");
        }
        return textGift.toString();
    }

    public void giftCode(Player p, String code) throws Exception {
        GirlkunResultSet rs = GirlkunDB.executeQuery(
                "SELECT * FROM giftcode_history WHERE `player_id` = " + p.getSession().userId + " AND `code` = '"
                        + code + "';");
        if (rs.first()) {
            Service.gI().sendThongBaoOK(Client.gI().getPlayer(p.getSession().userId).getSession(),
                    "Bạn đã nhập code: " + code + "\nvào lúc: " + rs.getTimestamp("time"));
        } else {
            rs.dispose();
            rs = GirlkunDB.executeQuery("SELECT * FROM `giftcode` WHERE `code` = '"
                    + code + "';");
            if (rs.first()) {
                StringBuilder textGift = new StringBuilder("Bạn vừa nhận được:\b\b");
                int amount = rs.getInt("amount");
                if (amount < 1) {
                    Service.gI().sendThongBao(p, "Hết lượt nhập!");
                    return;
                }
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("Item"));
                if (InventoryServiceNew.gI().getCountEmptyBag(p) < jar.size()) {
                    Service.gI().sendThongBaoOK(p, "Cần trống " + jar.size() + " Ô hành trang");
                    return;
                }
                for (Object o : jar) {
                    JSONObject job = (JSONObject) o;
                    int idItem = Integer.parseInt(job.get("item").toString());
                    int quantity = Integer.parseInt(job.get("soluong").toString());
                    if (idItem == -1) {
                        p.inventory.gold = Math.min(p.inventory.gold + quantity, 2000000000L);
                        textGift.append(quantity).append(" vàng\b");
                    } else if (idItem == -2) {
                        p.inventory.gem = Math.min(p.inventory.gem + quantity, 200000000);
                        textGift.append(quantity).append(" ngọc xanh\b");
                    } else if (idItem == -3) {
                        p.inventory.ruby = Math.min(p.inventory.ruby + quantity, 200000000);
                        textGift.append(quantity).append(" hồng ngọc\b");
                    } else {
                        Item itemGiftTemplate = ItemService.gI().createNewItem((short) idItem);
                        if (itemGiftTemplate != null) {
                            Item itemGift = new Item((short) idItem);
                            JSONArray Op = (JSONArray) JSONValue.parse(job.get("Option").toString());
                            for (Object Option2 : Op) {
                                JSONObject job2 = (JSONObject) Option2;
                                itemGift.itemOptions
                                        .add(new Item.ItemOption(Integer.parseInt(job2.get("option").toString()),
                                                Integer.parseInt(job2.get("chiso").toString())));
                                job2.clear();
                            }
                            itemGift.quantity = quantity;
                            InventoryServiceNew.gI().addItemBag(p, itemGift);
                            textGift.append("x").append(quantity).append(" ").append(itemGift.template.name).append("\b");
                        }
                    }
                    Service.gI().sendMoney(p);
                    InventoryServiceNew.gI().sendItemBags(p);
                    job.clear();
                }
                jar.clear();
                Service.gI().sendThongBaoOK(p, textGift.toString());
                amount--;
                String sqlSET = "(" + p.getSession().userId + ", '" + code + "', '"
                        + Util.toDateString(Date.from(Instant.now()))
                        + "');";
                GirlkunDB.executeUpdate(
                        "INSERT INTO `giftcode_history` (`player_id`,`code`,`time`) VALUES " + sqlSET);
                GirlkunDB.executeUpdate("UPDATE `giftcode` SET `amount` = '" + amount + "' WHERE `Code` = '"
                        + code + "' LIMIT 1;");
            } else {
                Service.gI().sendThongBao(p, "Code không tồn tại!");
            }
            rs.dispose();
        }
    }
}
