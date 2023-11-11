package com.girlkun.models.player;

import com.girlkun.models.item.Item;
import com.girlkun.models.mob.Mob;
import com.girlkun.services.*;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class EffectSkin {

    private static final String[] textOdo = new String[]{
            "Hôi quá", "Tránh ra đi thằng ở dơ", "Mùi gì kinh quá vậy?",
            "Kinh tởm quá", "Biến đi thằng ở dơ", "Kính ngài ở dơ"
    };

    private Player player;

    public EffectSkin(Player player) {
        this.player = player;
        this.xHPKI = 1;
    }

    public long lastTimeAttack;
    private long lastTimeOdo;
    private long lastTimeXenHutHpKi;
    private long lastTimeHP30s;
    private long lastTimeMP30s;

    public long lastTimeAddTimeTrainArmor;
    public long lastTimeSubTimeTrainArmor;

    public boolean isVoHinh;

    public long lastTimeXHPKI;
    public int xHPKI;

    public long lastTimeUpdateCTHT;

    public void update() {
        updateVoHinh();
        updateHoihp30s();
        updateHoimp30s();
        if (this.player.zone != null && !MapService.gI().isMapOffline(this.player.zone.map.mapId)) {
            updateOdo();
            updateXenHutXungQuanh();
        }
        if (!this.player.isBoss && !this.player.isPet && !player.isNewPet && !this.player.isTrieuHoiPet) {
            updateTrainArmor();
        }
        if (xHPKI != 1 && Util.canDoWithTime(lastTimeXHPKI, 1800000)) {
            xHPKI = 1;
            Service.getInstance().point(player);
        }
        updateCTHaiTac();
    }

    private void updateCTHaiTac() {
        if (this.player.setClothes.ctHaiTac != -1
                && this.player.zone != null
                && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
            int count = 0;
            int[] cts = new int[9];
            cts[this.player.setClothes.ctHaiTac - 618] = this.player.setClothes.ctHaiTac;
            List<Player> players = new ArrayList<>();
            players.add(player);
            try {
                for (Player pl : player.zone.getNotBosses()) {
                    if (!player.equals(pl) && pl.setClothes.ctHaiTac != -1 && Util.getDistance(player, pl) <= 300) {
                        cts[pl.setClothes.ctHaiTac - 618] = pl.setClothes.ctHaiTac;
                        players.add(pl);
                    }
                }
            } catch (Exception e) {
                Logger.logException(EffectSkin.class, e);
            }
            for (int j : cts) {
                if (j != 0) {
                    count++;
                }
            }
            for (Player pl : players) {
                Item ct = pl.inventory.itemsBody.get(5);
                if (ct.isNotNullItem() && ct.template.id >= 618 && ct.template.id <= 626) {
                    for (Item.ItemOption io : ct.itemOptions) {
                        if (io.optionTemplate.id == 147
                                || io.optionTemplate.id == 77
                                || io.optionTemplate.id == 103) {
                            io.param = count * 3;
                        }
                    }
                }
                if (!pl.isPet && !pl.isNewPet && !pl.isTrieuHoiPet && Util.canDoWithTime(lastTimeUpdateCTHT, 5000)) {
                    InventoryServiceNew.gI().sendItemBody(pl);
                }
                pl.effectSkin.lastTimeUpdateCTHT = System.currentTimeMillis();
            }
        }
    }

    private void updateHoihp30s() {
        try {
            int param = this.player.nPoint.hpHoiAdd;
            if (param > 0) {
                if (!this.player.isDie() && Util.canDoWithTime(lastTimeHP30s, 30000)) {
                    double hpHoi = this.player.nPoint.hpMax * param / 100;
                    this.player.nPoint.addHp(hpHoi);
                    PlayerService.gI().sendInfoHpMpMoney(this.player);
                    Service.getInstance().Send_Info_NV(this.player);
                    this.lastTimeHP30s = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            Logger.error("Lỗi hồi phục HP/30s");
        }
    }

    private void updateHoimp30s() {
        try {
            int param = this.player.nPoint.mpHoiAdd;
            if (param > 0) {
                if (!this.player.isDie() && Util.canDoWithTime(lastTimeMP30s, 30000)) {
                    double mpHoi = this.player.nPoint.mpMax * param / 100;
                    this.player.nPoint.addMp(mpHoi);
                    PlayerService.gI().sendInfoHpMpMoney(this.player);
                    Service.getInstance().Send_Info_NV(this.player);
                    this.lastTimeMP30s = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            Logger.error("Lỗi hồi phục MP/30s");
        }
    }

    private void updateXenHutXungQuanh() {
        try {
            int param = this.player.nPoint.tlHutHpMpXQ;
            if (param > 0) {
                if (!this.player.isDie() && Util.canDoWithTime(lastTimeXenHutHpKi, 5000)) {
                    int hpHut = 0;
                    int mpHut = 0;
                    List<Player> players = new ArrayList<>();
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (Player pl : playersMap) {
                        if (!this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            players.add(pl);
                        }
                    }
                    for (Mob mob : this.player.zone.mobs) {
                        if (mob.point.gethp() > 1) {
                            if (Util.getDistance(this.player, mob) <= 200) {
                                double subHp = (double) (Util.DoubleGioihan(mob.point.getHpFull()) * param) / 100;
                                if (subHp >= mob.point.gethp()) {
                                    subHp = mob.point.gethp() - 1;
                                }
                                hpHut += (int) subHp;
                                mob.injured(null, subHp, false);
                            }
                        }
                    }
                    for (Player pl : players) {
                        long subHp = Util.DoubleGioihan(pl.nPoint.hpMax * param / 100);
                        long subMp = Util.DoubleGioihan(pl.nPoint.mpMax * param / 100);
                        if (subHp >= pl.nPoint.hp) {
                            subHp = Util.DoubleGioihan(pl.nPoint.hp - 1);
                        }
                        if (subMp >= pl.nPoint.mp) {
                            subMp = Util.DoubleGioihan(pl.nPoint.mp - 1);
                        }
                        hpHut += (int) subHp;
                        mpHut += (int) subMp;
                        PlayerService.gI().sendInfoHpMpMoney(pl);
                        Service.getInstance().Send_Info_NV(pl);
                        pl.injured(null, subHp, true, false);
                    }
                    this.player.nPoint.addHp(hpHut);
                    this.player.nPoint.addMp(mpHut);
                    PlayerService.gI().sendInfoHpMpMoney(this.player);
                    Service.getInstance().Send_Info_NV(this.player);
                    this.lastTimeXenHutHpKi = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            Logger.error("");
        }
    }

    private void updateOdo() {
        try {
            int param = this.player.nPoint.tlHpGiamODo;
            if (param > 0) {
                if (Util.canDoWithTime(lastTimeOdo, 10000)) {
                    List<Player> players = new ArrayList<>();
                    List<Player> playersMap = this.player.zone.getNotBosses();
                    for (Player pl : playersMap) {
                        if (!this.player.equals(pl) && !pl.isBoss && !pl.isDie()
                                && Util.getDistance(this.player, pl) <= 200) {
                            players.add(pl);
                        }
                    }
                    for (Player pl : players) {
                        long subHp = Util.DoubleGioihan(pl.nPoint.hpMax * param / 100);
                        if (subHp >= pl.nPoint.hp) {
                            subHp = Util.DoubleGioihan(pl.nPoint.hp - 1);
                        }
                        Service.getInstance().chat(pl, textOdo[Util.nextInt(0, textOdo.length - 1)]);
                        PlayerService.gI().sendInfoHpMpMoney(pl);
                        Service.getInstance().Send_Info_NV(pl);
                        pl.injured(null, subHp, true, false);
                    }
                    this.lastTimeOdo = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            Logger.error("");
        }
    }

    //giáp tập luyện
    private void updateTrainArmor() {
        if (Util.canDoWithTime(lastTimeAddTimeTrainArmor, 60000) && !Util.canDoWithTime(lastTimeAttack, 30000)) {
            if (this.player.nPoint.wearingTrainArmor) {
                for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                    if (io.optionTemplate.id == 9) {
                        if (io.param < 1000) {
                            io.param++;
                            InventoryServiceNew.gI().sendItemBody(player);
                        }
                        break;
                    }
                }
            }
            this.lastTimeAddTimeTrainArmor = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeSubTimeTrainArmor, 60000)) {
            for (Item item : this.player.inventory.itemsBag) {
                if (item.isNotNullItem()) {
                    if (ItemService.gI().isTrainArmor(item)) {
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 9) {
                                if (io.param > 0) {
                                    io.param--;
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            for (Item item : this.player.inventory.itemsBox) {
                if (item.isNotNullItem()) {
                    if (ItemService.gI().isTrainArmor(item)) {
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 9) {
                                if (io.param > 0) {
                                    io.param--;
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            this.lastTimeSubTimeTrainArmor = System.currentTimeMillis();
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().point(this.player);
        }
    }

    private void updateVoHinh() {
        if (this.player.nPoint.wearingVoHinh) {
            isVoHinh = Util.canDoWithTime(lastTimeAttack, 5000);
        }
    }

    public void dispose() {
        this.player = null;
    }
}
