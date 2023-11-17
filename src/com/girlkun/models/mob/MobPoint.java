package com.girlkun.models.mob;

import com.girlkun.utils.Util;


public class MobPoint {

    public final Mob mob;
    public double hp;
    public double maxHp;
    public double dame;
    
    public MobPoint(Mob mob){
        this.mob = mob;
    }

    public double getHpFull() {
        return maxHp;
    }

    public void setHpFull(double hp) {
        maxHp = hp;
    }

    public double gethp() {
        return hp;
    }

    public void sethp(double hp) {
        if (this.hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public double getDameAttack() {
        return this.dame != 0 ? this.dame + Util.limitNextDame(-(this.dame / 100d), (this.dame / 100d))
                : this.getHpFull() * Util.limitNextDame(mob.pDame - 1, mob.pDame + 1) / 100d
                + Util.limitNextDame(-(mob.level * 10d), mob.level * 10d);
    }
}
