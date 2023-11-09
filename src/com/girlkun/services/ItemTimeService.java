package com.girlkun.services;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.item.Item;
import com.girlkun.models.map.bdkb.BanDoKhoBau;
import com.girlkun.models.map.doanhtrai.DoanhTrai;
import com.girlkun.models.map.gas.Gas;
import com.girlkun.models.player.Fusion;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.utils.Logger;

import static com.girlkun.models.item.ItemTime.*;
import static com.girlkun.models.item.ItemTimeSieuCap.*;


public class ItemTimeService {

    private static ItemTimeService i;

    public static ItemTimeService gI() {
        if (i == null) {
            i = new ItemTimeService();
        }
        return i;
    }

    //gửi cho client
    public void sendAllItemTime(Player player) {
        sendTextDoanhTrai(player);
        sendTextGas(player);
        sendTextBanDoKhoBau(player);
        if (player.fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
            sendItemTime(player, player.gender == ConstPlayer.NAMEC ? 3901 : 3790,
                    (int) ((Fusion.TIME_FUSION - (System.currentTimeMillis() - player.fusion.lastTimeFusion)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet) {
            sendItemTime(player, 2755, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet)) / 1000));
        }
        if (player.itemTime.isUseBoKhi) {
            sendItemTime(player, 2756, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi)) / 1000));
        }
        if (player.itemTime.isUseGiapXen) {
            sendItemTime(player, 2757, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen)) / 1000));
        }
        if (player.itemTime.isUseCuongNo) {
            sendItemTime(player, 2754, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo)) / 1000));
        }
        
        if (player.itemTime.isUseAnDanh) {
            sendItemTime(player, 2760, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh)) / 1000));
        }
        if (player.itemTime.isBiNgo) {
            sendItemTime(player, 5138, (int) ((TIME_BI_NGO - (System.currentTimeMillis() - player.itemTime.lastTimeBiNgo)) / 1000));
        }
        if (player.itemTime.isUseMayDo) {
            sendItemTime(player, 2758, (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo)) / 1000));
        }
        if (player.itemTime.isUseMayDo2) {
            sendItemTime(player, 11274, (int) ((TIME_MAY_DO2 - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo2)) / 1000));
        }
        if (player.itemTime.isDuoikhi) {
            sendItemTime(player, player.itemTime.iconDuoi, (int) ((TIME_DUOI_KHI - (System.currentTimeMillis() - player.itemTime.lastTimeDuoikhi)) / 1000));
        }
        if (player.itemTime.isUseTDLT) {
            sendItemTime(player, 4387,player.itemTime.timeTDLT / 1000);
        }
        if (player.itemTimesieucap.isUseBoHuyet3) {
            sendItemTime(player, 10714, (int) ((TIME_ITEM3 - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeBoHuyet3)) / 1000));
        }
        if (player.itemTimesieucap.isUseBoKhi3) {
            sendItemTime(player, 10715, (int) ((TIME_ITEM3 - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeBoKhi3)) / 1000));
        }
        if (player.itemTimesieucap.isUseGiapXen3) {
            sendItemTime(player, 10712, (int) ((TIME_ITEM3 - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeGiapXen3)) / 1000));
        }
        if (player.itemTimesieucap.isUseCuongNo3) {
            sendItemTime(player, 10716, (int) ((TIME_ITEM3 - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeCuongNo3)) / 1000));
        }
        
        if (player.itemTimesieucap.isUseAnDanh3) {
            sendItemTime(player, 10717, (int) ((TIME_ITEM3 - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeAnDanh3)) / 1000));
        }
        if (player.itemTimesieucap.isKeo) {
            sendItemTime(player, 8243, (int) ((TIME_KEO - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeKeo)) / 1000));
        }
        if (player.itemTimesieucap.isUseXiMuoi) {
            sendItemTime(player, 10904, (int) ((TIME_XI_MUOI - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeUseXiMuoi)) / 1000));
        }
        if (player.itemTimesieucap.isUseTrungThu) {
            sendItemTime(player, player.itemTimesieucap.iconBanh, (int) ((TIME_TRUNGTHU - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeUseBanh)) / 1000));
        }
        if (player.itemTimesieucap.isEatMeal) {
            sendItemTime(player, player.itemTimesieucap.iconMeal, (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTimesieucap.lastTimeMeal)) / 1000));
        }
    }

    //bật tđlt
    public void turnOnTDLT(Player player, Item item) {
        int min = 0;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                min = io.param;
                io.param = 0;
                break;
            }
        }
        player.itemTime.isUseTDLT = true;
        player.itemTime.timeTDLT = min * 60 * 1000;
        player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
        sendCanAutoPlay(player);
        sendItemTime(player, 4387, player.itemTime.timeTDLT / 1000);
        InventoryServiceNew.gI().sendItemBags(player);
    }

    //tắt tđlt
    public void turnOffTDLT(Player player, Item item) {
        player.itemTime.isUseTDLT = false;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                io.param += (short) ((player.itemTime.timeTDLT - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT)) / 60 / 1000);
                break;
            }
        }
        sendCanAutoPlay(player);
        removeItemTime(player, 4387);
        InventoryServiceNew.gI().sendItemBags(player);
    }

    public void sendCanAutoPlay(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(player.itemTime.isUseTDLT ? 1 : 0);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ItemTimeService.class, e);
        }
    }

    public void sendTextDoanhTrai(Player player) {
        if (player.clan != null && !player.clan.doanhTrai_haveGone
                && player.clan.timeOpenDoanhTrai != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenDoanhTrai) / 1000);
            int secondsLeft = (DoanhTrai.TIME_DOANH_TRAI / 1000) - secondPassed;
            sendTextTime(player, DOANH_TRAI, "Doanh trại độc nhãn", secondsLeft);
            if (secondsLeft <=0 || secondsLeft > 1800){
                removeTextDoanhTrai(player);
            }
        }
    }
    
    public void sendTextGas(Player player) {
        if (player.clan != null
                && player.clan.timeOpenKhiGas != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenKhiGas) / 1000);
            int secondsLeft = (Gas.TIME_KHI_GAS / 1000) - secondPassed;
            sendTextTime(player, KHI_GASS, "Khí Gas Hủy Diệt: ", secondsLeft);
            if (secondsLeft <=0 || secondsLeft > 1800){
                removeTextKhiGas(player);
            }
        }
    }
    
    public void sendTextBanDoKhoBau(Player player) {
        if (player.clan != null
                && player.clan.timeOpenbdkb != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenbdkb) / 1000);
            int secondsLeft = (BanDoKhoBau.TIME_KHI_BAN_DO_KHO_BAU / 1000) - secondPassed;
            sendTextTime(player, BAN_DO_KHO_BAU, "Bản đồ kho báu: ", secondsLeft);
            if (secondsLeft <=0 || secondsLeft > 1800){
                removeTextbdkb(player);
            }
        }
    }

    public void removeTextDoanhTrai(Player player) {
        removeTextTime(player, DOANH_TRAI);
    }

    public void removeTextTime(Player player, byte id) {
        sendTextTime(player, id, "", 0);
    }
       public void removeTextKhiGas(Player player) {
        removeTextTime(player, KHI_GASS );
    }
       public void removeTextbdkb(Player player) {
        removeTextTime(player, BAN_DO_KHO_BAU );
    }

     public void sendTextTime(Player player, byte id, String text, int seconds) {
        Message msg;
        try {
            msg = new Message(65);
            msg.writer().writeByte(id);
            msg.writer().writeUTF(text);
            msg.writer().writeShort(seconds);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    public void sendItemTime(Player player, int itemId, int time) {
        Message msg;
        try {
            msg = new Message(-106);
            msg.writer().writeShort(itemId);
            msg.writer().writeShort(time);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void removeItemTime(Player player, int itemTime) {
        sendItemTime(player, itemTime, 0);
    }

}
