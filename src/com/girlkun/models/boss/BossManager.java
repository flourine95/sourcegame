package com.girlkun.models.boss;

import com.girlkun.models.boss.list_boss.*;
import com.girlkun.models.boss.list_boss.BLACK.*;
import com.girlkun.models.boss.list_boss.Broly.Broly;
import com.girlkun.models.boss.list_boss.Broly.BrolyA;
import com.girlkun.models.boss.list_boss.Broly.BrolyB;
import com.girlkun.models.boss.list_boss.Cooler.Cooler;
import com.girlkun.models.boss.list_boss.Doraemon.*;
import com.girlkun.models.boss.list_boss.Mabu12h.*;
import com.girlkun.models.boss.list_boss.NRD.*;
import com.girlkun.models.boss.list_boss.android.*;
import com.girlkun.models.boss.list_boss.cell.SieuBoHung;
import com.girlkun.models.boss.list_boss.cell.XenBoHung;
import com.girlkun.models.boss.list_boss.cell.Xencon;
import com.girlkun.models.boss.list_boss.doanh_trai.*;
import com.girlkun.models.boss.list_boss.fide.Fide;
import com.girlkun.models.boss.list_boss.ginyu.TDST;
import com.girlkun.models.boss.list_boss.nappa.Kuku;
import com.girlkun.models.boss.list_boss.nappa.MapDauDinh;
import com.girlkun.models.boss.list_boss.nappa.Rambo;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.ServerManager;
import com.girlkun.services.ItemMapService;
import com.girlkun.services.MapService;
import com.girlkun.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class BossManager implements Runnable {

    private static BossManager I;
    public static final byte ratioReward = 2;

    public static BossManager gI() {
        if (BossManager.I == null) {
            BossManager.I = new BossManager();
        }
        return BossManager.I;
    }

    private BossManager() {
        this.bosses = new ArrayList<>();
    }

    private boolean loadedBoss;
    private final List<Boss> bosses;

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public List<Boss> getBosses() {
        return this.bosses;
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public void loadBoss() {
        if (this.loadedBoss) {
            return;
        }
        try {
//            Thread.sleep(10000);
            this.createBoss(BossID.TDST);
            this.createBoss(BossID.BROLY);
            this.createBoss(BossID.BROLYA);
            this.createBoss(BossID.BROLYB);
            this.createBoss(BossID.PIC);
            this.createBoss(BossID.POC);
            this.createBoss(BossID.KING_KONG);
            this.createBoss(BossID.SONGOKU_TA_AC);
            this.createBoss(BossID.CUMBER);
            this.createBoss(BossID.COOLER_GOLD);
            this.createBoss(BossID.XEN_BO_HUNG);
            this.createBoss(BossID.SIEU_BO_HUNG);
            this.createBoss(BossID.XEN_CON_1);
            this.createBoss(BossID.DORAEMON);
            this.createBoss(BossID.NOBITA);
            this.createBoss(BossID.XUKA);
            this.createBoss(BossID.CHAIEN);
            this.createBoss(BossID.XEKO);
            this.createBoss(BossID.BLACK);
            this.createBoss(BossID.ZAMASZIN);
            this.createBoss(BossID.BLACK2);
//            this.createBoss(BossID.ZAMASMAX);
            this.createBoss(BossID.BLACK);
            this.createBoss(BossID.BLACK3);
            this.createBoss(BossID.KUKU);
            this.createBoss(BossID.MAP_DAU_DINH);
            this.createBoss(BossID.RAMBO);
            this.createBoss(BossID.FIDE);
            this.createBoss(BossID.DR_KORE);
            this.createBoss(BossID.ANDROID_14);
            this.createBoss(BossID.SUPER_ANDROID_17);
//            this.createBoss(BossID.MABU);
            this.createBoss(BossID.BOSS_DETU_BERUS);
            this.createBoss(BossID.BOSS_DETU_BL);
            this.createBoss(BossID.BOSS_ZENO);
            this.createBoss(BossID.COOLER);
            this.createBoss(BossID.BOSS_BOSSMOON);
            this.createBoss(BossID.BOSS_NRO1S1);
            this.createBoss(BossID.BOSS_NRO1S2);
            this.createBoss(BossID.BOSS_NRO1S3);
            this.createBoss(BossID.BOSS_NRO1S4);
            this.createBoss(BossID.AN_TROM);
//            this.createBoss(BossID.BOSS_THOTRANG);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("loi ne  33      ClassCastException ");
        }
        this.loadedBoss = true;
        new Thread(BossManager.I, "Update boss").start();
    }

    public Boss createBossDoanhTrai(Zone zone, int bossID, double dame, double hp) {
        try {
            return switch (bossID) {
                case BossID.TRUNG_UY_TRANG -> new TrungUyTrang(zone, dame, hp);
                case BossID.TRUNG_UY_XANH_LO -> new TrungUyXanhLo(zone, dame, hp);
                case BossID.TRUNG_UY_THEP -> new TrungUyThep(zone, dame, hp);
                case BossID.NINJA_AO_TIM -> new NinjaTim(zone, dame, hp);
                case BossID.ROBOT_VE_SI_1, BossID.ROBOT_VE_SI_2, BossID.ROBOT_VE_SI_3, BossID.ROBOT_VE_SI_4 ->
                        new RobotVeSi(zone, dame, hp, bossID);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {
//                case BossID.AN_TROM:
//                    return new AnTrom();
                case BossID.KUKU -> new Kuku();
                case BossID.MAP_DAU_DINH -> new MapDauDinh();
                case BossID.RAMBO -> new Rambo();
                case BossID.DRABURA -> new Drabura();
                case BossID.DRABURA_2 -> new Drabura2();
                case BossID.BUI_BUI -> new BuiBui();
                case BossID.BUI_BUI_2 -> new BuiBui2();
                case BossID.YA_CON -> new Yacon();
                case BossID.MABU_12H -> new MabuBoss();
                case BossID.Rong_1Sao -> new Rong1Sao();
                case BossID.Rong_2Sao -> new Rong2Sao();
                case BossID.Rong_3Sao -> new Rong3Sao();
                case BossID.Rong_4Sao -> new Rong4Sao();
                case BossID.Rong_5Sao -> new Rong5Sao();
                case BossID.Rong_6Sao -> new Rong6Sao();
                case BossID.Rong_7Sao -> new Rong7Sao();
                case BossID.FIDE -> new Fide();
                case BossID.DR_KORE -> new DrKore();
                case BossID.ANDROID_19 -> new Android19();
                case BossID.ANDROID_13 -> new Android13();
                case BossID.ANDROID_14 -> new Android14();
                case BossID.ANDROID_15 -> new Android15();
//                case BossID.SUPER_ANDROID_17:
//                    return new SuperAndroid17();
//                case BossID.BOSS_CHIENTHAN:
//                    return new BossChienThan();
                case BossID.BOSS_DETU_BERUS -> new BossDetuBerus();
                case BossID.BOSS_BOSSMOON -> new BossMoon();
                case BossID.BOSS_NRO1S1 -> new Boss1S1();
                case BossID.BOSS_NRO1S2 -> new Boss1S2();
                case BossID.BOSS_NRO1S3 -> new Boss1S3();
                case BossID.BOSS_NRO1S4 -> new Boss1S4();
                case BossID.BOSS_DETU_BL -> new BossDetuBroly();

//                case BossID.BOSS_THOTRANG:
//                    return new ThoTrang();
                case BossID.PIC -> new Pic();
                case BossID.POC -> new Poc();
                case BossID.KING_KONG -> new KingKong();
                case BossID.XEN_BO_HUNG -> new XenBoHung();
                case BossID.SIEU_BO_HUNG -> new SieuBoHung();
                case BossID.XUKA -> new Xuka();
                case BossID.NOBITA -> new Nobita();
                case BossID.XEKO -> new Xeko();
                case BossID.CHAIEN -> new Chaien();
                case BossID.DORAEMON -> new Doraemon();
//                case BossID.VUA_COLD:
//                    return new Kingcold();
//                case BossID.FIDE_ROBOT:
//                    return new FideRobot();
                case BossID.COOLER -> new Cooler();
//                case BossID.ZAMASMAX:
//                    return new ZamasMax();
                case BossID.ZAMASZIN -> new ZamasKaio();
                case BossID.BLACK2 -> new SuperBlack2();
                case BossID.BLACK1 -> new BlackGokuTl();
                case BossID.BLACK -> new Black();
//                 case BossID.BLACK3:
//                    return new BlackGokuBase();
                case BossID.SUPER_BLACK_GOKU -> new BlackGokuBase();
                case BossID.XEN_CON_1 -> new Xencon();
//                case BossID.MABU:
//                    return new Mabu();
                case BossID.TDST -> new TDST();
//                case BossID.SONGOKU_TA_AC:
//                    return new SongokuTaAc();
                case BossID.BROLY -> new Broly();
                case BossID.BROLYA -> new BrolyA();
                case BossID.BROLYB -> new BrolyB();
                default -> null;
            };
        } catch (Exception e) {
//                System.out.println("        loi boss");
            return null;
        }
    }

    public boolean existBossOnPlayer(Player player) {
        return !player.zone.getBosses().isEmpty();
    }


    public void showListBoss(Player player) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh sách Boss");
            msg.writer().writeByte((int) bosses.stream().filter(boss ->
                    !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapDoanhTrai(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapBanDoKhoBau(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapKhiGas(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapNhanBan(boss.data[0].getMapJoin()[0])
                            && !(boss instanceof MiNuong)
                            && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])).count()
            );
            for (Boss boss : bosses) {
                if (MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                        || boss instanceof MiNuong
                        || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapBanDoKhoBau(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapKhiGas(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapNhanBan(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapDoanhTrai(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF("Trạng thái: Sống");
                    if (player.isAdmin()) {
                        msg.writer().writeUTF("Vị trí: " + boss.zone.map.mapName + " (" + boss.zone.map.mapId + ")" + " khu " + boss.zone.zoneId);
                    } else {
                        msg.writer().writeUTF("Vị trí: " + boss.zone.map.mapName);
                    }
                } else {
                    msg.writer().writeUTF("Trạng thái: Chết");
                    msg.writer().writeUTF("Vị trí: có thông tin !");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(BossManager.class, e, "Error BossManager");
        }
    }

    public void doBossMember(Player player) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            msg.writer().writeByte((int) bosses.stream().filter(boss -> !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapDoanhTrai(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapTienMon(boss.data[0].getMapJoin()[0])
                    && !(boss instanceof MiNuong)
                    && !(boss instanceof AnTrom)
                    && !MapService.gI().isMapBanDoKhoBau(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapKhiGas(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapNhanBan(boss.data[0].getMapJoin()[0])
                    && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])).count());
            for (Boss boss : bosses) {
                if (MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                        || boss instanceof MiNuong
                        || boss instanceof AnTrom
                        || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapDoanhTrai(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapBanDoKhoBau(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapKhiGas(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapNhanBan(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapTienMon(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF("Sống");
                    msg.writer().writeUTF("Dịch chuyển");
                } else {
                    msg.writer().writeUTF("Chết");
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("iii");
        }
    }


    public synchronized void callBoss(Player player, int mapId) {
        try {
            if (BossManager.gI().existBossOnPlayer(player) ||
                    player.zone.items.stream().anyMatch(itemMap -> ItemMapService.gI().isBlackBall(itemMap.itemTemplate.id)) ||
                    player.zone.getPlayers().stream().anyMatch(p -> p.iDMark.isHoldBlackBall())) {
                return;
            }
            Boss k = switch (mapId) {
                case 85 -> BossManager.gI().createBoss(BossID.Rong_1Sao);
                case 86 -> BossManager.gI().createBoss(BossID.Rong_2Sao);
                case 87 -> BossManager.gI().createBoss(BossID.Rong_3Sao);
                case 88 -> BossManager.gI().createBoss(BossID.Rong_4Sao);
                case 89 -> BossManager.gI().createBoss(BossID.Rong_5Sao);
                case 90 -> BossManager.gI().createBoss(BossID.Rong_6Sao);
                case 91 -> BossManager.gI().createBoss(BossID.Rong_7Sao);
                default -> null;
            };
            if (k != null) {
                k.currentLevel = 0;
                k.joinMapByZone(player);
            }
        } catch (Exception e) {
            System.out.println("ooo");
        }
    }

    public Boss getBossById(int bossId) {
        return BossManager.gI().bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(150 - (System.currentTimeMillis() - st), 0));
                    for (Boss boss : this.bosses) {
                        boss.update();
                    }
                }
            } catch (Exception e) {
                Logger.logException(BossManager.class, e, "Boss error");
            }
        }
    }
}

