package com.girlkun.models.boss.list_boss;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossData;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.SkillService;
import com.girlkun.utils.SkillUtil;
import com.girlkun.utils.Util;

public class NhanBan extends Boss {
    private Player playerAtt;

    public NhanBan(int bossID, BossData bossData, Zone zone, Player player) throws Exception {
        super(bossID, bossData);
        this.zone = zone;
        this.playerAtt = player;
    }

    @Override
    public void reward(Player plKill) {
        //vật phẩm rơi khi diệt boss nhân bản
        ItemMap it = new ItemMap(this.zone, Util.nextInt(1099, 1102), Util.nextInt(3, 4), this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                this.location.y - 24), plKill.id);
        Service.getInstance().dropItemMap(this.zone, it);
    }

    private long st;

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_PVP);
            PlayerService.gI().changeAndSendTypePK(this.playerAtt, ConstPlayer.PK_PVP);
        }
        if (this.playerAtt.session != null) {
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            if (playerAtt == this.playerAtt && Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.isTrue(5, 20)) {
                    if (SkillUtil.isUseSkillChuong(this)) {
                        this.moveTo(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 70));
                    } else {
                        this.moveTo(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 50));
                    }
                }
                SkillService.gI().useSkill(this, this.playerAtt, null, null);
            } else {
                if (Util.isTrue(1, 2)) {
                    this.moveToPlayer(playerAtt);
                }
            }
        }
        if (Util.canDoWithTime(st, 60000) || playerAtt.zone != this.zone || this.playerAtt.session == null || playerAtt.isDie()) {
            PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
            PlayerService.gI().changeAndSendTypePK(this.playerAtt, ConstPlayer.NON_PK);
            super.leaveMap();
            BossManager.gI().removeBoss(this);
            this.dispose();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}
