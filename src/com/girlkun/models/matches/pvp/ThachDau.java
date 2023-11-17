package com.girlkun.models.matches.pvp;

import com.girlkun.models.matches.PVP;
import com.girlkun.models.matches.TYPE_LOSE_PVP;
import com.girlkun.models.matches.TYPE_PVP;
import com.girlkun.models.player.Player;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;


public class ThachDau extends PVP {

    private int goldThachDau;
    private long goldReward;

    public ThachDau(Player p1, Player p2, int goldThachDau) {
        super(TYPE_PVP.THACH_DAU, p1, p2);
        this.goldThachDau = goldThachDau;
        this.goldReward = goldThachDau / 100 * 80;
    }

    @Override
    public void start() {
        this.p1.inventory.gold -= this.goldThachDau;
        this.p2.inventory.gold -= this.goldThachDau;
        Service.gI().sendMoney(this.p1);
        Service.gI().sendMoney(this.p2);
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
    }

    @Override
    public void reward(Player plWin) {
        plWin.inventory.gold += this.goldReward;
        Service.gI().sendMoney(plWin);
    }

    @Override
    public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
        if (typeLose == TYPE_LOSE_PVP.RUNS_AWAY) {
            Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ sợ quá bỏ chạy. Bạn thắng nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn bị xử thua vì bỏ chạy");
            (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldThachDau;
        } else if (typeLose == TYPE_LOSE_PVP.DEAD) {
            Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ kiệt sức. Bạn thắng nhận được " + Util.numberToMoney(this.goldReward) + " vàng");
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn bị xử thua vì kiệt sức");
            (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldThachDau;
        }
        Service.gI().sendMoney(p1.equals(plLose) ? p1 : p2);
    }

}

/**
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức Hãy tôn trọng tác giả
 * của mã nguồn này Xin cảm ơn! - Girlkun75
 */
