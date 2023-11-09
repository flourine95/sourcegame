///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.girlkun.models.boss.list_boss;
//
//import com.girlkun.models.boss.Boss;
//import com.girlkun.models.boss.BossID;
//import com.girlkun.models.boss.BossStatus;
//import com.girlkun.models.boss.BossesData;
//import com.girlkun.models.map.ItemMap;
//import com.girlkun.models.player.Player;
//import com.girlkun.services.EffectSkillService;
//import com.girlkun.services.Service;
//import com.girlkun.utils.Util;
//
///**
// * @@Stole By Hoàng Việt
// */
//public class Mabu extends Boss {
//
//    public Mabu() throws Exception {
//        super(BossID.MABU, BossesData.);
//    }
//
//    @Override
//    public void reward(Player plKill) {
//        ItemMap it = new ItemMap(this.zone, 568, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
//                this.location.y - 24), plKill.id);
//        Service.getInstance().dropItemMap(this.zone, it);
//    }
//    @Override
//    public void active() {
//        super.active(); //To change body of generated methods, choose Tools | Templates.
//        if (Util.canDoWithTime(st, 900000)) {
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
//     @Override
//    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
//        if (!this.isDie()) {
//            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1000)) {
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
