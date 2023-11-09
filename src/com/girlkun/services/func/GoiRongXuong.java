package com.girlkun.services.func;

import com.girlkun.consts.ConstNpc;
import com.girlkun.models.item.Item;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.*;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @Stole By Hoàng Việt💖
 *
 */
public class GoiRongXuong {

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_HALLOWEN = 0;

    public static final short XUONG_1_SAO = 702;
    public static final short XUONG_2_SAO = 703;
    public static final short XUONG_3_SAO = 704;
    public static final short XUONG_4_SAO = 705;
    public static final short XUONG_5_SAO = 706;
    public static final short XUONG_6_SAO = 707;
    public static final short XUONG_7_SAO = 708;

    public static final String RONG_XUONG_TUTORIAL
            = "Chỉ được gọi Rồng Xương 1 sao\n"
            + "Các ngọc 2 sao đến 7 sao không thể gọi rồng thần được\n"
            + "Để gọi Rồng Xương 1 sao cần ngọc từ 1 sao đến 7 sao\n"
            + "Điều ước rồng 1 sao: 20k Hồng ngọc ; Buff 30% Chỉ số 30p; 200 Thỏi vàng ; Đổi skill 4 Đệ\n"
            + "Ngọc Bí Ngô sẽ mất ngay khi gọi rồng dù bạn có ước hay không\n"
            + "Quá 5 phút nếu không ước Rồng Xương sẽ bay mất";
    public static final String HALLOWEN_SAY
            = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định";

    public static final String[] HALLOWEN_1_STAR_WISHES_1
            = new String[]{"Giàu có\n20k Hồng ngọc", "Tăng 20% HP,KI,SD\ntrong 30 Phút", "200 Thỏi vàng",
                "Thay\nChiêu 4\nĐệ tử"};
    //--------------------------------------------------------------------------
    private static GoiRongXuong instance;
    private final Map pl_dragonStar;
    public long lastTimeXuongAppeared;
    private long lastTimeRongxuongWait;
    public final int timeReRongXuong = 10000;
    public boolean isRongxuongAppear = false;
    private final int timeRongxuongWait = 150000;

    private final Thread update;
    private boolean active;

    public boolean isPlayerDisconnect;
    public Player playerRongXuong;
    private int playerRongXuongId;
    public Zone mapRongxuongAppear;
    private byte RongxuongStar;
    private int menuRongxuong;
    private byte select;

