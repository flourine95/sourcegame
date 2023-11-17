package com.girlkun.models.map.challenge;

import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.MapService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;

/**
 * @author BTH sieu cap vippr0 
 */
public class MartialCongressService {

    private static MartialCongressService i;

    public static MartialCongressService gI() {
        if (i == null) {
            i = new MartialCongressService();
        }
        return i;
    }
//    public boolean check = false;

    public void startChallenge(Player player) {
//        check = true;
        Zone zone = getMapChalllenge(129);
        if (zone != null) {
            player.inventory.ruby -= 200;
            Service.gI().sendThongBao(player, "Bạn đã bị trừ 200 Hồng ngọc");
            PlayerService.gI().sendInfoHpMpMoney(player);
            ChangeMapService.gI().changeMap(player, zone, player.location.x, 360);
            Util.setTimeout(() -> {
                MartialCongress mc = new MartialCongress();
                mc.setPlayer(player);
                mc.setNpc(zone.getReferee());
                mc.toTheNextRound();
                MartialCongressManager.gI().add(mc);
                Service.gI().sendThongBao(player, "Số thứ tự của ngươi là 1\n chuẩn bị thi đấu nhé");
            }, 500);
        } else {
            Service.gI().sendThongBao(player, "Vui lòng thử lại !!!");
        }
    }

    public void moveFast(Player pl, int x, int y) {
        Message msg;
        try {
            msg = new Message(58);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeInt((int) pl.id);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
                System.out.println("loi ne tfy 1 ");
        }
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
                System.out.println("loi ne werwe 1 ");
        }
    }

    public Zone getMapChalllenge(int mapId) {
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        if (map.getNumOfBosses() < 1) {
            return map;
        }
        return null;
    }
}
