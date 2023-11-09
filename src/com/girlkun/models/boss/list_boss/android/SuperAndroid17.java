//package com.girlkun.models.boss.list_boss.android;
//
//import com.girlkun.consts.ConstMob;
//import com.girlkun.models.boss.Boss;
//import com.girlkun.models.boss.BossID;
//import com.girlkun.models.boss.BossManager;
//import com.girlkun.models.boss.BossStatus;
//import com.girlkun.models.boss.BossesData;
//import com.girlkun.models.map.ItemMap;
//import com.girlkun.models.mob.Mob;
//import com.girlkun.models.player.Player;
//import com.girlkun.services.EffectSkillService;
//import com.girlkun.services.PetService;
//import com.girlkun.services.Service;
//import com.girlkun.services.TaskService;
//import com.girlkun.utils.Util;
//import java.util.Random;
//
//public class SuperAndroid17 extends Boss {
//
//    public SuperAndroid17() throws Exception {
//        super(BossID.SUPER_ANDROID_17, BossesData.SUPER_ANDROID_17);
//    }
//  @Override
//    public void reward(Player plKill) {
//        plKill.achievement.plusCount(3);
//        plKill.inventory.event++;
//        Service.getInstance().sendThongBao(plKill, "Bạn đã nhận được 1 điểm săn Boss");
//         int[] itemDos = new int[]{579};
//        int[] NRs = new int[]{17, 16, 579};
//        int randomDo = new Random().nextInt(itemDos.length);
//        int randomNR = new Random().nextInt(NRs.length);
//        if (Util.isTrue(15, 100)) {
//            if (Util.isTrue(1, 5)) {
//                Service.getInstance().dropItemMap(this.zone, Util.ratiItem(zone, 561, 1, this.location.x, this.location.y, plKill.id));
//                return;
//            }
//            Service.getInstance().dropItemMap(this.zone, Util.ratiItem(zone, itemDos[randomDo], 5, this.location.x, this.location.y, plKill.id));
//        }
//        else {
//            Service.getInstance().dropItemMap(this.zone, new ItemMap(zone, NRs[randomNR], 1, this.location.x, zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
//        }
//                ItemMap it1 = new ItemMap(this.zone, 2030, 1, this.location.x - 10, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24),  plKill.id);
//            Service.getInstance().dropItemMap(this.zone, it1);
//    }
//
//    @Override
//    public void active() {
//        super.active(); //To change body of generated methods, choose Tools | Templates.
//        if (Util.canDoWithTime(st, 1000000)) {
//            this.changeStatus(BossStatus.LEAVE_MAP);
//        }
//    }
//     
//    @Override
//    public void joinMap() {
//        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
//        st = System.currentTimeMillis();
//    }
//    private long st;
//    @Override
//    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
//        if (!this.isDie()) {
//            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1)) {
//                this.chat("Xí hụt");
//                return 0;
//            }
//            damage = this.nPoint.subDameInjureWithDeff(damage);
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
