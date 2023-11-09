package com.girlkun.models.player;

import com.girlkun.models.map.Zone;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.List;

/**
 * @author BTH sieu cap vippr0
 */
public class Referee1 extends Player {

    private long lastTimeChat;
    private Player playerTarget;

    private long lastTimeTargetPlayer;
    private long timeTargetPlayer = 5000;
    private long lastZoneSwitchTime;
    private long zoneSwitchInterval;
    private List<Zone> availableZones;

    public void initReferee1() {
        init();
    }

    @Override
    public short getHead() {
        return 678;
    }

    @Override
    public short getBody() {
        return 679;
    }

    @Override
    public short getLeg() {
        return 680;
    }

    public void joinMap(Zone z, Player player) {
        MapService.gI().goToMap(player, z);
        z.loadMeToAnother(player);
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeChat, 5000)) {
            Service.getInstance().chat(this, "Xin Chúc Mừng Top Đại Gia Sever Tabi");
            Service.getInstance().chat(this, "Danh Sách"
                                    + "\n|3|Mấy con gà!!!"
                                    );
            Service.getInstance().chat(this, "Chúc Các Cư Dân Online Vui Vẻ ");
            lastTimeChat = System.currentTimeMillis();
        }
    }

    private void init() {
//        int id = -1000000;
//        for (Map m : Manager.MAPS) {
//            if (m.mapId == 0) {
//                for (Zone z : m.zones) {
//                    Referee1 pl = new Referee1();
//                    pl.name = "SOLOMON";
//                    pl.gender = 0;
//                    pl.id = id++;
//                    pl.nPoint.hpMax = 69696969696969L;
//                    pl.nPoint.hpg = 69696969696969L;
//                    pl.nPoint.hp = 69696969696969L;
//                    pl.nPoint.setFullHpMp();
//                    pl.location.x = 714;
//                    pl.location.y = 432;
//                    joinMap(z, pl);
//                    z.setReferee(pl);
//                }
//            } else if (m.mapId == 7) {                      
//                    for (Zone z : m.zones) {
//                    Referee1 pl = new Referee1();
//                    pl.name = "SOLOMON";
//                    pl.gender = 0;
//                    pl.id = id++;
//                    pl.nPoint.hpMax = 69696969696969L;
//                    pl.nPoint.hpg = 69696969696969L;
//                    pl.nPoint.hp = 69696969696969L;
//                    pl.nPoint.setFullHpMp();
//                    pl.location.x = 204;
//                    pl.location.y = 432;
//                    joinMap(z, pl);
//                    z.setReferee(pl);
//                 }
//              } 
//            else if (m.mapId == 14) {                      
//                    for (Zone z : m.zones) {
//                    Referee1 pl = new Referee1();
//                    pl.name = "BOT";
//                    pl.gender = 0;
//                    pl.id = id++;
//                    pl.nPoint.hpMax = 69696969696969L;
//                    pl.nPoint.hpg = 69696969696969L;
//                    pl.nPoint.hp = 69696969696969L;
//                    pl.nPoint.setFullHpMp();
//                    pl.location.x = 752;
//                    pl.location.y = 408;
//                    joinMap(z, pl);
//                    z.setReferee(pl);
//                 }
//            }
//        }
    }
}

