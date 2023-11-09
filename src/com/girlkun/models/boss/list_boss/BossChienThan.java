//package com.girlkun.models.boss.list_boss;
//
//import com.girlkun.models.boss.Boss;
//import com.girlkun.models.boss.BossID;
//import com.girlkun.models.boss.BossesData;
//import com.girlkun.models.map.ItemMap;
//import com.girlkun.models.player.Player;
//import com.girlkun.services.EffectSkillService;
//import com.girlkun.services.Service;
//import java.util.Random;
//import com.girlkun.utils.Util;
// hét chx
///**
// * @@Stole By Hoàng Việt
// */
//public class BossChienThan extends Boss {
//
//    public BossChienThan() throws Exception {
//        super(BossID.BOSS_CHIENTHAN, BossesData.BOSS_CHIENTHAN);
//    }
//
//    @Override
//    public void reward(Player plKill) {
//        plKill.inventory.event++;
//        Service.getInstance().sendThongBao(plKill, "Bạn đã nhận được 1 điểm săn Boss");
//        int[] DaTienMon = new int[]{1260, 1261, 1262};
//        int randomDA = new Random().nextInt(DaTienMon.length);
//            if (Util.isTrue(4, 5)) {
//                Service.getInstance().dropItemMap(this.zone, new ItemMap(zone, DaTienMon[randomDA], 1, this.location.x, this.location.y, plKill.id));
//            }
//    }
////    @Override
////    public void active() {
////        super.active(); //To change body of generated methods, choose Tools | Templates.
////        if (Util.canDoWithTime(st, 900000)) {
////            this.changeStatus(BossStatus.LEAVE_MAP);
////        }
////    }
//     
//    @Override
//    public void joinMap() {
//        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
//        st = System.currentTimeMillis();
//    }
//    private long st;
//     @Override
//    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
//        if (!this.isDie()) {
//            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1000)) {
//                this.chat("Xí hụt");
//                return 0;
//            }
//            damage = this.nPoint.subDameInjureWithDeff(damage/2);
//            if (!piercing && effectSkill.isShielding) {
//                if (damage > nPoint.hpMax) {
//                    EffectSkillService.gI().breakShield(this);
//                }
//                damage = 1;
//            }
//            this.nPoint.subHP(damage);
//            if (isDie()) {
//                this.setDie(plAtt);
//                die(plAtt);
//            }
//            return damage;
//        } else {
//            return 0;
//        }
//    }
//}
