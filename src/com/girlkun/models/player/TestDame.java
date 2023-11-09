package com.girlkun.models.player;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.map.Map;
import com.girlkun.models.map.Zone;
import com.girlkun.server.Manager;
import com.girlkun.services.MapService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;

import java.util.List;

/**
 * @author BTH sieu cap vippr0
 */
public class TestDame extends Player {

    private long lastTimeChat;
//    protected Player playerTarger;

    private long lastTimeTargetPlayer;
    private long timeTargetPlayer = 5000;
    private long lastZoneSwitchTime;
    private long zoneSwitchInterval;
    private List<Zone> availableZones;

    public void initTestDame() {
        init();
    }

    @Override
    public short getHead() {
        return 1219;
    }

    @Override
    public short getBody() {
        return 1220;
    }

    @Override
    public short getLeg() {
        return 1221;
    }

    public void joinMap(Zone z, Player player) {
        MapService.gI().goToMap(player, z);
        z.loadMeToAnother(player);
    }
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
    }

    protected long lastTimeAttack;
    

    @Override
    public void update() {
        active();
        if(this.isDie()){
            Service.getInstance().sendMoney(this);
            PlayerService.gI().hoiSinh(this);
            Service.getInstance().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMp(this);
        }
    }

    private void init() {
        int id = -1000000;
        for (Map m : Manager.MAPS) {
            if (m.mapId == 170) {
                for (Zone z : m.zones) {
                    TestDame pl = new TestDame();
                    pl.name = "TEST DAME";
                    pl.gender = 0;
                    pl.id = id++;
                    pl.nPoint.hpMax = 100000000000000000000000000000000000000D;
                    pl.nPoint.hpg = 100000000000L;
                    pl.nPoint.hp = 100000000000000000000000000000000000000D;
                    pl.nPoint.setFullHpMp();
                    pl.location.x = 360;
                    pl.location.y = 336;
                    joinMap(z, pl);
                    z.setReferee(pl);
                }
            }
//            else if (m.mapId == 7) {                      
//                    for (Zone z : m.zones) {
//                    Referee1 pl = new Referee1();
//                    pl.name = "TEST DAME";
//                    pl.gender = 0;
//                    pl.id = id++;
//                    pl.nPoint.hpMax = 50000000000L;
//                    pl.nPoint.hpg = 50000000000L;
//                    pl.nPoint.hp = 50000000000L;
//                    pl.nPoint.setFullHpMp();
//                    pl.location.x = 204;
//                    pl.location.y = 432;
//                    joinMap(z, pl);
//                    z.setReferee(pl);
//                 }
//              } 
        }
    }
}
