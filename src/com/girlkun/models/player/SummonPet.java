package com.girlkun.models.player;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.skill.Skill;
import com.girlkun.server.ServerNotify;
import com.girlkun.services.MapService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.SkillService;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;
import lombok.Getter;

public class SummonPet extends Player {
    public static final byte FOLLOW = 0;
    public static final byte ATTACK_PLAYER = 1;
    public static final byte ATTACK_MOB = 2;
    public static final byte GOHOME = 3;
    private static final short ARANGE_CAN_ATTACK = 1500;
    public Player master;
    private boolean goingHome;
    private long lastTimeTargetPlayer;
    private int timeTargetPlayer;
    private long lastTimeMoveAtHome;
    private byte directAtHome = -1;
    private long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;
    public long lastTimeRevival;

    private Mob mobAttack;
    private Player playerAttack;

    @Getter
    public byte status = 0;

    public SummonPet(Player masterr) {
        this.master = masterr;
        this.isTrieuHoiPet = true;
        this.id = -masterr.id * 100000;
    }

    @Override
    public short getHead() {
        if (master.TrieuHoiCapBac >= 0 && master.TrieuHoiCapBac <= 3) {
            return 1351;
        } else if (master.TrieuHoiCapBac >= 4 && master.TrieuHoiCapBac <= 6) {
            return 1357;
        } else {
            return 1354;
        }
    }

    @Override
    public short getBody() {
        if (master.TrieuHoiCapBac >= 0 && master.TrieuHoiCapBac <= 3) {
            return 1352;
        } else if (master.TrieuHoiCapBac >= 4 && master.TrieuHoiCapBac <= 6) {
            return 1358;
        } else {
            return 1355;
        }
    }

    @Override
    public short getLeg() {
        if (master.TrieuHoiCapBac >= 0 && master.TrieuHoiCapBac <= 3) {
            return 1353;
        } else if (master.TrieuHoiCapBac >= 4 && master.TrieuHoiCapBac <= 6) {
            return 1359;
        } else {
            return 1356;
        }
    }

    @Override
    public byte getAura() {
        if (master.TrieuHoiCapBac >= 0 && master.TrieuHoiCapBac <= 3) {
            return 54;
        } else if (master.TrieuHoiCapBac >= 4 && master.TrieuHoiCapBac <= 6) {
            return 55;
        } else if (master.TrieuHoiCapBac >= 7 && master.TrieuHoiCapBac < 10) {
            return 56;
        } else {
            return 22;
        }
    }


    public void changeStatus(byte status) {
        if (goingHome || this.isDie()) {
            Service.gI().sendThongBao(master, "Không thể thực hiện");
            return;
        }
        if (status == GOHOME) {
            goHome();
        }
        this.status = status;
    }

    public void goHome() {
        if (this.status == GOHOME) {
            return;
        }
        goingHome = true;
        ChangeMapService.gI().goToMap(this, MapService.gI().getMapCanJoin(this, master.gender + 21, -1));
        this.zone.loadMeToAnother(this);
        SummonPet.this.status = SummonPet.GOHOME;
        goingHome = false;
    }

    private Mob findMobAttack() {
        int dis = ARANGE_CAN_ATTACK;
        Mob mobAtt = null;
        for (Mob mob : zone.mobs) {
            if (mob.isDie()) {
                continue;
            }
            int d = Util.getDistance(this, mob);
            if (d <= dis) {
                dis = d;
                mobAtt = mob;
            }
        }
        return mobAtt;
    }

