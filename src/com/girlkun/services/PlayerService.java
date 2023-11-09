package com.girlkun.services;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.Client;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;


public class PlayerService {
    private static final int COST_GOLD_HOI_SINH = 10000000;
    private static final int COST_GEM_HOI_SINH = 1;
    private static final int COST_GOLD_HOI_SINH_NRSD = 20000000;
    private static PlayerService i;

    public PlayerService() {
    }

    public static PlayerService gI() {
        if (i == null) {
            i = new PlayerService();
        }
        return i;
    }

    public void sendTNSM(Player player, byte type, double param) {
        if (param > 0) {
            Message msg;
            try {
                msg = new Message(-3);
                msg.writer().writeByte(type);// 0 là cộng sm, 1 cộng tn, 2 là cộng cả 2
                msg.writer().writeInt((int) param);// số tn cần cộng
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(PlayerService.class, e, "Error player: " + player.name);
            }
        }
    }

    public void sendMessageAllPlayer(Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendMessageIgnore(Player plIgnore, Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null && !pl.equals(plIgnore)) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendInfoHp(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 5);
            msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.hp));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendInfoMp(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 6);
            msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.mp));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendInfoHpMp(Player player) {
        sendInfoHp(player);
        sendInfoMp(player);
    }

    public void hoiPhuc(Player player, double hp, double mp) {
        if (!player.isDie()) {
            player.nPoint.addHp(hp);
            player.nPoint.addMp(mp);
            Service.getInstance().Send_Info_NV(player);
            if (!player.isPet && !player.isNewPet && !player.isTrieuHoiPet) {
                PlayerService.gI().sendInfoHpMp(player);
            }
        }
    }

    public void sendInfoHpMpMoney(Player player) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 4);
            try {
                if (!player.isBoss) {
                    if (player.getSession().version >= 214) {
                        msg.writer().writeLong(player.inventory.gold);
                    } else {
                        msg.writer().writeInt((int) player.inventory.gold);
                    }
                }
            } catch (Exception e) {
                msg.writer().writeInt((int) player.inventory.gold);
            }
            msg.writer().writeInt(player.inventory.gem);//luong
            msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.hp));//chp
            msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.mp));//cmp
            msg.writer().writeInt(player.inventory.ruby);//ruby
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void playerMove(Player player, int x, int y) {
        if (player.zone == null) {
            return;
        }
        if (!player.isDie()) {
            if (player.effectSkill.isCharging) {
                EffectSkillService.gI().stopCharge(player);
            }
            if (player.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(player);
            }
            player.location.x = x;
            player.location.y = y;
            player.location.lastTimeplayerMove = System.currentTimeMillis();
            switch (player.zone.map.mapId) {
                case 85, 86, 87, 88, 89, 90, 91 -> {
                    if (!player.isBoss && !player.isPet && !player.isTrieuHoiPet) {
                        if (x < 24 || x > player.zone.map.mapWidth - 24 || y < 0 || y > player.zone.map.mapHeight - 24) {
                            if (MapService.gI().getWaypointPlayerIn(player) == null) {
                                ChangeMapService.gI().changeMap(player, 21 + player.gender, 0, 200, 336);
                                return;
                            }
                        }
                        int yTop = player.zone.map.yPhysicInTop(player.location.x, player.location.y);
                        if (yTop >= player.zone.map.mapHeight - 24) {
                            ChangeMapService.gI().changeMap(player, 21 + player.gender, 0, 200, 336);
                            return;
                        }
                    }
                }
            }
            if (player.pet != null) {
                player.pet.followMaster();
            }
            if (player.newpet != null) {
                player.newpet.followMaster();
            }
            if (player.thuTrieuHoi != null) {
                player.thuTrieuHoi.followMaster();
            }
            MapService.gI().sendPlayerMove(player);
            TaskService.gI().checkDoneTaskGoToMap(player, player.zone);
        }
    }

    public void sendCurrentStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-68);
            msg.writer().writeShort(player.nPoint.stamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendMaxStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-69);
            msg.writer().writeShort(player.nPoint.maxStamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void changeAndSendTypePK(Player player, int type) {
        changeTypePK(player, type);
        sendTypePk(player);
    }

    public void changeTypePK(Player player, int type) {
        player.typePk = (byte) type;
    }

    public void sendTypePk(Player player) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(player.typePk);
            Service.getInstance().sendMessAllPlayerInMap(player.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e, "Error player: " + player.name);
        }
    }

    public void banPlayer(Player playerBaned) {
        try {
            GirlkunDB.executeUpdate("update account set ban = 1 where id = ? and username = ?",
                    playerBaned.getSession().userId, playerBaned.getSession().uu);
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e, "Error player: " + playerBaned.name);
        }
        Service.getInstance().sendThongBao(playerBaned,
                "Tài khoản của bạn đã bị khóa\nGame sẽ mất kết nối sau 5 giây...");
        playerBaned.iDMark.setLastTimeBan(System.currentTimeMillis());
        playerBaned.iDMark.setBan(true);
    }


    public void hoiSinh(Player player) {
        if (player.isDie()) {
            if (MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH_NRSD) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH_NRSD;
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH_NRSD
                            - player.inventory.gold) + " vàng");
                    return;
                }
            } else {
                if (player.inventory.gem >= COST_GEM_HOI_SINH) {
                    player.inventory.gem -= COST_GEM_HOI_SINH;
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GEM_HOI_SINH
                            - player.inventory.gem) + " vàng");
                    return;
                }
            }
            Service.getInstance().sendMoney(player);
            Service.getInstance().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
//            player.achievement.plusCount(13);
        }
    }

    public void hoiSinhMaBu(Player player) {
        if (player.isDie()) {
            if (MapService.gI().isMapMaBu(player.zone.map.mapId)) {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH_NRSD) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH_NRSD;
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH_NRSD
                            - player.inventory.gold) + " vàng");
                    return;
                }
            } else {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH;
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH
                            - player.inventory.gold) + " vàng");
                    return;
                }
            }
            Service.getInstance().sendMoney(player);
            Service.getInstance().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
        }
    }

}
