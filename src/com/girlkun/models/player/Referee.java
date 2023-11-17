package com.girlkun.models.player;

import com.girlkun.models.map.Map;
import com.girlkun.models.map.Zone;
import com.girlkun.server.Manager;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.List;

/**
 * @author BTH sieu cap vippr0
 */
public class Referee extends Player {

    private long lastTimeChat;
    private Player playerTarget;

    private long lastTimeTargetPlayer;
    private long timeTargetPlayer = 5000;
    private long lastZoneSwitchTime;
    private long zoneSwitchInterval;
    private List<Zone> availableZones;

    public void initReferee() {
        init();
    }

    @Override
    public short getHead() {
        return 956;
    }

    @Override
    public short getBody() {
        return 957;
    }

    @Override
    public short getLeg() {
        return 958;
    }

    public void joinMap(Zone z, Player player) {
        MapService.gI().goToMap(player, z);
        z.loadMeToAnother(player);
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeChat, 5000)) {
            Service.gI().chat(this, "Đại Hội Võ Thuật lần thứ 23 đã chính thức khai mạc");
            Service.gI().chat(this, "Còn chờ gì nữa mà không đăng kí tham gia để nhận nhiều phẩn quà hấp dẫn");
            Service.gI().chat(this, "Nếu boss của bạn bay đi mất !!\b|7|Đổi vô lại khu: "+ this.zone.zoneId);
            lastTimeChat = System.currentTimeMillis();
        }
    }

    private void init() {
        int id = -1000000;
        for (Map m : Manager.MAPS) {
            if (m.mapId == 52) {
                for (Zone z : m.zones) {
                    Referee pl = new Referee();
                    pl.name = "TABI";
                    pl.gender = 0;
                    pl.id = id++;
                    pl.nPoint.hpMax = 69696969696969L;
                    pl.nPoint.hpg = 69696969696969L;
                    pl.nPoint.hp = 69696969696969L;
                    pl.nPoint.setFullHpMp();
                    pl.location.x = 387;
                    pl.location.y = 336;
                    joinMap(z, pl);
                    z.setReferee(pl);
                }
            } else if (m.mapId == 129) {
                for (Zone z : m.zones) {
                    Referee pl = new Referee();
                    pl.name = "TABI";
                    pl.gender = 0;
                    pl.id = id++;
                    pl.nPoint.hpMax = 69696969696969L;
                    pl.nPoint.hpg = 69696969696969L;
                    pl.nPoint.hp = 69696969696969L;
                    pl.nPoint.setFullHpMp();
                    pl.location.x = 385;
                    pl.location.y = 264;
                    joinMap(z, pl);
                    z.setReferee(pl);
                 }         
            }
//            else if (m.mapId == 14) {
//                for (Zone z : m.zones) {
//                    Referee pl = new Referee();
//                    pl.name = "Tabi";
//                    pl.gender = 0;
//                    pl.id = id++;
//                    pl.nPoint.hpMax = 69;
//                    pl.nPoint.hpg = 69;
//                    pl.nPoint.hp = 69;
//                    pl.nPoint.setFullHpMp();
//                    pl.location.x = 1546;
//                    pl.location.y = 408;
//                    joinMap(z, pl);
//                    z.setReferee(pl);
//                }
//            }
        }
    }
}

