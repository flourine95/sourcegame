package com.girlkun.models.boss.dhvt;

import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.BossesData;
import com.girlkun.models.player.Player;
import com.girlkun.services.EffectSkillService;
import com.girlkun.utils.Util;

/**
 * @author BTH sieu cap vippr0 
 */
public class ThienXinHang extends BossDHVT {

    private long lastTimePhanThan
            = System.currentTimeMillis();

    public ThienXinHang(Player player) throws Exception {
        super(BossID.THIEN_XIN_HANG, BossesData.THIEN_XIN_HANG);
        this.playerAtt = player;
//        phanThan();
    }

    @Override
    public void attack() {
        super.attack();
        try {
            EffectSkillService.gI().removeStun(this);
            if (Util.canDoWithTime(lastTimePhanThan, 20000)) {
                lastTimePhanThan = System.currentTimeMillis();
                phanThan();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
                System.out.println("loi ne    55    ClassCastException ");
        }
    }

    private void phanThan() {
        try {
            new ThienXinHangClone(BossID.THIEN_XIN_HANG_CLONE, playerAtt);
            new ThienXinHangClone(BossID.THIEN_XIN_HANG_CLONE1, playerAtt);
            new ThienXinHangClone(BossID.THIEN_XIN_HANG_CLONE2, playerAtt);
            new ThienXinHangClone(BossID.THIEN_XIN_HANG_CLONE3, playerAtt);
        } catch (Exception e) {
            System.out.println("aaaaa");
        }

    }
}