package com.girlkun.models.map.blackball;

import com.girlkun.models.item.Item;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.player.Player;
import com.girlkun.services.MapService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.Date;
import java.util.List;


public class BlackBallWar {

    private static final int TIME_CAN_PICK_BLACK_BALL_AFTER_DROP = 5000;

    public static final byte X3 = 3;
    public static final byte X5 = 5;
    public static final byte X7 = 7;

    public static final int COST_X3 = 100000000;
    public static final int COST_X5 = 300000000;
    public static final int COST_X7 = 500000000;

    public static final byte HOUR_OPEN = 20;
    public static final byte MIN_OPEN = 0;
    public static final byte SECOND_OPEN = 0;

    public static final byte HOUR_CAN_PICK_DB = 20;
    public static final byte MIN_CAN_PICK_DB = 30;
    public static final byte SECOND_CAN_PICK_DB = 0;

    public static final byte HOUR_CLOSE = 21;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 0;
    
//    public static final byte HOUR_RS = 21;
//    public static final byte MIN_RS = 0;
//    public static final byte SECOND_RS = 1;

    //    public static final byte HOUR_OPEN = 20;
//    public static final byte MIN_OPEN = 0;
//    public static final byte SECOND_OPEN = 0;
//
//    public static final byte HOUR_CAN_PICK_DB = 1;
//    public static final byte MIN_CAN_PICK_DB = 30;
//    public static final byte SECOND_CAN_PICK_DB = 0;
//
//    public static final byte HOUR_CLOSE = 21;
//    public static final byte MIN_CLOSE = 0;
//    public static final byte SECOND_CLOSE = 0;
    //*************************************
    public static final int AVAILABLE = 7;
    private static final int TIME_WIN = 300000; //cam toi thieu 300k mili = 5p

    private static BlackBallWar i;

    public static long TIME_OPEN;
    public static long TIME_CAN_PICK_DB;
    public static long TIME_CLOSE;
//    public static long TIME_RS;

    private int day = -1;
    public long lastTimeReinit;
    public boolean isReinit;

    public static BlackBallWar gI() {
        if (i == null) {
            i = new BlackBallWar();
        }
        i.setTime();
        return i;
    }

    public void setTime() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                this.TIME_OPEN = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_OPEN + ":" + MIN_OPEN + ":" + SECOND_OPEN, "dd/MM/yyyy HH:mm:ss");
                this.TIME_CAN_PICK_DB = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CAN_PICK_DB + ":" + MIN_CAN_PICK_DB + ":" + SECOND_CAN_PICK_DB, "dd/MM/yyyy HH:mm:ss");
                this.TIME_CLOSE = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CLOSE + ":" + MIN_CLOSE + ":" + SECOND_CLOSE, "dd/MM/yyyy HH:mm:ss");
//                this.TIME_RS = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_RS + ":" + MIN_RS + ":" + SECOND_RS, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {
                System.out.println("loi ne nrsd 1 ");
            }
        }
    }
