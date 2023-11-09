package com.girlkun.models.map.MapMaBu;

import com.girlkun.models.player.Player;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.List;

public class MapMaBu {


    public static final byte HOUR_OPEN_MAP_MABU = 12;
    public static final byte MIN_OPEN_MAP_MABU = 0;
    public static final byte SECOND_OPEN_MAP_MABU = 0;


    public static final byte HOUR_CLOSE_MAP_MABU = 13;
    public static final byte MIN_CLOSE_MAP_MABU = 0;
    public static final byte SECOND_CLOSE_MAP_MABU = 0;

    public static final int AVAILABLE = 7;

    private static MapMaBu i;

    public static long TIME_OPEN_MABU;
    public static long TIME_CLOSE_MABU;

    private int day = -1;

    public static MapMaBu gI() {
        if (i == null) {
            i = new MapMaBu();
        }
        i.setTimeJoinMapMaBu();
        return i;
    }

    public void setTimeJoinMapMaBu() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                TIME_OPEN_MABU = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_OPEN_MAP_MABU + ":" + MIN_OPEN_MAP_MABU + ":" + SECOND_OPEN_MAP_MABU, "dd/MM/yyyy HH:mm:ss");
                TIME_CLOSE_MABU = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CLOSE_MAP_MABU + ":" + MIN_CLOSE_MAP_MABU + ":" + SECOND_CLOSE_MAP_MABU, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception ignored) {
                System.out.println("        loi map mabu");
            }
        }
    }


    private void kickOutOfMapMabu(Player player) {
        if (MapService.gI().isMapMaBu(player.zone.map.mapId)) {
            Service.getInstance().sendThongBao(player, "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
        }
    }

    private void ketthucmabu(Player player) {
        player.zone.finishMapMaBu = true;
        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfMapMabu(pl);
        }
    }

    public void joinMapMabu(Player player) {
        boolean changed = false;
        if (player.clan != null) {
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
                if (pl.clan != null && !player.equals(pl) && player.clan.equals(pl.clan) && !player.isBoss) {
                    Service.getInstance().changeFlag(player, Util.nextInt(9, 10));
                    changed = true;
                    break;
                }
            }
        }
        if (!changed && !player.isBoss) {
            Service.getInstance().changeFlag(player, Util.nextInt(9, 10));
        }
    }

    public void update(Player player) {
        if (player.zone == null || !MapService.gI().isMapMaBu(player.zone.map.mapId)) {
            return;
        }
            try {
                long now = System.currentTimeMillis();
//                if (now < TIME_OPEN_MABU || now > TIME_CLOSE_MABU) {
//                    ketthucmabu(player);
//                }
            } catch (Exception ignored) {
                System.out.println("        loi update map mabu");
            }

    }
}
