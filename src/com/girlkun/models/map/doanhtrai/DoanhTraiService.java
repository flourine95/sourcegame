package com.girlkun.models.map.doanhtrai;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static com.girlkun.models.map.doanhtrai.DoanhTrai.TIME_DOANH_TRAI;

public class DoanhTraiService {

    private static DoanhTraiService I;

    public static DoanhTraiService gI() {
        if (DoanhTraiService.I == null) {
            DoanhTraiService.I = new DoanhTraiService();
        }
        DoanhTraiService.I.setTimeJoinMapDT();
        return DoanhTraiService.I;

    }

    public List<DoanhTrai> doanhTrais;
    public static long TIME_RESET_DT;
    public static long TIME_DELAY_DT;

    public static final byte TIME_RESET_DT_HOUR = 7;
    public static final byte TIME_RESET_DT_MIN = 0;
    public static final byte TIME_RESET_DT_SECOND = 0;

    public static final byte TIME_DELAY_DT_HOUR = 7;
    public static final byte TIME_DELAY_DT_MIN = 0;
    public static final byte TIME_DELAY_DT_SECOND = 2;
    private int day = -1;

    public void setTimeJoinMapDT() {
        if (DoanhTraiService.I.day == -1 || DoanhTraiService.I.day != TimeUtil.getCurrDay()) {
            DoanhTraiService.I.day = TimeUtil.getCurrDay();
            try {
                TIME_RESET_DT = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + TIME_RESET_DT_HOUR + ":" + TIME_RESET_DT_MIN + ":" + TIME_RESET_DT_SECOND, "dd/MM/yyyy HH:mm:ss");
                TIME_DELAY_DT = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + TIME_DELAY_DT_HOUR + ":" + TIME_DELAY_DT_MIN + ":" + TIME_DELAY_DT_SECOND, "dd/MM/yyyy HH:mm:ss");

            } catch (Exception ignored) {
                System.out.println("loi ne dtrai 1 ");
            }
        }
    }

    private DoanhTraiService() {
        this.doanhTrais = new ArrayList<>();
        for (int i = 0; i < DoanhTrai.AVAILABLE; i++) {
            this.doanhTrais.add(new DoanhTrai(i));
        }
    }

    public void addMapDoanhTrai(int id, Zone zone) {
        this.doanhTrais.get(id).getZones().add(zone);
    }

    public void update(Player player) {
        if (player.zone == null || !MapService.gI().isMapDoanhTrai(player.zone.map.mapId)) {
            return;
        }
        if (player.isPl() == true && player.clan.doanhTrai != null
                && player.clan.timeOpenDoanhTrai != 0) {
            if (Util.canDoWithTime(player.clan.timeOpenDoanhTrai, TIME_DOANH_TRAI)) {
                ketthucDT(player);
                //player.clan.doanhTrai = null;
            }
            try {
                long now = System.currentTimeMillis();
                if (now > TIME_RESET_DT && now < TIME_DELAY_DT) {
//                    if (player.clan.doanhTrai != null) {
//                        player.clan.doanhTrai = null;
//                        player.clan.doanhTrai_haveGone = false;
//                    }
                    GirlkunDB.executeUpdate("UPDATE player SET Tai_xiu = JSON_REPLACE(JSON_REPLACE(Tai_xiu, '$[0]', 0), '$[4]', 0)");
                    Thread.sleep(500);
                }
            } catch (Exception ignored) {
                System.out.println("loi ne dtrai 2 ");
            }
        } else {
            try {
            } catch (Exception ignored) {
                System.out.println("loi ne dtrai 3 ");
            }
        }

    }

    private void kickOutOfDT(Player player) {
        if (MapService.gI().isMapDoanhTrai(player.zone.map.mapId)) {
            Service.gI().sendThongBao(player, "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
            ItemTimeService.gI().sendTextTime(player, (byte) 0, "", 0);
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
        }
        if (player.clan.doanhTrai != null) {
            player.clan.doanhTrai.dispose();
            player.clan.doanhTrai = null;
        }
    }

    private void ketthucDT(Player player) {
        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfDT(pl);
            ItemTimeService.gI().removeTextDoanhTrai(player);
            player.clan.doanhTrai_haveGone = true;
        }
    }

    public void joinDoanhTrai(Player player) {

        if (player.clan == null) {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (player.clan.doanhTrai != null) {
            ChangeMapService.gI().changeMapInYard(player, 53, -1, 60);
            return;
        }
        DoanhTrai doanhTrai = null;
        for (DoanhTrai dt : this.doanhTrais) {
            if (dt.getClan() == null) {
                doanhTrai = dt;
                break;
            }
        }
        if (doanhTrai == null) {
            Service.getInstance().sendThongBao(player, "Doanh trại đã đầy, hãy quay lại vào lúc khác!");
        } else {
            doanhTrai.openDoanhTrai(player);
        }
    }
}

/**
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức.
 */