//    public void reInitNrd() {
//        lastTimeReinit = TIME_CAN_PICK_DB;
//        isReinit = true;
//    }

    public synchronized void dropBlackBall(Player player) {
        if (player.iDMark.isHoldBlackBall()) {
            player.iDMark.setHoldBlackBall(false);
            ItemMap itemMap = new ItemMap(player.zone,
                    player.iDMark.getTempIdBlackBallHold(), 1, player.location.x,
                    player.zone.map.yPhysicInTop(player.location.x, player.location.y - 24),
                    -1);
            Service.gI().dropItemMap(itemMap.zone, itemMap);
            player.iDMark.setTempIdBlackBallHold(-1);
            player.zone.lastTimeDropBlackBall = System.currentTimeMillis();
            Service.gI().sendFlagBag(player); //gui vao tui do

            if (player.clan != null) {
                List<Player> players = player.zone.getPlayers();
                for (Player pl : players) {
                    if (pl.clan != null && player.clan.equals(pl.clan)) {
                        Service.gI().changeFlag(pl, Util.nextInt(1, 7));
                    }
                }
            } else {
                Service.gI().changeFlag(player, Util.nextInt(1, 7));
            }
        }
    }

    public void update(Player player) {
        if (player.zone == null || !MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
            return;
        }
        if (player.iDMark.isHoldBlackBall()) {
            if (Util.canDoWithTime(player.iDMark.getLastTimeHoldBlackBall(), TIME_WIN)) {
                win(player);
                return;
            } else {
                if (Util.canDoWithTime(player.iDMark.getLastTimeNotifyTimeHoldBlackBall(), 10000)) {
                    Service.gI().sendThongBao(player, "Cố gắng giữ ngọc rồng trong "
                            + TimeUtil.getSecondLeft(player.iDMark.getLastTimeHoldBlackBall(), TIME_WIN / 1000)
                            + " giây nữa, đem chiến thắng về cho bang hội!");
                    player.iDMark.setLastTimeNotifyTimeHoldBlackBall(System.currentTimeMillis());
                }
            }
        }
        try {
            long now = System.currentTimeMillis();
            if (!(now > TIME_OPEN && now < TIME_CLOSE)) {
                if (player.iDMark.isHoldBlackBall()) {
                    win(player);
                } else {
                    kickOutOfMap(player);
                }
            }
//            if ((now > TIME_CLOSE && now < TIME_RS)) {
//                lastTimeReinit += 86400000;
//                Thread.sleep(1000);
//            }
        } catch (Exception ex) {
                System.out.println("        loi update map blackball");
        }
    }

    private void win(Player player) {
        player.zone.finishBlackBallWar = true;
        int star = player.iDMark.getTempIdBlackBallHold() - 371;
        if (player.clan != null) {
            try {
                List<Player> players = player.clan.membersInGame;
                for (Player pl : players) {
                    if (pl != null) {
                        pl.rewardBlackBall.reward((byte) star);
                        Service.gI().sendThongBao(pl, "Chúc mừng bang hội của bạn đã "
                                + "dành chiến thắng ngọc rồng sao đen " + star + " sao");
                    }
                }

            } catch (Exception e) {
                Logger.logException(BlackBallWar.class, e,
                        "Lỗi ban thưởng ngọc rồng đen "
                                + star + " sao cho clan " + player.clan.id);
            }
        } else {
            player.rewardBlackBall.reward((byte) star);
            Service.gI().sendThongBao(player, "Chúc mừng bang hội của bạn đã "
                    + "dành chiến thắng ngọc rồng sao đen " + star + " sao");
        }

        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfMap(pl);
        }
    }

    private void kickOutOfMap(Player player) {
        if (player.cFlag == 8) {
            Service.gI().changeFlag(player, Util.nextInt(1, 7));
        }
        Service.gI().sendThongBao(player, "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
    }

    public void changeMap(Player player, byte index) {
        try {
            long now = System.currentTimeMillis();
            if (now > TIME_OPEN && now < TIME_CLOSE) {
                ChangeMapService.gI().changeMap(player,
                        player.mapBlackBall.get(index).map.mapId, -1, 50, 50);
            } else {
                Service.gI().sendThongBao(player, "Đại chiến ngọc rồng đen chưa mở");
                Service.gI().hideWaitDialog(player);
            }
        } catch (Exception ex) {
                System.out.println("loi ne nrsd 2 ");
        }
    }

    public void joinMapBlackBallWar(Player player) {
        boolean changed = false;
        if (player.clan != null) {
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
                if (pl.clan != null && !player.equals(pl) && player.clan.equals(pl.clan) && !player.isBoss) {
                    Service.gI().changeFlag(player, pl.cFlag);
                    changed = true;
                    break;
                }
            }
        }
        if (!changed && !player.isBoss) {
            Service.gI().changeFlag(player, Util.nextInt(1, 7));
        }
    }

    public boolean pickBlackBall(Player player, Item item) {
        try {
            if (System.currentTimeMillis() < this.TIME_CAN_PICK_DB) {
                Service.gI().sendThongBao(player, "Chưa thể nhặt ngọc rồng ngay lúc này, vui lòng đợi "
                        + TimeUtil.diffDate(new Date(this.TIME_CAN_PICK_DB),
                        new Date(System.currentTimeMillis()), TimeUtil.SECOND) + " giây nữa");
                return false;
            } else if (player.zone.finishBlackBallWar) {
                Service.gI().sendThongBao(player, "Đại chiến ngọc rồng sao đen "
                        + "đã kết thúc, vui lòng đợi đến ngày mai");
                return false;
            } else {
                if (Util.canDoWithTime(player.zone.lastTimeDropBlackBall, TIME_CAN_PICK_BLACK_BALL_AFTER_DROP)) {

                    player.iDMark.setHoldBlackBall(true);
                    player.iDMark.setTempIdBlackBallHold(item.template.id);
                    player.iDMark.setLastTimeHoldBlackBall(System.currentTimeMillis());
                    Service.gI().sendFlagBag(player);
                    if (player.clan != null) {
                        List<Player> players = player.zone.getPlayers();
                        for (Player pl : players) {
                            if (pl.clan != null && player.clan.equals(pl.clan)) {
                                Service.gI().changeFlag(pl, 8);
                            }
                        }
                    } else {
                        Service.gI().changeFlag(player, 8);
                    }
                    return true;
                } else {
                    Service.gI().sendThongBao(player, "Không thể nhặt ngọc rồng đen ngay lúc này");
                    return false;
                }
            }
        } catch (Exception ex) {
                System.out.println("loi ne nrsd 3 ");
            return false;
        }
    }

    public void xHPKI(Player player, byte x) {
        int cost = 0;
        switch (x) {
            case X3:
                cost = COST_X3;
                break;
            case X5:
                cost = COST_X5;
                break;
            case X7:
                cost = COST_X7;
                break;
        }
        if (player.inventory.gold >= cost) {
            player.inventory.gold -= cost;
            Service.gI().sendMoney(player);
            player.effectSkin.lastTimeXHPKI = System.currentTimeMillis();
            player.effectSkin.xHPKI = x;
            player.nPoint.calPoint();
            player.nPoint.setHp((double) player.nPoint.hp * x);
            player.nPoint.setMp((double) player.nPoint.mp * x);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().point(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu "
                    + Util.numberToMoney(cost - player.inventory.gold) + " vàng");
        }
    }
}