    private GoiRongXuong() {
        this.pl_dragonStar = new HashMap<>();
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isRongxuongAppear == true) {
                        if (isPlayerDisconnect) {
                            List<Player> players = mapRongxuongAppear.getPlayers();
                            for (Player plMap : players) {
                                if (plMap.id == playerRongXuongId) {
                                    playerRongXuong = plMap;
                                    reSummonRongXuong();
                                    isPlayerDisconnect = false;
                                    break;
                                }
                            }
                        }
                        if (Util.canDoWithTime(lastTimeRongxuongWait, timeRongxuongWait)) {
                            RongxuongLeave(playerRongXuong, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Logger.logException(GoiRongXuong.class, e);
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public static GoiRongXuong gI() {
        if (instance == null) {
            instance = new GoiRongXuong();
        }
        return instance;
    }

    public void openMenuRongXuong(Player pl, byte dragonBallStar) {
        this.pl_dragonStar.put(pl, dragonBallStar);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.RONG_HALLOWEN, -1, "Bạn muốn gọi Rồng Xương ?",
                "Hướng\ndẫn thêm\n(mới)", "Gọi\nRồng Xương\n" + dragonBallStar + " Sao");
    }

    public synchronized void summonRongxuong(Player pl) {
        if (pl.zone.map.mapId == 5) {
            if (checkRongxuongBall(pl)) {
                if (isRongxuongAppear == true) {
                    Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }
                long currentTimeMillis = System.currentTimeMillis();
                long timeSinceLastRongxuongAppeared = currentTimeMillis - lastTimeXuongAppeared;
                long timeLeftUntilResummon = timeReRongXuong - timeSinceLastRongxuongAppeared;

                if (timeSinceLastRongxuongAppeared < timeReRongXuong) {
                    int timeLeftInSeconds = (int) (timeLeftUntilResummon / 1000);
                    String timeLeftString = (timeLeftInSeconds < 7200) ? (timeLeftInSeconds + " giây") : ((timeLeftInSeconds / 60) + " phút");
                    Service.getInstance().sendThongBao(pl, "Vui lòng đợi " + timeLeftString + " để gọi rồng");
                    return;
                }

                // Summon Rongxuong
                isRongxuongAppear = true;
                playerRongXuong = pl;
                playerRongXuongId = (int) pl.id;
                mapRongxuongAppear = pl.zone;
                int begin = XUONG_1_SAO;
                for (int i = begin; i <= XUONG_7_SAO; i++) {
                    try {
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, InventoryServiceNew.gI().findItemBag(pl, i), 1);
                    } catch (Exception ex) {
                        // Ignore exception
                    }
                }
                InventoryServiceNew.gI().sendItemBags(pl);
                sendNotifyRongxuongAppear();
                activeRongXuong(pl, true);
                sendWhishesRongxuong(pl);
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Chỉ được gọi Rồng Xương ở Đảo Kame");
        }
    }

    private void sendWhishesRongxuong(Player pl) {
        byte dragonStar;
        try {
            dragonStar = (byte) pl_dragonStar.get(pl);
            this.RongxuongStar = dragonStar;
        } catch (Exception e) {
            dragonStar = this.RongxuongStar;
        }
        switch (dragonStar) {
            case 1:
                NpcService.gI().createMenuRongXuong(pl, ConstNpc.HALLOWEN_1_1, HALLOWEN_SAY, HALLOWEN_1_STAR_WISHES_1);
                break;
        }
    }

    private void activeRongXuong(Player pl, boolean appear) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(0);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(1);
                lastTimeRongxuongWait = System.currentTimeMillis();
            }
            Service.getInstance().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private void reSummonRongXuong() {
        activeRongXuong(playerRongXuong, true);
        sendWhishesRongxuong(playerRongXuong);
    }

    public boolean checkRongxuongBall(Player pl) {
        byte dragonStar = (byte) this.pl_dragonStar.get(pl);
        if (dragonStar == 1) {
            if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_2_SAO)) {
                Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 2 sao");
                return false;
            }
            if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_3_SAO)) {
                Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 3 sao");
                return false;
            }
        } else if (dragonStar == 2) {
            if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_3_SAO)) {
                Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 3 sao");
                return false;
            }
        }
        if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_4_SAO)) {
            Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 4 sao");
            return false;
        }
        if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_5_SAO)) {
            Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 5 sao");
            return false;
        }
        if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_6_SAO)) {
            Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 6 sao");
            return false;
        }
        if (InventoryServiceNew.gI().checkNotItemBag(pl, XUONG_7_SAO)) {
            Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 Bí ngô 7 sao");
            return false;
        }
        return true;
    }

    private void sendNotifyRongxuongAppear() {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerRongXuong.name + " vừa gọi Rồng Xương tại "
                    + playerRongXuong.zone.map.mapName + " khu vực " + playerRongXuong.zone.zoneId);
            Service.getInstance().sendMessAllPlayerIgnoreMe(playerRongXuong, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void confirmWish() {
        switch (this.menuRongxuong) {
            case ConstNpc.HALLOWEN_1_1:
                switch (this.select) {
                    case 0: //20k hong ngoc
                        this.playerRongXuong.inventory.ruby += 20000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerRongXuong);
                        break;
                    case 1: //20% HP,KI,SD 30 phút
                        this.playerRongXuong.itemTime.lastTimeBiNgo = System.currentTimeMillis();
                        this.playerRongXuong.itemTime.isBiNgo = true;
                        Service.getInstance().point(this.playerRongXuong);
                        ItemTimeService.gI().sendAllItemTime(this.playerRongXuong);
                        break;
                    case 2: //100 Thỏi vàng
                        if (InventoryServiceNew.gI().getCountEmptyBag(playerRongXuong) != 0) {
                            Item thoivang = ItemService.gI().createNewItem((short) 457);
                            thoivang.quantity = 200;
                            InventoryServiceNew.gI().addItemBag(playerRongXuong, thoivang);
                            InventoryServiceNew.gI().sendItemBags(playerRongXuong);
                        } else {
                            Service.getInstance().sendThongBao(playerRongXuong, "Hành trang không đủ chổ trống");
                            reOpenRongxuongWishes(playerRongXuong);
                            return;
                        }
                        break;
                    case 3: //thay chiêu 2-3 đệ tử
                        if (playerRongXuong.pet.nPoint.power > 20000000000L) {
                            if (playerRongXuong.pet != null) {
                                if (playerRongXuong.pet.playerSkill.skills.get(2).skillId != -1) {
                                    playerRongXuong.pet.openSkill4();
                                    Service.getInstance().chatJustForMe(playerRongXuong, playerRongXuong.pet, "Cảm ơn sư phụ");
                                } else {
                                    Service.getInstance().sendThongBao(playerRongXuong, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                    reOpenRongxuongWishes(playerRongXuong);
                                    return;
                                }
                            } else {
                                Service.getInstance().sendThongBao(playerRongXuong, "Ngươi làm gì có đệ tử?");
                                reOpenRongxuongWishes(playerRongXuong);
                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(playerRongXuong, "Yêu cầu đệ tử có skill 4");
                            reOpenRongxuongWishes(playerRongXuong);
                            return;
                        }
                        break;
                }
                break;
        }
        RongxuongLeave(this.playerRongXuong, WISHED);
    }

    public void showConfirmRongxuong(Player pl, int menu, byte select) {
        this.menuRongxuong = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.HALLOWEN_1_1:
                wish = HALLOWEN_1_STAR_WISHES_1[select];
                break;
        }
        NpcService.gI().createMenuRongXuong(pl, ConstNpc.HALLOWEN_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void reOpenRongxuongWishes(Player pl) {
        switch (menuRongxuong) {
            case ConstNpc.HALLOWEN_1_1:
                NpcService.gI().createMenuRongXuong(pl, ConstNpc.HALLOWEN_1_1, HALLOWEN_SAY, HALLOWEN_1_STAR_WISHES_1);
                break;
        }
    }

    public void RongxuongLeave(Player pl, byte type) {
        if (type == WISHED) {
            NpcService.gI().createTutorial(pl, -1, "Điều ước của ngươi đã trở thành sự thật\nHẹn gặp ngươi lần sau, ta đi ngủ đây, bái bai");
        } else {
            NpcService.gI().createMenuRongXuong(pl, ConstNpc.IGNORE_MENU, "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
        }
        RongXuongBienMat(pl, false);
        this.isRongxuongAppear = false;
        this.menuRongxuong = -1;
        this.select = -1;
        this.playerRongXuong = null;
        this.playerRongXuongId = -1;
        this.RongxuongStar = -1;
        this.mapRongxuongAppear = null;
        lastTimeXuongAppeared = System.currentTimeMillis();
    }

    public void RongXuongBienMat(Player pl, boolean appear) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(0);
            }
            Service.getInstance().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    //--------------------------------------------------------------------------
}
