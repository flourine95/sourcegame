package com.girlkun.models.map.challenge;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossStatus;
import com.girlkun.models.boss.dhvt.*;
import com.girlkun.models.player.Player;
import com.girlkun.services.EffectSkillService;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;
import lombok.Getter;
import lombok.Setter;

/**
 * @author BTH sieu cap vippr0 
 */
public class MartialCongress {
    @Setter
    @Getter
    private Player player;
    private Boss boss;
    @Setter
    private Player npc;


    private int time;
    private int round;
    private int timeWait;

    public void update() {
        if (time > 0) {
            time--;
            if (player.isDie()) {
                die();
                return;
            }
            if (player.location != null && !player.isDie() && player != null && player.zone != null) {
                if (boss.isDie()) {
                    round++;
                    boss.leaveMap();
                    toTheNextRound();
                }
                if (player.location.y > 370) {
                    leave();
                }
            } else {
                endChallenge();
//                if (boss != null) {
//                    boss.leaveMap();
//                }
//                MartialCongressManager.gI().remove(this);
            }

        } else {
            timeOut();
        }
        if (timeWait > 0) {
            switch (timeWait) {
                case 10:
                    Service.getInstance().chat(npc, "Trận đấu giữa " + player.name + " VS " + boss.name + " sắp diễn ra");
                    ready();
                    break;
                case 8:
                    Service.getInstance().chat(npc, "Xin quý vị khán giả cho 1 tràng pháo tay để cổ vũ cho 2 đối thủ nào");
                    break;
                case 4:
                    Service.getInstance().chat(npc, "Mọi người ngồi sau hãy ổn định chỗ ngồi,trận đấu sẽ bắt đầu sau 3 giây nữa");
                    break;
                case 2:
                    Service.getInstance().chat(npc, "Trận đấu bắt đầu");
                    break;
                case 1:
                    Service.getInstance().chat(player, "Ok");
                    Service.getInstance().chat(boss, "Ok");
                    break;
            }
            timeWait--;
        }
    }

    public void ready() {
        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 10000);
        EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 10000);
        ItemTimeService.gI().sendItemTime(player, 3779, 10000 / 1000);
        Util.setTimeout(() -> {
            if (boss.effectSkill != null) {
                EffectSkillService.gI().removeStun(boss);
            }
            MartialCongressService.gI().sendTypePK(player, boss);
            PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
            boss.changeStatus(BossStatus.ACTIVE);
        }, 10000);
    }

    public void toTheNextRound() {
        try {
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            Boss bss = null;
            switch (round) {
                case 0:
                    bss = new SoiHecQuyn(player);
                    break;
                case 1:
                    bss = new ODo(player);
                    break;
                case 2:
                    bss = new Xinbato(player);
                    break;
                case 3:
                    bss = new ChaPa(player);
                    break;
                case 4:
                    bss = new PonPut(player);
                    break;
                case 5:
                    bss = new ChanXu(player);
                    break;
                case 6:
                    bss = new TauPayPay(player);
                    break;
                case 7:
                    bss = new Yamcha(player);
                    break;
                case 8:
                    bss = new JackyChun(player);
                    break;
                case 9:
                    bss = new ThienXinHang(player);
                    break;
                case 10:
                    bss = new LiuLiu(player);
                    break;
                default:
                    champion();
                    return;
            }
            MartialCongressService.gI().moveFast(player, 335, 250);
            setTimeWait(13);
            setBoss(bss);
            setTime(185);
        } catch (Exception e) {
            System.out.println("jjjjj");
        }
//        if (round > 0 && round < 11) {
//            bss.joinMap();
//        }
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setTimeWait(int timeWait) {
        this.timeWait = timeWait;
    }

    private void die() {
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì chết queo");
        if (player.zone != null) {
            endChallenge();
        }
    }

    private void timeOut() {
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì hết thời gian");
        endChallenge();
    }

    private void champion() {
        Service.getInstance().sendThongBao(player, "Chúc mừng " + player.name + " vừa đoạt giải vô địch");
        endChallenge();
    }

    public void leave() {
        setTime(0);
        EffectSkillService.gI().removeStun(player);
        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì rời khỏi võ đài");
        endChallenge();
    }

    private void reward() {
        if (player.levelWoodChest < round) {
            player.levelWoodChest = round;
        }
    }

    public void endChallenge() {
//        MartialCongressService.gI().check = false;
        reward();
        if (player.zone != null) {
            PlayerService.gI().hoiSinh(player);
        }
        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
        if (player != null && player.zone != null && player.zone.map.mapId == 129) {
            Util.setTimeout(() -> {
                ChangeMapService.gI().changeMapNonSpaceship(player, 129, player.location.x, 360);
            }, 500);
        }
        if (boss != null) {
            boss.leaveMap();
        }
        MartialCongressManager.gI().remove(this);
    }
}
