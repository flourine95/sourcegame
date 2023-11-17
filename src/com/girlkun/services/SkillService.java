package com.girlkun.services;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.intrinsic.Intrinsic;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.mob.MobMe;
import com.girlkun.models.player.*;
import com.girlkun.models.skill.Skill;
import com.girlkun.network.io.Message;
import com.girlkun.utils.Logger;
import com.girlkun.utils.SkillUtil;
import com.girlkun.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SkillService {

    private static SkillService i;

    private SkillService() {

    }

    public static SkillService gI() {
        if (i == null) {
            i = new SkillService();
        }
        return i;
    }

    public boolean useSkill(Player player, Player plTarget, Mob mobTarget, Message message) {
        try {
            if(player != null){
                if (player.effectSkill != null && player.effectSkill.isHaveEffectSkill()) {
                    return false;
                }
                if (player.playerSkill == null) {
                    return false;
                }
                if (player.playerSkill.skillSelect.template.type == 2 && canUseSkillWithMana(player) && canUseSkillWithCooldown(player)) {
                    useSkillBuffToPlayer(player, plTarget);
                    return true;
                }
                if ((player.effectSkill.isHaveEffectSkill()
                        && (player.playerSkill.skillSelect.template.id != Skill.TU_SAT
                        && player.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI
                        && player.playerSkill.skillSelect.template.id != Skill.MAKANKOSAPPO))
                        || (plTarget != null && !canAttackPlayer(player, plTarget))
                        || (mobTarget != null && mobTarget.isDie())
                        || !canUseSkillWithMana(player) || !canUseSkillWithCooldown(player)) {
                    return false;
                }
                if (player.effectSkill.useTroi) {
                    EffectSkillService.gI().removeUseTroi(player);
                }
                if (player.effectSkill.isCharging) {
                    EffectSkillService.gI().stopCharge(player);
                }
    //            if (player.isPet) {
    ////            ((Pet) player).lastTimeMoveIdle = System.currentTimeMillis();
    //            }
                switch (player.playerSkill.skillSelect.template.type) {
                    case 1:
                        useSkillAttack(player, plTarget, mobTarget);
    //                Service.getInstance().releaseCooldownSkill(player);
                        break;
                    case 3:
                        useSkillAlone(player);
                        break;
                    case 4:
    //                System.out.println("người chơi xài skill  mới       " + player.id);
                        userSkillSpecial(player, message);
                        break;
                    default:
                        return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("    Loi ueskill");
        }
        return true;
    }

    private void userSkillSpecial(Player player, Message message) {
        if (message == null) {
            return;
        }
        try {
            byte st = message.reader().readByte();
            byte skillId = message.reader().readByte();
            Short dx = message.reader().readShort();
            Short dy = message.reader().readShort();

            byte dir = message.reader().readByte();
            Short x = message.reader().readShort();
            Short y = message.reader().readShort();
            switch (skillId) {
                case Skill.SUPER_KAME:
//                    System.out.println("người chơi xài skill td " + player.id);
                    sendEffSkillSpecialID24(player, dir);
                    break;
                case Skill.LIEN_HOAN_CHUONG:
//                    System.out.println("người chơi xài skill xd " + player.id);
                    sendEffSkillSpecialID25(player, dir);
                    break;
                case Skill.MA_PHONG_BA:
//                    System.out.println("người chơi xài skill nm " + player.id);
                    sendEffSkillSpecialID26(player, dir);
                    break;
            }
            affterUseSkill(player, player.playerSkill.skillSelect.template.id);
            player.skillSpecial.setSkillSpecial(dir, dx, dy, x, y);
        } catch (Exception e) {
            System.out.println("             loi skill moiiiiiiiii");
        }
    }

    public void updateSkillSpecial(Player player) {
        try {
            if (player.isDie() || player.effectSkill.isHaveEffectSkill()) {
                player.skillSpecial.closeSkillSpecial();
                return;
            }
            if (player.skillSpecial.skillSpecial.template.id == Skill.MA_PHONG_BA) {
                if (Util.canDoWithTime(player.skillSpecial.lastTimeSkillSpecial, SkillSpecial.TIME_GONG)) {
                    player.skillSpecial.lastTimeSkillSpecial = System.currentTimeMillis();
                    player.skillSpecial.closeSkillSpecial();
                    int timeBinh = SkillUtil.getTimeBinh();//thời gian biến thành bình

                    //hút người
                    for (Player playerMap : player.zone.getPlayers()) {
                        if (player.skillSpecial.dir == -1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.skillSpecial.playersTaget.add(playerMap);

                        } else if (player.skillSpecial.dir == 1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.skillSpecial.playersTaget.add(playerMap);
                        }
                        if (playerMap == null || playerMap.id == player.id) {
                            continue;
                        }
                    }
                    //hút quái
                    for (Mob mobMap : player.zone.mobs) {
                        if (player.skillSpecial.dir == -1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                            player.skillSpecial.mobsTaget.add(mobMap);

                        } else if (player.skillSpecial.dir == 1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                            player.skillSpecial.mobsTaget.add(mobMap);

                        }
                        if (mobMap == null) {
                            continue;
                        }
                    }

                    //bắt đầu hút
                    this.startSkillSpecialID26(player);
                    Thread.sleep(3000);//nghỉ 3s

                    //biến quái - bình
                    for (Mob mobMap : player.zone.mobs) {
                        if (player.skillSpecial.dir == -1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                            player.skillSpecial.mobsTaget.add(mobMap);

                        } else if (player.skillSpecial.dir == 1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                            player.skillSpecial.mobsTaget.add(mobMap);

                        }
                        if (mobMap == null) {
                            continue;
                        }
                        EffectSkillService.gI().sendMobToBinh(player, mobMap, timeBinh);//biến mob thành bình
                        this.playerAttackMob(player, mobMap, false, true); // trừ dame 
                    }

                    //biến người - bình
                    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

                    for (Player playerMap : player.zone.getPlayers()) {
                        if (player.skillSpecial.dir == -1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.skillSpecial.playersTaget.add(playerMap);
                        } else if (player.skillSpecial.dir == 1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.skillSpecial.playersTaget.add(playerMap);
                        }
                        if (playerMap == null || playerMap.id == player.id) {
                            continue;
                        }
                        if (this.canAttackPlayer(player, playerMap)) {
                            ItemTimeService.gI().sendItemTime(playerMap, 14523, timeBinh / 1000);
                            EffectSkillService.gI().setBinh(playerMap, System.currentTimeMillis(), timeBinh);
                            Service.gI().Send_Caitrang(playerMap);
                            Skill curSkill = SkillUtil.getSkillbyId(player, Skill.MA_PHONG_BA);
                            double ptdame = 0;
                            if (curSkill.point == 1) {
                                ptdame = 0.01;
                            } else if (curSkill.point == 2) {
                                ptdame = 0.01;
                            } else if (curSkill.point == 3) {
                                ptdame = 0.02;
                            } else if (curSkill.point == 4) {
                                ptdame = 0.02;
                            } else if (curSkill.point == 5) {
                                ptdame = 0.03;
                            } else if (curSkill.point == 6) {
                                ptdame = 0.03;
                            } else if (curSkill.point == 7) {
                                ptdame = 0.04;
                            } else if (curSkill.point == 8) {
                                ptdame = 0.04;
                            } else if (curSkill.point == 9) {
                                ptdame = 0.06;
                            }
                            double dameHit = playerMap.nPoint.hpMax * ptdame;
                            for (int i = 0; i < 10; i++) {
                                final int index = i;
                                executorService.schedule(() -> {
                                    playerMap.injured(playerMap, dameHit, false, false);
                                    PlayerService.gI().sendInfoHpMpMoney(playerMap); //gửi in4 hp cho player bị nhốt
                                    this.playerAttackPlayer(player, playerMap, true);
                                    if (index == 0) {
                                        this.playerAttackPlayer(player, playerMap, true);
                                    }
                                }, index, TimeUnit.SECONDS);
                            }
                        }
                    }

                    // Sau khi hoàn thành tất cả các tác vụ, hủy bỏ ScheduledExecutorService
                    executorService.shutdown();

                }
            } else {
                if (player.skillSpecial.stepSkillSpecial == 0 && Util.canDoWithTime(player.skillSpecial.lastTimeSkillSpecial, SkillSpecial.TIME_GONG)) {
                    player.skillSpecial.lastTimeSkillSpecial = System.currentTimeMillis();
                    player.skillSpecial.stepSkillSpecial = 1;
                    if (player.skillSpecial.skillSpecial.template.id == Skill.SUPER_KAME) {
                        this.startSkillSpecialID24(player);
                    } else {
                        this.startSkillSpecialID25(player);
                    }
                } else if (player.skillSpecial.stepSkillSpecial == 1 && !Util.canDoWithTime(player.skillSpecial.lastTimeSkillSpecial, SkillSpecial.TIME_END_24_25)) {
                    for (Player playerMap : player.zone.getHumanoids()) {
                        if (player.skillSpecial.dir == -1 && !playerMap.isDie()
                                && playerMap.location.x <= player.location.x - 15
                                && Math.abs(playerMap.location.x - player.skillSpecial._xPlayer) <= player.skillSpecial._xObjTaget
                                && Math.abs(playerMap.location.y - player.skillSpecial._yPlayer) <= player.skillSpecial._yObjTaget
                                && this.canAttackPlayer(player, playerMap)) {
                            this.playerAttackPlayer(player, playerMap, false);
                            PlayerService.gI().sendInfoHpMpMoney(playerMap);
                        }
                        if (player.skillSpecial.dir == 1 && !playerMap.isDie()
                                && playerMap.location.x >= player.location.x + 15
                                && Math.abs(playerMap.location.x - player.skillSpecial._xPlayer) <= player.skillSpecial._xObjTaget
                                && Math.abs(playerMap.location.y - player.skillSpecial._yPlayer) <= player.skillSpecial._yObjTaget
                                && this.canAttackPlayer(player, playerMap)) {
                            this.playerAttackPlayer(player, playerMap, false);
                            PlayerService.gI().sendInfoHpMpMoney(playerMap);
                        }
                        if (playerMap == null) {
                            continue;
                        }
                    }
                    for (Mob mobMap : player.zone.mobs) {
                        if (player.skillSpecial.dir == -1 && !mobMap.isDie()
                                && mobMap.location.x <= player.skillSpecial._xPlayer - 15
                                && Math.abs(mobMap.location.x - player.skillSpecial._xPlayer) <= player.skillSpecial._xObjTaget
                                && Math.abs(mobMap.location.y - player.skillSpecial._yPlayer) <= player.skillSpecial._yObjTaget) {
                            this.playerAttackMob(player, mobMap, false, false);
                        }
                        if (player.skillSpecial.dir == 1 && !mobMap.isDie()
                                && mobMap.location.x >= player.skillSpecial._xPlayer + 15
                                && Math.abs(mobMap.location.x - player.skillSpecial._xPlayer) <= player.skillSpecial._xObjTaget
                                && Math.abs(mobMap.location.y - player.skillSpecial._yPlayer) <= player.skillSpecial._yObjTaget) {
                            this.playerAttackMob(player, mobMap, false, false);
                        }
                        if (mobMap == null) {
                            continue;
                        }
                    }
                } else if (player.skillSpecial.stepSkillSpecial == 1) {
                    player.skillSpecial.closeSkillSpecial();
                }
            }
        } catch (Exception e) {
        }
    }

    public void sendCurrLevelSpecial(Player player, Skill skill) {
        Message message = null;
        try {
            message = Service.gI().messageSubCommand((byte) 62);
            message.writer().writeShort(skill.skillId);
            message.writer().writeByte(0);
            message.writer().writeShort(skill.currLevel);
            player.sendMessage(message);
        } catch (final Exception ex) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    //============================================================================
    // Skill SuperKame
    public void sendEffSkillSpecialID24(Player player, byte dir) {
        Message message = null;
        try {
            message = new Message(-45);// passt code k dc vcb 
            message.writer().writeByte(20);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(24);
            message.writer().writeByte(1);
            message.writer().writeByte(dir); // -1 trai | 1 phai
            message.writer().writeShort(2000);
            message.writer().writeByte(0);
            message.writer().writeByte(player.gender);
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (Exception e) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    // Skill liên hoàn chưởng
    public void sendEffSkillSpecialID25(Player player, byte dir) {
        Message message = null;
        try {
            message = new Message(-45);// passt code k dc vcb 
            message.writer().writeByte(20);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(25);
            message.writer().writeByte(2);
            message.writer().writeByte(dir); // -1 trai | 1 phai
            message.writer().writeShort(2000);
            message.writer().writeByte(0);
            message.writer().writeByte(player.gender);
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (Exception e) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    // Skill Ma phong ba
    public void sendEffSkillSpecialID26(Player player, byte dir) {
        Message message = null;
        try {
            message = new Message(-45);// passt code k dc vcb 
            message.writer().writeByte(20);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(26);
            message.writer().writeByte(3);
            message.writer().writeByte(dir); // -1 trai | 1 phai
            message.writer().writeShort(SkillSpecial.TIME_GONG);
            message.writer().writeByte(0);
            message.writer().writeByte(player.gender);
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (Exception e) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    public void startSkillSpecialID24(Player player) {
        Message message = null;
        try {
            message = new Message(-45);
            message.writer().writeByte(21);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(player.skillSpecial.skillSpecial.template.id);
            message.writer().writeShort(player.skillSpecial._xPlayer + ((player.skillSpecial.dir == -1) ? (-player.skillSpecial._xObjTaget) : player.skillSpecial._xObjTaget));
            message.writer().writeShort(player.skillSpecial._xPlayer);
            message.writer().writeShort(3000); // thời gian skill chưởng chưởng nè
            message.writer().writeShort(player.skillSpecial._yObjTaget);
            message.writer().writeByte(player.gender);
            message.writer().writeByte(0);
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (final Exception ex) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    public void startSkillSpecialID25(Player player) {
        Message message = null;
        try {
            message = new Message(-45);
            message.writer().writeByte(21);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(player.skillSpecial.skillSpecial.template.id);
            message.writer().writeShort(player.skillSpecial._xPlayer + ((player.skillSpecial.dir == -1) ? (-player.skillSpecial._xObjTaget) : player.skillSpecial._xObjTaget));
            message.writer().writeShort(player.skillSpecial._yPlayer);
            message.writer().writeShort(3000); // thời gian skill chưởng chưởng nè
            message.writer().writeShort(25);
            message.writer().writeByte(player.gender);
            message.writer().writeByte(0);
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (final Exception ex) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }
    }

    public void startSkillSpecialID26(Player player) {
        Message message = null;
        try {
            message = new Message(-45);
            message.writer().writeByte(21);
            message.writer().writeInt((int) player.id);
            message.writer().writeShort(26);
            message.writer().writeShort(player.skillSpecial._xPlayer + ((player.skillSpecial.dir == -1) ? (-75) : 75));
            message.writer().writeShort(player.skillSpecial._yPlayer);
            message.writer().writeShort(3000);
            message.writer().writeShort(player.skillSpecial._yObjTaget);
            message.writer().writeByte(player.gender);
            final byte size = (byte) (player.skillSpecial.playersTaget.size() + player.skillSpecial.mobsTaget.size());
            message.writer().writeByte(size);
            for (Player playerMap : player.skillSpecial.playersTaget) {
                message.writer().writeByte(1);
                message.writer().writeInt((int) playerMap.id);

            }
            for (Mob mobMap : player.skillSpecial.mobsTaget) {
                message.writer().writeByte(0);
                message.writer().writeByte(mobMap.id);
            }
            message.writer().writeByte(0);
            Service.gI().sendMessAllPlayerInMap(player, message);
            message.cleanup();
        } catch (final Exception ex) {
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }
        }

    }

    // này hoc5 skill nha
    public void learSkillSpecial(Player player, byte skillID) {
        Message message = null;
        try {
            Skill curSkill = SkillUtil.createSkill(skillID, 1);
            SkillUtil.setSkill(player, curSkill);
            message = Service.gI().messageSubCommand((byte) 23);
            message.writer().writeShort(curSkill.skillId);
            player.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
            System.out.println("88888");
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }

        }
    }

    private void useSkillAttack(Player player, Player plTarget, Mob mobTarget) {
        try {
        if (!player.isBoss) {
            if (player.isTrieuHoiPet && ((SummonPet) player).master.TrieuHoiCapBac != -1
                    && ((SummonPet) player).master.thuTrieuHoi != null) {
                if (((SummonPet) player).master.TrieuHoiLevel < 100) {
                    if (((SummonPet) player).master.itemTimesieucap != null && ((SummonPet) player).master.itemTimesieucap.isUseXiMuoi) { //x3 kinh ngiệm Chiến Thần khi xài Xí Muội
                        ((SummonPet) player).master.TrieuHoiExpThanThu += (Util.nextInt(10000, 50000) * 3L);
                    } else {
                        ((SummonPet) player).master.TrieuHoiExpThanThu += Util.nextInt(10000, 50000);
                    }
                    if ((((SummonPet) player).master.TrieuHoiExpThanThu >= 3000000L + ((SummonPet) player).master.TrieuHoiLevel * 1500000L)
                            && ((SummonPet) player).master.TrieuHoiLevel < 100) {
                        ((SummonPet) player).master.TrieuHoiLevel++;
                        ((SummonPet) player).master.TrieuHoiExpThanThu = 0;
                        ((SummonPet) player).master.TrieuHoiDame += Util.limitDame(10000,
                                10000L + ((((SummonPet) player).master.TrieuHoiCapBac + 1) * 1000L));
                        ((SummonPet) player).master.TrieuHoiHP += Util.limitDame(50000,
                                50000L + ((((SummonPet) player).master.TrieuHoiCapBac + 1) * 5000L));
                    }
                }
                if (((SummonPet) player).master.TrieuHoiCapBac >= 8
                        && ((SummonPet) player).master.TrieuHoiCapBac <= 10) {
                    if (Util.isTrue(10f, 100)) {
                        int tnhan = (((SummonPet) player).master.TrieuHoiLevel + 1) * 20;
                        ((SummonPet) player).master.inventory.ruby += tnhan;
                        Service.gI().sendThongBao(((SummonPet) player).master,
                                "Bạn vừa nhận " + tnhan + " Hồng ngọc, Do Chiến Thần tìm được");
                        Service.gI().sendMoney(((SummonPet) player).master);
                    }
                }
//                if (((Thu_TrieuHoi) player).masterr.TrieuHoiCapBac == 7
//                        || ((Thu_TrieuHoi) player).masterr.TrieuHoiCapBac == 10) {
//                    if (Util.isTrue(0.01f, 100)) {
//                        int tnnhan = Util.nextInt(1, 4);
//                        if (tnnhan == 1) {
//                            ((Thu_TrieuHoi) player).masterr.nPoint.hpg += (((Thu_TrieuHoi) player).masterr.TrieuHoiLevel
//                                    + 1) * 20;
//                        } else if (tnnhan == 2) {
//                            ((Thu_TrieuHoi) player).masterr.nPoint.mpg += (((Thu_TrieuHoi) player).masterr.TrieuHoiLevel
//                                    + 1) * 20;
//                        } else if (tnnhan == 3) {
//                            ((Thu_TrieuHoi) player).masterr.nPoint.defg += (((Thu_TrieuHoi) player).masterr.TrieuHoiLevel
//                                    + 1) * 20;
//                        } else if (tnnhan == 4) {
//                            ((Thu_TrieuHoi) player).masterr.nPoint.dameg += (((Thu_TrieuHoi) player).masterr.TrieuHoiLevel
//                                    + 1) * 5;
//                        }
//                    }
//                }
            } else if (player.isPet) {
                if (player.nPoint.stamina > 0) {
                    player.nPoint.numAttack++;
                    boolean haveCharmPet = ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis();
                    if (haveCharmPet ? player.nPoint.numAttack >= 5 : player.nPoint.numAttack >= 2) {
                        player.nPoint.numAttack = 0;
                        player.nPoint.stamina--;
                    }
                } else {
                    ((Pet) player).askPea();
                    return;
                }
            } else {
                if (player.nPoint.stamina > 0) {
                    if (player.charms.tdDeoDai < System.currentTimeMillis()) {
                        player.nPoint.numAttack++;
                        if (player.nPoint.numAttack == 5) {
                            player.nPoint.numAttack = 0;
                            player.nPoint.stamina--;
                            PlayerService.gI().sendCurrentStamina(player);
                        }
                    }
                } else {
                    Service.gI().sendThongBao(player, "Thể lực đã cạn kiệt, hãy nghỉ ngơi để lấy lại sức");
                    return;
                }
            }
        }
        List<Mob> mobs;
        boolean miss = false;
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.KAIOKEN: //kaioken
                double hpUse = player.nPoint.hpMax / 100 * 10;
                if (player.nPoint.hp <= hpUse) {
                    break;
                } else {
                    player.nPoint.setHp(player.nPoint.hp - hpUse);
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    Service.gI().Send_Info_NV(player);
                }
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.GALICK:
            case Skill.LIEN_HOAN:
                if (plTarget != null && Util.getDistance(player, plTarget) > Skill.RANGE_ATTACK_CHIEU_DAM) {
                    miss = true;
                }
                if (mobTarget != null && Util.getDistance(player, mobTarget) > Skill.RANGE_ATTACK_CHIEU_DAM) {
                    miss = true;
                }
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                if (plTarget != null) {
                    playerAttackPlayer(player, plTarget, miss);
                }
                if (mobTarget != null) {
                    playerAttackMob(player, mobTarget, miss, false);
                }
                if (player.mobMe != null) {
                    player.mobMe.attack(plTarget, mobTarget);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            //******************************************************************
            case Skill.QUA_CAU_KENH_KHI:
                if (!player.playerSkill.prepareQCKK) {
                    //bắt đầu tụ quả cầu
                    player.playerSkill.prepareQCKK = !player.playerSkill.prepareQCKK;
                    player.playerSkill.lastTimePrepareQCKK = System.currentTimeMillis();
                    sendPlayerPrepareSkill(player, 4000);
                } else {
                    //ném cầu
                    player.playerSkill.prepareQCKK = !player.playerSkill.prepareQCKK;
                    mobs = new ArrayList<>();
                    if (plTarget != null) {
                        playerAttackPlayer(player, plTarget, false);
                        for (Mob mob : player.zone.mobs) {
                            if (!mob.isDie()
                                    && Util.getDistance(plTarget, mob) <= SkillUtil.getRangeQCKK(player.playerSkill.skillSelect.point)) {
                                mobs.add(mob);
                            }
                        }
                    }
                    if (mobTarget != null) {
                        playerAttackMob(player, mobTarget, false, true);
                        for (Mob mob : player.zone.mobs) {
                            if (!mob.equals(mobTarget) && !mob.isDie()
                                    && Util.getDistance(mob, mobTarget) <= SkillUtil.getRangeQCKK(player.playerSkill.skillSelect.point)) {
                                mobs.add(mob);
                            }
                        }
                    }
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                break;
            case Skill.MAKANKOSAPPO:
                if (!player.playerSkill.prepareLaze) {
                    //bắt đầu nạp laze
                    player.playerSkill.prepareLaze = !player.playerSkill.prepareLaze;
                    player.playerSkill.lastTimePrepareLaze = System.currentTimeMillis();
                    sendPlayerPrepareSkill(player, 3000);
                } else {
                    //bắn laze
                    player.playerSkill.prepareLaze = !player.playerSkill.prepareLaze;
                    if (plTarget != null) {
                        playerAttackPlayer(player, plTarget, false);
                    }
                    if (mobTarget != null) {
                        playerAttackMob(player, mobTarget, false, true);
//                        mobTarget.attackMob(player, false, true);
                    }
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                PlayerService.gI().sendInfoHpMpMoney(player);
                break;
            case Skill.SOCOLA:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.SOCOLA);
                int timeSocola = SkillUtil.getTimeSocola();
                if (plTarget != null) {
                    EffectSkillService.gI().setSocola(plTarget, System.currentTimeMillis(), timeSocola);
                    Service.gI().Send_Caitrang(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 3780, timeSocola / 1000);
                }
                if (mobTarget != null) {
                    EffectSkillService.gI().sendMobToSocola(player, mobTarget, timeSocola);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                int timeChoangDCTT = SkillUtil.getTimeDCTT(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    Service.gI().setPos(player, plTarget.location.x, plTarget.location.y);
                    playerAttackPlayer(player, plTarget, miss);
                    EffectSkillService.gI().setBlindDCTT(plTarget, System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                    PlayerService.gI().sendInfoHpMpMoney(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 3779, timeChoangDCTT / 1000);
                }
                if (mobTarget != null) {
                    Service.gI().setPos(player, mobTarget.location.x, mobTarget.location.y);
//                    mobTarget.attackMob(player, false, false);
                    playerAttackMob(player, mobTarget, false, false);
                    mobTarget.effectSkill.setStartBlindDCTT(System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                }
                player.nPoint.isCrit100 = true;
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.THOI_MIEN:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.THOI_MIEN);
                int timeSleep = SkillUtil.getTimeThoiMien(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    EffectSkillService.gI().setThoiMien(plTarget, System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SLEEP_EFFECT);
                    ItemTimeService.gI().sendItemTime(plTarget, 3782, timeSleep / 1000);
                }
                if (mobTarget != null) {
                    mobTarget.effectSkill.setThoiMien(System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SLEEP_EFFECT);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TROI:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.TROI);
                int timeHold = SkillUtil.getTimeTroi(player.playerSkill.skillSelect.point);
                EffectSkillService.gI().setUseTroi(player, System.currentTimeMillis(), timeHold);
                if (plTarget != null && (!plTarget.playerSkill.prepareQCKK && !plTarget.playerSkill.prepareLaze && !plTarget.playerSkill.prepareTuSat)) {
                    player.effectSkill.plAnTroi = plTarget;
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HOLD_EFFECT);
                    EffectSkillService.gI().setAnTroi(plTarget, player, System.currentTimeMillis(), timeHold);
                }
                if (mobTarget != null) {
                    player.effectSkill.mobAnTroi = mobTarget;
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HOLD_EFFECT);
                    mobTarget.effectSkill.setTroi(System.currentTimeMillis(), timeHold);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                if (plTarget != null
                        && plTarget.isBoss
                        && MapService.gI().isMapHuyDiet(player.zone.map.mapId)
                        && Util.isTrue(40, 100)) {
                    EffectSkillService.gI().removeUseTroi(player);
                    EffectSkillService.gI().removeAnTroi(plTarget);
                    Service.gI().chat(plTarget, "Chiêu đó không có tác dụng đâu kaka");
                }
                if (plTarget != null
                        && plTarget.isBoss
                        && MapService.gI().isMap1sao(player.zone.map.mapId)
                        && Util.isTrue(99, 100)) {
                    EffectSkillService.gI().removeUseTroi(player);
                    EffectSkillService.gI().removeAnTroi(plTarget);
                    Service.gI().chat(plTarget, "Chiêu đó không có tác dụng đâu kaka");
                }
                break;
        }
        if (!player.isBoss) {
            player.effectSkin.lastTimeAttack = System.currentTimeMillis();
        }
        } catch (Exception e) {
            e.getStackTrace();
            System.err.println("  Loi Skillattack");
        }
    }

    private void useSkillAlone(Player player) {
        try {
            
        List<Mob> mobs;
        List<Player> players;
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.THAI_DUONG_HA_SAN:
                int timeStun = SkillUtil.getTimeStun(player.playerSkill.skillSelect.point);
//                if (player.setClothes.thienXinHang == 5) {
//                    timeStun *= 2;
//                }
                mobs = new ArrayList<>();
                players = new ArrayList<>();
                if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                    List<Player> playersMap = player.zone.getHumanoids();
                    for (Player pl : playersMap) {
                        if (!pl.nPoint.khangTDHS) {
                            if (pl != null && !player.equals(pl)) {
                                if (Util.getDistance(player, pl) <= SkillUtil.getRangeStun(player.playerSkill.skillSelect.point)
                                        && canAttackPlayer(player, pl) //                                        && (!pl.playerSkill.prepareQCKK && !pl.playerSkill.prepareLaze && !pl.playerSkill.prepareTuSat)
                                        ) {
                                    if (player.isPet && ((Pet) player).master.equals(pl)) {
                                        continue;
                                    }
                                    EffectSkillService.gI().startStun(pl, System.currentTimeMillis(), timeStun);
                                    players.add(pl);
                                }
                            }
                        } else {
                            Service.gI().chat(pl, "Kháng choáng rồi lêu lêu");
                        }
                    }
                }
                for (Mob mob : player.zone.mobs) {
                    if (Util.getDistance(player, mob) <= SkillUtil.getRangeStun(player.playerSkill.skillSelect.point)) {
                        mob.effectSkill.startStun(System.currentTimeMillis(), timeStun);
                        mobs.add(mob);
                    }
                }
                EffectSkillService.gI().sendEffectBlindThaiDuongHaSan(player, players, mobs, timeStun);
                if (player.isPl()) {
                    player.achievement.plusCount(14);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.DE_TRUNG:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.DE_TRUNG);
                if (player.mobMe != null) {
                    player.mobMe.mobMeDie();
                }
                player.mobMe = new MobMe(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.BIEN_KHI:
                EffectSkillService.gI().sendEffectMonkey(player);
                EffectSkillService.gI().setIsMonkey(player);
                EffectSkillService.gI().sendEffectMonkey(player);

                Service.gI().sendSpeedPlayer(player, 0);
                Service.gI().Send_Caitrang(player);
                Service.gI().sendSpeedPlayer(player, -1);
                if (!player.isPet) {
                    PlayerService.gI().sendInfoHpMp(player);
                }
                Service.gI().point(player);
                Service.gI().Send_Info_NV(player);
                Service.gI().sendInfoPlayerEatPea(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.KHIEN_NANG_LUONG:
                EffectSkillService.gI().setStartShield(player);
                EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SHIELD_EFFECT);
                ItemTimeService.gI().sendItemTime(player, 3784, player.effectSkill.timeShield / 1000);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.HUYT_SAO:
                long tileHP = SkillUtil.getPercentHPHuytSao(player.playerSkill.skillSelect.point);
                if (player.zone != null) {
                    if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                        List<Player> playersMap = player.zone.getHumanoids();
                        for (Player pl : playersMap) {
                            if (pl.effectSkill.useTroi) {
                                EffectSkillService.gI().removeUseTroi(pl);
                            }
                            if (!pl.isBoss && pl.gender != ConstPlayer.NAMEC
                                    && player.cFlag == pl.cFlag) {
                                EffectSkillService.gI().setStartHuytSao(pl, tileHP);
                                EffectSkillService.gI().sendEffectPlayer(pl, pl, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HUYT_SAO_EFFECT);
                                pl.nPoint.calPoint();
                                pl.nPoint.setHp(pl.nPoint.hp + (pl.nPoint.hp * tileHP / 100));
                                Service.gI().point(pl);
                                Service.gI().Send_Info_NV(pl);
                                ItemTimeService.gI().sendItemTime(pl, 3781, 30);
                                PlayerService.gI().sendInfoHpMp(pl);
                            }

                        }
                    } else {
                        EffectSkillService.gI().setStartHuytSao(player, tileHP);
                        EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HUYT_SAO_EFFECT);
                        player.nPoint.calPoint();
                        player.nPoint.setHp(player.nPoint.hp + (player.nPoint.hp * tileHP / 100));
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        ItemTimeService.gI().sendItemTime(player, 3781, 30);
                        PlayerService.gI().sendInfoHpMp(player);
                    }
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TAI_TAO_NANG_LUONG:
                EffectSkillService.gI().startCharge(player);
                if (player.isPl()) {
                    player.achievement.plusCount(14);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TU_SAT:
                if (!player.playerSkill.prepareTuSat) {
                    //gồng tự sát
                    player.playerSkill.prepareTuSat = !player.playerSkill.prepareTuSat;
                    player.playerSkill.lastTimePrepareTuSat = System.currentTimeMillis();
                    sendPlayerPrepareBom(player, 2000);
                } else {
                    if (!player.isBoss && !Util.canDoWithTime(player.playerSkill.lastTimePrepareTuSat, 1500)) {
                        player.playerSkill.skillSelect.lastTimeUseThisSkill = System.currentTimeMillis();
                        player.playerSkill.prepareTuSat = false;
                        return;
                    }
                    //nổ
                    player.playerSkill.prepareTuSat = !player.playerSkill.prepareTuSat;
                    int rangeBom = SkillUtil.getRangeBom(player.playerSkill.skillSelect.point);
                    double dame = player.nPoint.hpMax;
                    for (Mob mob : player.zone.mobs) {
                        mob.injured(player, dame, true);
                        if (Util.getDistance(player, mob) <= rangeBom) { //khoảng cách có tác dụng bom
                            mob.injured(player, dame, true);
                        }
                    }
                    List<Player> playersMap = null;
                    if (player.isBoss) {
                        playersMap = player.zone.getNotBosses();
                    } else {
                        playersMap = player.zone.getHumanoids();
                    }
                    if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                        for (Player pl : playersMap) {
                            if (!player.equals(pl) && canAttackPlayer(player, pl)) {
                                pl.injured(player, pl.isBoss ? dame / 2 : dame, false, false);
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                Service.gI().Send_Info_NV(pl);
                            }
                        }
                    }
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                    player.injured(null, 2100000000, true, false);
                    if (player.effectSkill.tiLeHPHuytSao != 0) {
                        player.effectSkill.tiLeHPHuytSao = 0;
                        EffectSkillService.gI().removeHuytSao(player);
                    }
                }
                break;
        }
        } catch (Exception e) {
            System.err.println("   loi skillalone");
        }
    }

    public void useSkillBuffToPlayer(Player player, Player plTarget) {
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.TRI_THUONG:
                List<Player> playersMap = null;
                playersMap = player.zone.getNotBosses();
                Message msg = null;
                Player plTriThuong = null;
                for (Player pl : playersMap) {
                    if (pl != null && player != null && plTarget != null && pl.zone.zoneId == player.zone.zoneId && !pl.isNewPet && !pl.isBoss && !plTarget.isBoss && !plTarget.isNewPet && canHsPlayer(player, plTarget)) {
                        if (player.playerSkill.skillSelect.point > 1) {
                            plTriThuong = pl;
                        } else if (player.playerSkill.skillSelect.point == 1) {
                            plTriThuong = plTarget;
                        }
                        if (plTriThuong != null) {
                            int percentTriThuong = SkillUtil.getPercentTriThuong(player.playerSkill.skillSelect.point);
                            Service.gI().chat(plTriThuong, "Cảm ơn " + player.name + " đã cứu mình");
                            try {
                                msg = new Message(-60);
                                msg.writer().writeInt((int) player.id); //id pem
                                msg.writer().writeByte(player.playerSkill.skillSelect.skillId); //skill pem
                                msg.writer().writeByte(1); //số người pem
                                msg.writer().writeInt((int) plTriThuong.id); //id ăn pem
                                msg.writer().writeByte(0); //read continue
                                msg.writer().writeByte(player.playerSkill.skillSelect.template.type); //type skill
                                Service.gI().sendMessAllPlayerInMap(plTriThuong.zone, msg);
                                msg.cleanup();
                                boolean isDie = plTriThuong.isDie();
                                player.nPoint.addHp((player.nPoint.hpMax * percentTriThuong / 100));
                                plTriThuong.nPoint.addHp((plTriThuong.nPoint.hpMax * percentTriThuong / 100));
                                plTriThuong.nPoint.addMp((plTriThuong.nPoint.mpMax * percentTriThuong / 100));
                                if (isDie) {
                                    Service.gI().Send_Info_NV(player);
                                    Service.gI().hsChar(plTriThuong, plTriThuong.nPoint.getHP(), plTriThuong.nPoint.getMP());
                                    PlayerService.gI().sendInfoHpMpMoney(plTriThuong);
                                    PlayerService.gI().sendInfoHpMp(player);
                                } else {
                                    Service.gI().Send_Info_NV(player);
                                    PlayerService.gI().sendInfoHpMpMoney(plTriThuong);
                                    PlayerService.gI().sendInfoHpMp(player);
                                }
                                Service.gI().Send_Info_NV(plTriThuong);
                            } catch (IOException e) {
                                //                            e.printStackTrace();
                            }
                        }
                    }
                }
                if (player.isPl()) {
                    player.achievement.plusCount(14);
                }

                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                playersMap = null;
                break;
        }
    }

    private void phanSatThuong(Player plAtt, Player plTarget, double dame) {
        double damePST = 0;
        int percentPST = 0;
        percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            damePST = dame * percentPST / 100;
            Message msg;
            try {
                msg = new Message(56);
                msg.writer().writeInt((int) plAtt.id);
//                if (damePST >= plAtt.nPoint.hp) {
//                    damePST = Util.DoubleGioihan(plAtt.nPoint.hp) - 1;
//                }
                damePST = (damePST >= plAtt.nPoint.hp || plAtt.nPoint.hp < 2) ? 0 : plAtt.injured(null, damePST, true, false);
                plAtt.nPoint.hp = (damePST >= plAtt.nPoint.hp) ? 1 : (plAtt.nPoint.hp - damePST);
                msg.writer().writeInt(Util.DoubleGioihan(plAtt.nPoint.hp));
                msg.writer().writeInt(Util.DoubleGioihan(damePST));
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(plAtt, msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(SkillService.class, e);
            }
        }
    }

    private void hutHPMP(Player player, double dame, boolean attackMob) {
        int tiLeHutHp = player.nPoint.getTileHutHp(attackMob);
        int tiLeHutMp = player.nPoint.getTiLeHutMp();
        int hpHoi = Util.DoubleGioihan(dame * tiLeHutHp / 100);
        int mpHoi = Util.DoubleGioihan(dame * tiLeHutMp / 100);
        if (hpHoi > 0 || mpHoi > 0) {
            PlayerService.gI().hoiPhuc(player, hpHoi, mpHoi);
        }
    }

    private void playerAttackPlayer(Player plAtt, Player plInjure, boolean miss) {
        if (plInjure.effectSkill.anTroi) {
            plAtt.nPoint.isCrit100 = true;
        }
        double dameHit = plInjure.injured(plAtt, miss ? 0 : plAtt.nPoint.getDameAttack(false), false, false);
        phanSatThuong(plAtt, plInjure, dameHit);
        hutHPMP(plAtt, dameHit, false);
        Message msg;
        try {
            msg = new Message(-60);
            msg.writer().writeInt((int) plAtt.id); //id pem
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId); //skill pem
            msg.writer().writeByte(1); //số người pem
            msg.writer().writeInt((int) plInjure.id); //id ăn pem
            byte typeSkill = SkillUtil.getTyleSkillAttack(plAtt.playerSkill.skillSelect);
            msg.writer().writeByte(typeSkill == 2 ? 0 : 1); //read continue
            msg.writer().writeByte(0); //type skill
            if (dameHit > 2123456789) {
                if (plAtt.isPl()) {
                    plAtt.Hppl = "\n|8|Tên Boss:\b|4|" + plInjure.name;
                    plAtt.Hppl += "\n|8|HP Boss:\b|7|" + Util.powerToString(plInjure.nPoint.hp);
                    if (dameHit > 1) {
                        plAtt.Hppl += "\b|8|Dame lên Boss:\b|7|" + Util.powerToString(dameHit);
                    } else {
                        plAtt.Hppl += "\nDame lên Boss: HỤT";
                    }
                }
                if (plInjure instanceof TestDame) {
                    plAtt.totalDame += dameHit;
                    Service.gI().sendThongBao(plAtt, "|4|Dame thật: \b|5|" + Util.format(dameHit) + "\n\n|1| Tổng DAME trong 5 Giây: \b|7|" + Util.numberToMoney((long) plAtt.totalDame));

                    if (plAtt.isResetDame) {
                        plAtt.lastTimeDame = System.currentTimeMillis();
                        plAtt.isResetDame = false;
                    }
                }
                if (dameHit > 2123455999 && !(plInjure instanceof TestDame)) {
                    Service.gI().sendThongBao(plAtt, "|4|Dame thật: \b|5|" + Util.format(dameHit));
                }
                dameHit = 2123456000;
                msg.writer().writeInt(Util.DoubleGioihan(dameHit)); //dame ăn
            } else {
                if (plAtt.isPl()) {
                    plAtt.Hppl = "\n|8|Name Boss:\b|4|" + plInjure.name;
                    plAtt.Hppl += "\n|8|Hp Boss:\b|7|" + Util.powerToString(plInjure.nPoint.hp);
                    if (dameHit > 1) {
                        plAtt.Hppl += "\b|8|Dame lên Boss:\b|7|" + Util.powerToString(dameHit);
                    } else {
                        plAtt.Hppl += "\nDame lên Boss: HỤT";
                    }
                }

                if (plInjure instanceof TestDame) {
                    plAtt.totalDame += dameHit;
                    Service.gI().sendThongBao(plAtt, "|4|Sát thương vừa gây ra: \b|5|" + Util.format(dameHit) + "\n" +
                            "|1| Tổng sát thương trong 5 giây: \b|7|" + Util.numberToMoney((long) plAtt.totalDame));

                    if (plAtt.isResetDame) {
                        plAtt.lastTimeDame = System.currentTimeMillis();
                        plAtt.isResetDame = false;
                    }
                }
                msg.writer().writeInt(Util.DoubleGioihan(dameHit)); //dame ăn
            }

            msg.writer().writeBoolean(plInjure.isDie()); //is die
            msg.writer().writeBoolean(plAtt.nPoint.isCrit); //crit
            if (typeSkill != 1) {
                Service.gI().sendMessAllPlayerInMap(plAtt, msg);
                msg.cleanup();
            } else {
                plInjure.sendMessage(msg);
                msg.cleanup();
                msg = new Message(-60);
                msg.writer().writeInt((int) plAtt.id); //id pem
                msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId); //skill pem
                msg.writer().writeByte(1); //số người pem
                msg.writer().writeInt((int) plInjure.id); //id ăn pem
                msg.writer().writeByte(typeSkill == 2 ? 0 : 1); //read continue
                msg.writer().writeByte(0); //type skill
                msg.writer().writeInt(Util.DoubleGioihan(dameHit)); //dame ăn
                msg.writer().writeBoolean(plInjure.isDie()); //is die
                msg.writer().writeBoolean(plAtt.nPoint.isCrit); //crit
                Service.gI().sendMessAnotherNotMeInMap(plInjure, msg);
                msg.cleanup();
            }
            try {
                msg = Service.gI().messageSubCommand((byte) 14);
                msg.writer().writeInt((int) plInjure.id);
                msg.writer().writeInt(Util.DoubleGioihan(plInjure.nPoint.hp));
                msg.writer().writeByte(0);
                msg.writer().writeInt(Util.DoubleGioihan(plInjure.nPoint.hpMax));
                Service.gI().sendMessAnotherNotMeInMap(plInjure, msg);
                msg.cleanup();
            } catch (Exception e) {

            }
            Service.gI().addSMTN(plInjure, (byte) 1, 1, false);
            if (plInjure.isDie() && !plAtt.isBoss) {
                plAtt.fightMabu.changePoint((byte) 5);
            }

        } catch (Exception e) {
            Logger.logException(SkillService.class, e);
        }

        if (plInjure.isBoss && plInjure.nPoint.hp > 2100000000) {
            try {
                msg = new Message(44);
                msg.writer().writeInt((int) plInjure.id);
                msg.writer().writeUTF("Hp của ta:\b|7| " + Util.format(plInjure.nPoint.hp));
                Service.gI().sendMessAllPlayerInMap(plInjure, msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    private void playerAttackMob(Player plAtt, Mob mob, boolean miss, boolean dieWhenHpFull) {
        if (!mob.isDie()) {
            double dameHit = plAtt.nPoint.getDameAttack(true);
            if (plAtt.charms.tdBatTu > System.currentTimeMillis() && plAtt.nPoint.hp == 1) {
                dameHit = 0;
            }
            if (plAtt.charms.tdManhMe > System.currentTimeMillis()) {
                dameHit += (dameHit * 150 / 100);
            }
            if (plAtt.isPet) {
                if (plAtt.charms.tdDeTu > System.currentTimeMillis()) {
                    dameHit *= 2;
                }
            }
            if (miss) {
                dameHit = 0;
            }
            if (mob.isSieuQuai()) {
                if (dameHit > mob.point.maxHp / 10) {
                    dameHit = mob.point.maxHp / 10;
                }
            }
            hutHPMP(plAtt, dameHit, true);
            sendPlayerAttackMob(plAtt, mob);
            mob.injured(plAtt, dameHit, dieWhenHpFull);
            if (plAtt.isPl() && !plAtt.isDie()) {
                plAtt.HpQuai = "|6|Mục tiêu: QUÁI\n";
                plAtt.HpQuai += "\n|7|Hp Quái:\n" + Util.powerToString(mob.point.hp);
                if (dameHit > 1) {
                    plAtt.HpQuai += "\b|2|Dame Quái:\n" + Util.powerToString(dameHit);
                } else {
                    plAtt.HpQuai += "\nDame Quái: HỤT";
                }
            }
        }
    }

    private void sendPlayerPrepareSkill(Player player, int affterMiliseconds) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(4);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(affterMiliseconds);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendPlayerPrepareBom(Player player, int affterMiliseconds) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(7);
            msg.writer().writeInt((int) player.id);
//            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(104);
            msg.writer().writeShort(affterMiliseconds);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public boolean canUseSkillWithMana(Player player) {
        if (player.playerSkill.skillSelect != null) {
            if (player.playerSkill.skillSelect.template.id == Skill.KAIOKEN) {
                double hpUse = player.nPoint.hpMax / 100 * 10;
                if (player.nPoint.hp <= hpUse) {
                    return false;
                }
            }
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0:
                    return player.nPoint.mp >= player.playerSkill.skillSelect.manaUse;
                case 1:
                    double mpUse = player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100;
                    return player.nPoint.mp >= mpUse;
                case 2:
                    return player.nPoint.mp > 0;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean canUseSkillWithCooldown(Player player) {
        return Util.canDoWithTime(player.playerSkill.skillSelect.lastTimeUseThisSkill,
                player.playerSkill.skillSelect.coolDown - 50);
    }

    private void affterUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        switch (skillId) {
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                if (player.isPl()) {
                    player.achievement.plusCount(4);
                }
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                if (intrinsic.id == 6) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
                break;
            case Skill.THOI_MIEN:
                if (intrinsic.id == 7) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
                break;
            case Skill.SOCOLA:
                if (intrinsic.id == 14) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
                break;
            case Skill.TROI:
                if (intrinsic.id == 22) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
                break;
        }
        setMpAffterUseSkill(player);
        setLastTimeUseSkill(player, skillId);
    }

    private void setMpAffterUseSkill(Player player) {
        if (player.playerSkill.skillSelect != null) {
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0:
                    if (player.nPoint.mp >= player.playerSkill.skillSelect.manaUse) {
                        player.nPoint.setMp(player.nPoint.mp - player.playerSkill.skillSelect.manaUse);
                    }
                    break;
                case 1:
                    double mpUse = player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100;
                    if (player.nPoint.mp >= mpUse) {
                        player.nPoint.setMp(player.nPoint.mp - mpUse);
                    }
                    break;
                case 2:
                    player.nPoint.setMp(0);
                    break;
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
        }
    }

    public void setLastTimeUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        int subTimeParam = 0;
        switch (skillId) {
            case Skill.TRI_THUONG:
                if (intrinsic.id == 10) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.THAI_DUONG_HA_SAN:
                if (intrinsic.id == 3) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.QUA_CAU_KENH_KHI:
                if (intrinsic.id == 4) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.KHIEN_NANG_LUONG:
                if (intrinsic.id == 5 || intrinsic.id == 15 || intrinsic.id == 20) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.MAKANKOSAPPO:
                if (intrinsic.id == 11) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.DE_TRUNG:
                if (intrinsic.id == 12) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.TU_SAT:
                if (intrinsic.id == 19) {
                    subTimeParam = intrinsic.param1;
                }
                break;
            case Skill.HUYT_SAO:
                if (intrinsic.id == 21) {
                    subTimeParam = intrinsic.param1;
                }
                break;
        }
        int coolDown = player.playerSkill.skillSelect.coolDown;
        player.playerSkill.skillSelect.lastTimeUseThisSkill = System.currentTimeMillis() - ((long) coolDown * subTimeParam / 100);
        if (subTimeParam != 0) {
            Service.gI().sendTimeSkill(player);
        }
    }

    private boolean canHsPlayer(Player player, Player plTarget) {
        if (plTarget == null) {
            return false;
        }
        if (plTarget.isBoss) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_ALL) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_PVP) {
            return false;
        }
        if (player.cFlag != 0) {
            return plTarget.cFlag == 0 || plTarget.cFlag == player.cFlag;
        } else return plTarget.cFlag == 0;
    }

    private boolean canAttackPlayer(Player p1, Player p2) {
        if (p1.isDie() || p2.isDie()) {
            return false;
        }
        if (p1.zone.map.mapId == 129 && p1.typePk > 0 && p2.typePk > 0) {
            return true;
        }

        if (p1.typePk == ConstPlayer.PK_ALL || p2.typePk == ConstPlayer.PK_ALL) {
            return true;
        }
        if (p1.typePk == ConstPlayer.PK_PVP || p2.typePk == ConstPlayer.PK_PVP) {
            return true;
        }
        if ((p1.cFlag != 0 && p2.cFlag != 0)
                && (p1.cFlag == 8 || p2.cFlag == 8 || p1.cFlag != p2.cFlag)) {
            return true;
        }
        if (p1.pvp == null || p2.pvp == null) {
            return false;
        }
        return p1.pvp.isInPVP(p2) || p2.pvp.isInPVP(p1);
    }

    private void sendPlayerAttackMob(Player plAtt, Mob mob) {
        Message msg;
        try {
            msg = new Message(54);
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(plAtt, msg);
            msg.cleanup();

        } catch (Exception e) {

        }
    }

    public void selectSkill(Player player, int skillId) {
        Skill skillBefore = player.playerSkill.skillSelect;
        for (Skill skill : player.playerSkill.skills) {
            if (skill.skillId != -1 && skill.template.id == skillId) {
                player.playerSkill.skillSelect = skill;
                switch (skillBefore.template.id) {
                    case Skill.DRAGON:
                    case Skill.KAMEJOKO:
                    case Skill.DEMON:
                    case Skill.MASENKO:
                    case Skill.LIEN_HOAN:
                    case Skill.GALICK:
                    case Skill.ANTOMIC:
                        switch (skill.template.id) {
                            case Skill.DRAGON:
                            case Skill.KAMEJOKO:
                            case Skill.DEMON:
                            case Skill.MASENKO:
                            case Skill.LIEN_HOAN:
                            case Skill.GALICK:
                            case Skill.ANTOMIC:
//                                skill.lastTimeUseThisSkill = System.currentTimeMillis() + (skill.coolDown / 100);
                                break;
                        }
                        break;
                }
                break;
            }
        }
    }
}

/**
 * Code được viết bởi Hoàng Việt Vui lòng không sao chép mã nguồn này dưới mọi
 * hình thức.
 */
