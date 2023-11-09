package com.girlkun.models.boss.list_boss.gas;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossData;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.models.skill.Skill;
import com.girlkun.services.EffectSkillService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

/**
 * @author BTH sieu cap vippr0
 */
public class DrLyChee extends Boss {
    private static final int[][] FULL_DEMON = new int[][]{{Skill.DEMON, 1}, {Skill.DEMON, 2}, {Skill.DEMON, 3}, {Skill.DEMON, 4}, {Skill.DEMON, 5}, {Skill.DEMON, 6}, {Skill.DEMON, 7}};
  private long lastTimeHapThu;
    private int timeHapThu;
       private long lastUpdate = System.currentTimeMillis();
    private long timeJoinMap;
    private int levell;
    private int initSuper = 0;
    protected Player playerAtt;
    private int timeLive = 200000000;
    public DrLyChee(Zone zone ,byte level, int dame, double hp,int id) throws Exception {
        super(id, new BossData(
                "Drabura Frost", //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{1309, 1310, 1311, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
               ((10000 * level)), //dame
                new double[]{((1000000000L * level))}, //hp
                new int[]{148}, //map join
                new int[][]{
                {Skill.DEMON, 3, 1}, {Skill.DEMON, 6, 2}, {Skill.DRAGON, 7, 3}, {Skill.DRAGON, 1, 4}, {Skill.GALICK, 5, 5},
                {Skill.KAMEJOKO, 7, 6}, {Skill.KAMEJOKO, 6, 7}, {Skill.KAMEJOKO, 5, 8}, {Skill.KAMEJOKO, 4, 9}, {Skill.KAMEJOKO, 3, 10}, {Skill.KAMEJOKO, 2, 11},{Skill.KAMEJOKO, 1, 12},
              {Skill.ANTOMIC, 1, 13},  {Skill.ANTOMIC, 2, 14},  {Skill.ANTOMIC, 3, 15},{Skill.ANTOMIC, 4, 16},  {Skill.ANTOMIC, 5, 17},{Skill.ANTOMIC, 6, 19},  {Skill.ANTOMIC, 7, 20},
                {Skill.MASENKO, 1, 21}, {Skill.MASENKO, 5, 22}, {Skill.MASENKO, 6, 23},
                    {Skill.KAMEJOKO, 7, 1000},},
                new String[]{}, //text chat 1
                new String[]{"|-1|Nhóc con"}, //text chat 2
                new String[]{}, //text chat 3
                60
        ));
        
        this.zone = zone;
        this.levell = level;
    }
    @Override
    public void reward(Player plKill) {
       if (Util.isTrue(100, 100)) {
            if (levell == 110)
            {
//                int a=0;
//                for (int i=0; i<20; i++)
//                {
                      ItemMap it = new ItemMap(this.zone, 1201, 20, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.getInstance().dropItemMap(this.zone, it);
//            a+=10;
//                }
            }
            else if (levell > 99 && levell < 110)
            {
//            int a=0;
//                for (int i=0; i<15; i++)
//                {
                      ItemMap it = new ItemMap(this.zone, 1201, 15, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.getInstance().dropItemMap(this.zone, it);
//            a+=10;
//                }
            }
            else if (levell < 100)
            {
//            int a=0;
//                for (int i=0; i<6; i++)
//                {
                      ItemMap it = new ItemMap(this.zone, 1201, 6, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.getInstance().dropItemMap(this.zone, it);
//            a+=10;
//                }
            }  
        }
    }
    @Override
    public void active() {
    super.active();
    }
    
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - plAtt.nPoint.tlchinhxac, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = (int) this.nPoint.subDameInjureWithDeff(damage/2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }
     
        @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}