    public Player getPlayerAttack() {
        if (master.TrieuHoiPlayerAttack != null) {
            if (master.TrieuHoiPlayerAttack.isDie() || !this.zone.equals(master.TrieuHoiPlayerAttack.zone)) {
                master.TrieuHoiPlayerAttack = null;
                if (this.playerAttack != null && (this.playerAttack.isDie() || !this.zone.equals(this.playerAttack.zone) || (this.playerAttack.pvp == null || this.pvp == null) || (this.playerAttack.typePk != ConstPlayer.PK_ALL || this.typePk != ConstPlayer.PK_ALL) || (!this.pvp.isInPVP(this.playerAttack) || !this.playerAttack.pvp.isInPVP(this)) || ((this.playerAttack.cFlag == 0 && this.cFlag == 0) && (this.playerAttack.cFlag != 8 || this.cFlag == this.playerAttack.cFlag))) || this.playerAttack == this || this.playerAttack == master) {
                    this.playerAttack = null;
                }
                if (this.zone != null && this.playerAttack == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
                    this.playerAttack = this.zone.playerPKInMap();
                    this.lastTimeTargetPlayer = System.currentTimeMillis();
                    this.timeTargetPlayer = Util.nextInt(40000, 45000);
                }
                return this.playerAttack;
            }
            return master.TrieuHoiPlayerAttack;
        } else {
            if (this.playerAttack != null && (this.playerAttack.isDie() || !this.zone.equals(this.playerAttack.zone) || (this.playerAttack.pvp == null || this.pvp == null) || (this.playerAttack.typePk != ConstPlayer.PK_ALL || this.typePk != ConstPlayer.PK_ALL) || (!this.pvp.isInPVP(this.playerAttack) || !this.playerAttack.pvp.isInPVP(this)) || ((this.playerAttack.cFlag == 0 && this.cFlag == 0) && (this.playerAttack.cFlag != 8 || this.cFlag == this.playerAttack.cFlag))) || this.playerAttack == this || this.playerAttack == master) {
                this.playerAttack = null;
            }
            if (this.zone != null && this.playerAttack == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
                this.playerAttack = this.zone.playerPKInMap();
                this.lastTimeTargetPlayer = System.currentTimeMillis();
                this.timeTargetPlayer = Util.nextInt(40000, 45000);
            }
            return this.playerAttack;
        }
    }

    public void joinMapMaster() {
        if (status != GOHOME && !isDie()) {
            this.location.x = master.location.x + Util.nextInt(-10, 10);
            this.location.y = master.location.y;
            ChangeMapService.gI().goToMap(this, master.zone);
            this.zone.loadMeToAnother(this);
        }
    }


    private void moveIdle() {
        if (status == GOHOME) {
            return;
        }
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }


