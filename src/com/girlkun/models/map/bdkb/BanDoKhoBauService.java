package com.girlkun.models.map.bdkb;

import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.list_boss.phoban.TrungUyXanhLoBdkb;
import com.girlkun.models.item.Item;
import com.girlkun.models.player.Player;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.List;

import static com.girlkun.models.map.bdkb.BanDoKhoBau.TIME_KHI_BAN_DO_KHO_BAU;

public class BanDoKhoBauService {

    public int timeoutmap;

    public long timeoutmapwait;

    private static BanDoKhoBauService i;

    private BanDoKhoBauService() {

    }

    public static BanDoKhoBauService gI() {
        if (i == null) {
            i = new BanDoKhoBauService();
        }
        return i;
    }

    public void update(Player player) {
        if (player.zone == null || !MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
            return;
        }
        if (player.isPl() && player.clan.banDoKhoBau != null
                && player.clan.timeOpenbdkb != 0) {
            if (Util.canDoWithTime(player.clan.timeOpenbdkb, TIME_KHI_BAN_DO_KHO_BAU)) {
                ketthucbdkb(player);
                player.clan.banDoKhoBau = null;
            }
            if (this.timeoutmap > 0 && player.isPl() && player.clan.banDoKhoBau != null
                    && player.clan.timeOpenbdkb != 0) {
                while (this.timeoutmap > 0) {
                    try {
                        this.timeoutmap--;
                        Service.gI().sendThongBao(player, "Bản đồ kho báu sẽ kết thúc trong " + timeoutmap + " giây. Tàu vũ trụ sẽ đưa bạn về nhà");
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (Exception e) {
                        Logger.logException(BanDoKhoBauService.class, e);
                    }
                }
                BanDoKhoBauService.gI().ketthucbdkb(player);
                player.clan.banDoKhoBau = null;
            }
        }
    }

    public void joinBDKB(Player pl) {
        if (pl.clan == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        if (pl.bdkb_countPerDay >= 3) {
            Service.gI().sendThongBao(pl, "Bạn đã đạt giới hạn lượt đi trong ngày");
            return;
        }
        if (pl.clan.banDoKhoBau != null) {
            if (!pl.bdkb_isJoinBdkb) {
                pl.bdkb_countPerDay++;
                pl.bdkb_isJoinBdkb = true;
            }
            pl.bdkb_lastTimeJoin = System.currentTimeMillis();
            ChangeMapService.gI().goToDBKB(pl);
            ItemTimeService.gI().sendTextBanDoKhoBau(pl);
        }
    }

    private void kickOutOfBDKB(Player player) {
        if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
            Service.gI().sendThongBao(player, "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
        }
    }

    public void ketthucbdkb(Player player) {
        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfBDKB(pl);
            ItemTimeService.gI().removeTextbdkb(player);
            pl.bdkb_isJoinBdkb = false;
        }
    }


    public void openBanDoKhoBau(Player player, byte level) {
        if (level >= 1 && level <= 110) {
            if (player.clan != null && player.clan.banDoKhoBau == null) {
                Item item = InventoryServiceNew.gI().findItemBag(player, 611);
                if (item != null && item.quantity > 0) {
                    BanDoKhoBau bdkb = null;
                    for (BanDoKhoBau bdkb1 : BanDoKhoBau.BAN_DO_KHO_BAU) {
                        if (!bdkb1.isOpened) {
                            bdkb = bdkb1;
                            break;
                        }
                    }
                    if (bdkb != null) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                        InventoryServiceNew.gI().sendItemBags(player);
                        bdkb.openBanDoKhoBau(player, player.clan, level);
                        try {
                            double totalDame = 0;
                            double totalHp = 0;
                            for (Player play : player.clan.membersInGame) {
                                totalDame += play.nPoint.dame;
                                totalHp += play.nPoint.hpMax;
                            }
                            double dame = (totalHp / 20) * (level);
                            double hp = (totalDame * 10) * (level);
                            if (dame >= 2000000000L) {
                                dame = 2000000000L;
                            }
                            if (hp >= 2000000000L) {
                                hp = 2000000000L;
                            }
                            new TrungUyXanhLoBdkb(player.clan.banDoKhoBau.getMapById(137), level, 1, 1, BossID.TRUNG_UY_XANH_LO_BDKB);
                        } catch (Exception e) {
                            Logger.logException(BanDoKhoBauService.class, e, "Lỗi init boss");
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Bản đồ kho báu đã đầy, vui lòng quay lại sau");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Yêu cầu có bản đồ kho báu");
                }
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.gI().sendThongBao(player, "Cấp độ phải thuộc khoảng từ 1-110");
        }
    }
}
