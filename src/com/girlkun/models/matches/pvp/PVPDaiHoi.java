/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girlkun.models.matches.pvp;

import com.girlkun.models.matches.PVP;
import com.girlkun.models.matches.TYPE_LOSE_PVP;
import com.girlkun.models.matches.TYPE_PVP;
import com.girlkun.models.player.Player;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;

/**
 *
 * @author Administrator
 */
public class PVPDaiHoi extends PVP{
    private final int goldThachDau;
    private final long goldReward;
    private final DaiHoiVoThuat dh;
    private long lastTimePVP;

    public PVPDaiHoi(Player p1, Player p2, int goldThachDau , DaiHoiVoThuat d , long l) {
        super(TYPE_PVP.DAI_HOI_VO_THUAT, p1, p2);
        this.goldThachDau = goldThachDau;
        this.goldReward = goldThachDau;
        dh = d;
        lastTimePVP = l;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void finish() {

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void update() {
        if(Util.canDoWithTime(lastTimePVP, 180000)){
            if(p1.nPoint.hp > p2.nPoint.hp){
                ChangeMapService.gI().changeMapBySpaceShip(p2,21 +p2.gender, 0, -1);
                p1.pvp.lose(p2, TYPE_LOSE_PVP.DEAD);
            }else if(p2.nPoint.hp > p1.nPoint.hp){
                ChangeMapService.gI().changeMapBySpaceShip(p1,21 + p1.gender, 0, -1);
                p2.pvp.lose(p1, TYPE_LOSE_PVP.DEAD);
            }else{
                Service.gI().sendThongBao(p1, "Trận đấu hòa. Bạn nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
                p1.inventory.gold += this.goldReward / 2;
                Service.gI().sendMoney(p1);
                dh.listReg.add(p1);
                p1.lastTimeWin = System.currentTimeMillis();
                p1.isWin = true;
                
                Service.gI().sendThongBao(p2, "Trận đấu hòa. Bạn nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
                p2.inventory.gold += this.goldReward / 2;
                Service.gI().sendMoney(p2);
                dh.listReg.add(p2);
                p2.lastTimeWin = System.currentTimeMillis();
                p2.isWin = true;
            }
        }
    }

    @Override
    public void reward(Player plWin) {
        plWin.inventory.gold += this.goldReward;
        Service.gI().sendMoney(plWin);
        dh.listReg.add(plWin);
        plWin.lastTimeWin = System.currentTimeMillis();
        plWin.isWin = true;
    }

    @Override
    public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
        if (typeLose == TYPE_LOSE_PVP.RUNS_AWAY) {
            Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ kiệt sức. Bạn thắng nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn bị xử thua vì kiệt sức");
            (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldReward;
            dh.listReg.remove(plLose);
        } else if (typeLose == TYPE_LOSE_PVP.DEAD) {
            Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ kiệt sức. Bạn thắng nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn bị xử thua vì kiệt sức");
            (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldReward;
            dh.listReg.remove(plLose);
        }
        Service.gI().sendMoney(p1.equals(plLose) ? p1 : p2);
    }
}
