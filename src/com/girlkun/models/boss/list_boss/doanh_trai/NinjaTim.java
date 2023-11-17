package com.girlkun.models.boss.list_boss.doanh_trai;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.boss.*;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.models.skill.Skill;
import com.girlkun.services.EffectSkillService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

public class NinjaTim extends Boss {

    private static final int[][] FULL_DEMON = new int[][]{{Skill.DEMON, 1}, {Skill.DEMON, 2}, {Skill.DEMON, 3}, {Skill.DEMON, 4}, {Skill.DEMON, 5}, {Skill.DEMON, 6}, {Skill.DEMON, 7}};
    private long lastTimeHapThu;
    private int timeHapThu;
    private int initSuper = 0;
    protected Player playerAtt;
    private int timeLive = 10;
    private boolean calledNinja;

    public NinjaTim(Zone zone, double dame, double hp) throws Exception {
        super(BossID.NINJA_AO_TIM, new BossData(
                "Ninja Áo Tím", //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{123, 124, 125, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                ((hp / 5)), //dame
                new double[]{((dame * 350))}, //hp
                new int[]{57}, //map join
                new int[][]{
                    {Skill.DEMON, 3, 1}, {Skill.DEMON, 6, 2}, {Skill.DRAGON, 7, 3}, {Skill.DRAGON, 1, 4}, {Skill.GALICK, 5, 5},
                    {Skill.KAMEJOKO, 7, 6}, {Skill.KAMEJOKO, 6, 7}, {Skill.KAMEJOKO, 5, 8}, {Skill.KAMEJOKO, 4, 9}, {Skill.KAMEJOKO, 3, 10}, {Skill.KAMEJOKO, 2, 11}, {Skill.KAMEJOKO, 1, 12},
                    {Skill.ANTOMIC, 1, 13}, {Skill.ANTOMIC, 2, 14}, {Skill.ANTOMIC, 3, 15}, {Skill.ANTOMIC, 4, 16}, {Skill.ANTOMIC, 5, 17}, {Skill.ANTOMIC, 6, 19}, {Skill.ANTOMIC, 7, 20},
                    {Skill.MASENKO, 1, 21}, {Skill.MASENKO, 5, 22}, {Skill.MASENKO, 6, 23},
                    {Skill.KAMEJOKO, 7, 1000},},
                new String[]{}, //text chat 1
                new String[]{"|-1|Nhóc con"}, //text chat 2
                new String[]{}, //text chat 3
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(80, 100)) {
            ItemMap it = new ItemMap(this.zone, 15, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        } else {
            ItemMap it = new ItemMap(this.zone, 14, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        if (Util.canDoWithTime(st, 1800000) || this.isDie() == true) {
            this.changeStatus(BossStatus.LEAVE_MAP);
            BossManager.gI().removeBoss(this);
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        this.calledNinja = false;
        st = System.currentTimeMillis();
    }
    private long st;

    @Override
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
            }
            this.nPoint.subHP(damage);
            if (this.calledNinja == false && this.zone.map.mapId == 54) {
                if (this.nPoint.hp <= 150000000) {
                    try {
                        System.out.println("  this zone  " + this.zone.map.mapId);
                        new NinjaClone(this.zone, 2, Util.nextInt(1000, 10000), BossID.NINJA_AO_TIM1);
                        new NinjaClone(this.zone, 2, Util.nextInt(1000, 10000), BossID.NINJA_AO_TIM2);
                        new NinjaClone(this.zone, 2, Util.nextInt(1000, 10000), BossID.NINJA_AO_TIM3);
                        new NinjaClone(this.zone, 2, Util.nextInt(1000, 10000), BossID.NINJA_AO_TIM4);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    this.calledNinja = true;
                }
            }
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }

}