    @Override
    public void update() {
        super.update();
        if (isDie()) {
            if (System.currentTimeMillis() - lastTimeRevival > 30000) {
                Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
            } else {
                return;
            }
        }
        if (justRevived1 && this.zone == master.zone) {
            Service.gI().chatJustForMe(master, this, "Ta đã sống dậy rồi đây !!");
            justRevived1 = false;
        }
        if (master != null && (this.zone == null || this.zone != master.zone)) {
            joinMapMaster();
        }
        if (master != null && master.isDie() || effectSkill.isHaveEffectSkill()) {
            return;
        }
        if (Util.canDoWithTime(master.Autothucan, 900000) && master.trangthai) {
            if (master.inventory.ruby < 200) {
                Service.gI().sendThongBao(master, "|7|Không đủ Hồng ngọc");
                return;
            }
            master.inventory.ruby -= 200;
            master.TrieuHoiThucAn++;
            master.Autothucan = System.currentTimeMillis();
            if (master.TrieuHoiThucAn > 200) {
                master.TrieuHoiCapBac = -1;
                Service.gI().sendThongBao(master, "|7|Vì cho Chiến Thần ăn quá no nên Chiến Thần đã bạo thể mà chết.");
            } else {
                Service.gI().sendThongBao(master, "|2|Thức ăn Chiến Thần: " + master.TrieuHoiThucAn + "%\n|1|Bạn đã cho Chiến Thần ăn\nLưu ý: khi cho quá 200% Chiến Thần sẽ no quá mà chết");
            }
        }
        if (Util.canDoWithTime(master.TrieuHoilastTimeThucan, 600000)) {
            master.TrieuHoilastTimeThucan = System.currentTimeMillis();
            master.TrieuHoiThucAn--;
            master.TrieuHoiDame += Util.nextInt(50, 100);
            if (master.TrieuHoiThucAn <= 0) {
                master.TrieuHoiCapBac = -1;
                Service.gI().sendThongBao(master, "|7|Chiến Thần đã chết vì bị bỏ đói");
            } else if (master.TrieuHoiThucAn <= 20) {
                Service.gI().sendThongBao(master, "|7|Thức ăn Chiến Thần dưới 20%");
            }
            ServerNotify.gI().sendNotifyTab(master);
        }
        moveIdle();
        switch (status) {
            case FOLLOW -> followMaster(60);
            case ATTACK_PLAYER -> {
                Player plPr = getPlayerAttack();
                if (plPr != null && plPr != this && plPr != master && plPr.location != null) {
                    this.playerSkill.skillSelect = getSkill(1);
                    if (SkillService.gI().canUseSkillWithCooldown(this)) {
                        PlayerService.gI().playerMove(this, plPr.location.x + Util.nextInt(-60, 60), plPr.location.y);
                        SkillService.gI().useSkill(this, plPr, null, null);
                    }
                } else {
                    idle = true;
                }
            }
            case ATTACK_MOB -> {
                mobAttack = findMobAttack();
                if (mobAttack != null) {
                    this.playerSkill.skillSelect = getSkill(1);
                    if (SkillService.gI().canUseSkillWithCooldown(this)) {
                        PlayerService.gI().playerMove(this, mobAttack.location.x + Util.nextInt(-60, 60), mobAttack.location.y);
                        SkillService.gI().useSkill(this, null, mobAttack, null);
                    }
                } else {
                    idle = true;
                }
            }
            case GOHOME -> {
                if (this.zone != null && (this.zone.map.mapId == 21 || this.zone.map.mapId == 22 || this.zone.map.mapId == 23)) {
                    if (System.currentTimeMillis() - lastTimeMoveAtHome <= 5000) {
                        return;
                    } else {
                        if (this.zone.map.mapId == 21) {
                            if (directAtHome == -1) {

                                PlayerService.gI().playerMove(this, 250, 336);
                                directAtHome = 1;
                            } else {
                                PlayerService.gI().playerMove(this, 200, 336);
                                directAtHome = -1;
                            }
                        } else if (this.zone.map.mapId == 22) {
                            if (directAtHome == -1) {
                                PlayerService.gI().playerMove(this, 500, 336);
                                directAtHome = 1;
                            } else {
                                PlayerService.gI().playerMove(this, 452, 336);
                                directAtHome = -1;
                            }
                        }
                        lastTimeMoveAtHome = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    private Skill getSkill(int indexSkill) {
        return this.playerSkill.skills.get(indexSkill - 1);
    }

    public void followMaster() {
        if (this.isDie() || effectSkill.isHaveEffectSkill()) {
            return;
        }
        switch (this.status) {
            case ATTACK_MOB -> {
                if (mobAttack != null && Util.getDistance(this, master) <= 1500) {
                    return;
                }
                if (getPlayerAttack() != null && Util.getDistance(this, master) <= 1500) {
                    return;
                }
                followMaster(500);
            }
            case ATTACK_PLAYER -> {
                if (getPlayerAttack() != null && Util.getDistance(this, master) <= 1500) {
                    return;
                }
                followMaster(500);
            }
            case FOLLOW -> followMaster(500);
        }
    }

    private void followMaster(int dis) {
        try {
            if (master != null) {
                int mX = master.location.x;
                int mY = master.location.y;
                int disX = this.location.x - mX;
                if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= dis) {
                    if (disX < 0) {
                        this.location.x = mX - Util.nextInt(0, dis);
                    } else {
                        this.location.x = mX + Util.nextInt(0, dis);
                    }
                    this.location.y = mY;
                    PlayerService.gI().playerMove(this, this.location.x, this.location.y);
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void dispose() {
        this.mobAttack = null;
        this.master = null;
        super.dispose();
    }
}
