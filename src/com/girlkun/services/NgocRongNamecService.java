package com.girlkun.services;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Map;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.server.Client;
import com.girlkun.server.Manager;
import com.girlkun.server.ServerManager;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NgocRongNamecService implements Runnable {
    private static NgocRongNamecService instance;

    public static NgocRongNamecService gI() {
        if (instance == null) {
            instance = new NgocRongNamecService();
        }
        NgocRongNamecService.instance.setTimeopenNRNM();
        return instance;
    }

    public static long TIME_OP;
    public static long TIME_BL;
    public static long TIME_DELAY;


    public static final byte TIME_OP_HOUR = 14;
    public static final byte TIME_OP_MIN = 0;
    public static final byte TIME_OP_SECOND = 0;

    public static final byte TIME_BL_HOUR = 16;
    public static final byte TIME_BL_MIN = 0;
    public static final byte TIME_BL_SECOND = 0;

    public static final byte TIME_DL_HOUR = 14;
    public static final byte TIME_DL_MIN = 0;
    public static final byte TIME_DL_SECOND = 1;
    private int day = -1;

    public void setTimeopenNRNM() {
        if (NgocRongNamecService.instance.day == -1 || NgocRongNamecService.instance.day != TimeUtil.getCurrDay()) {
            NgocRongNamecService.instance.day = TimeUtil.getCurrDay();
            try {
                TIME_OP = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + TIME_OP_HOUR + ":" + TIME_OP_MIN + ":" + TIME_OP_SECOND, "dd/MM/yyyy HH:mm:ss");
                TIME_BL = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + TIME_BL_HOUR + ":" + TIME_BL_MIN + ":" + TIME_BL_SECOND, "dd/MM/yyyy HH:mm:ss");
                TIME_DELAY = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + TIME_DL_HOUR + ":" + TIME_DL_MIN + ":" + TIME_DL_SECOND, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {
                Logger.logException(NgocRongNamecService.class, e, "Error time");
            }
        }
    }

    public int[] mapNrNamec = {-1, -1, -1, -1, -1, -1, -1};
    public String[] nameNrNamec = {"", "", "", "", "", "", ""};
    public byte[] zoneNrNamec = {-1, -1, -1, -1, -1, -1, -1};
    public String[] pNrNamec = {"", "", "", "", "", "", ""};
    public int[] idpNrNamec = {-1, -1, -1, -1, -1, -1, -1};
    public long timeNrNamec = 0;
    public boolean firstNrNamec = true;
    public long tOpenNrNamec = 0;
    public long lastTimeReinit;
    public boolean isReinit;

    public void initNgocRongNamec(byte type) {
        //type 0:INIT NGOC RONG, type 1: INIT HOA THACH NGOC RONG
        ArrayList<Integer> listMap = new ArrayList<>();
        listMap.add(8);
        listMap.add(9);
        listMap.add(10);
        listMap.add(11);
        listMap.add(12);
        listMap.add(13);
        listMap.add(31);
        listMap.add(32);
        listMap.add(33);
        listMap.add(34);
        for (byte i = 0; i < (byte) 7; i++) {
            int index = Util.nextInt(0, listMap.size() - 1);
            int idZone = Util.nextInt(0, Manager.MAPS.get(listMap.get(index)).zones.size() - 1);
            mapNrNamec[i] = listMap.get(index);
            nameNrNamec[i] = Manager.MAPS.get(listMap.get(index)).mapName;
            zoneNrNamec[i] = (byte) idZone;
            Zone zone = Manager.MAPS.get(listMap.get(index)).zones.get(idZone);
            int x = 0;
            int y = 0;
            if (null != listMap.get(index)) {
                switch (listMap.get(index)) {
                    case 8 -> {
                        x = (short) 553;
                        y = (short) 288;
                    }
                    case 9 -> {
                        x = (short) 634;
                        y = (short) 432;
                    }
                    case 10 -> {
                        x = (short) 711;
                        y = (short) 288;
                    }
                    case 11 -> {
                        x = (short) 1078;
                        y = (short) 336;
                    }
                    case 12 -> {
                        x = (short) 1300;
                        y = (short) 288;
                    }
                    case 13 -> {
                        x = (short) 323;
                        y = (short) 432;
                    }
                    case 31 -> {
                        x = (short) 606;
                        y = (short) 312;
                    }
                    case 32 -> {
                        x = (short) 650;
                        y = (short) 360;
                    }
                    case 33 -> {
                        x = (short) 1325;
                        y = (short) 360;
                    }
                    case 34, 7 -> {
                        x = (short) 643;
                        y = (short) 432;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + listMap.get(index));
                }
            }
            if (type == (byte) 0) {
                ItemMap itemMap = new ItemMap(zone, i + 353, 1, x, y, -1);
                Service.getInstance().dropItemMap(zone, itemMap);
                System.out.println(itemMap.itemTemplate.name + "[" + zone.map.mapId + "-" + zone.zoneId + "]");
            } else {
                ItemMap itemMap = new ItemMap(zone, 362, 1, x, y, -1);
                Service.getInstance().dropItemMap(zone, itemMap);
            }
            listMap.remove(index);
        }
    }

    public void removeStoneNrNamec() {
        for (byte i = 0; i < (byte) 7; i++) {
            Zone zone = Manager.MAPS.get(mapNrNamec[i]).zones.get(zoneNrNamec[i]);
            int idItem = i + 353;
            for (byte j = 0; j < zone.items.size(); j++) {
                if (zone.items.get(j).itemTemplate.id == idItem) {
                    ItemMapService.gI().removeItemMap(zone.items.remove(j));
                }
            }
        }
    }

    public void doneDragonNamec() {
        for (int i = 0; i < 7; i++) {
            Player p = Client.gI().getPlayer(idpNrNamec[i]);
            if (p != null) {
                p.idNRNM = -1;
                pNrNamec[i] = "";
                idpNrNamec[i] = -1;
                Service.getInstance().sendFlagBag(p);
                PlayerService.gI().changeAndSendTypePK(p, ConstPlayer.NON_PK);
            }
        }
    }

    public void reInitNrNamec(long time) {
        lastTimeReinit = time;
        isReinit = true;
    }

    public boolean isSameMapNrNamec() {
        return (mapNrNamec[0] == 7) && (mapNrNamec[1] == 7) && (mapNrNamec[2] == 7) && (mapNrNamec[3] == 7) && (mapNrNamec[4] == 7) && (mapNrNamec[5] == 7) && (mapNrNamec[6] == 7);
    }

    public boolean isSameZoneNrNamec() {
        return (zoneNrNamec[0] == zoneNrNamec[1]) && (zoneNrNamec[2] == zoneNrNamec[0]) && (zoneNrNamec[3] == zoneNrNamec[0]) && (zoneNrNamec[4] == zoneNrNamec[0]) && (zoneNrNamec[5] == zoneNrNamec[0]) && (zoneNrNamec[6] == zoneNrNamec[0]);
    }

    public boolean canCallDragonNamec(Player p) {
        byte count = (byte) 0;
        if (isSameMapNrNamec() && isSameZoneNrNamec()) {
            if (p.clan != null) {
                for (int k : idpNrNamec) {
                    for (int j = 0; j < p.clan.members.size(); j++) {
                        if (k == p.clan.members.get(j).id) {
                            count++;
                        }
                    }
                }
                return count == (byte) 7;
            }
        }
        return false;
    }

    public void teleportToNrNamec(Player p) {
        int idMAP = mapNrNamec[p.idGo];
        int idZone = zoneNrNamec[p.idGo];
        Zone z = Manager.MAPS.get(idMAP).zones.get(idZone);
        if (z != null && !z.items.isEmpty()) {
            for (int i = 0; i < z.items.size(); i++) {
                ItemMap it = z.items.get(i);
                if (it != null && it.isNamecBall) {
                    ChangeMapService.gI().changeMap(p, z, Util.nextInt(100, z.map.mapWidth), 5);
                }
            }
        }
    }

    public String getDis(Player pl, int id, short temp) {
        try {
            int idMAP = mapNrNamec[id];
            int idZone = zoneNrNamec[id];
            Integer[] sttMap = {8, 9, 11, 12, 13, 31, 32, 33, 34};
            Zone z = Manager.MAPS.get(idMAP).zones.get(idZone);
            if (z != null && !z.items.isEmpty()) {
                ItemMap it = z.getItemMapByTempId(temp);
                if (it != null) {
                    if (pl.zone.map.mapId == it.zone.map.mapId) {
                        return String.valueOf(Math.abs((pl.location.x - it.x) / 10));
                    } else {
                        List<Integer> check = Arrays.asList(sttMap);
                        if (check.contains(pl.zone.map.mapId)) {
                            int index = findIndex(pl.zone.map.mapId);
                            int indexMap = findIndex(idMAP);
                            int w = 0;
                            for (int i = 0; i < findIndex(index, indexMap).size(); i++) {
                                int map = findIndex(index, indexMap).get(i);
                                w += Manager.MAPS.get(map).mapWidth;
                            }
                            return String.valueOf(Math.abs((pl.location.x - it.x - w) / 10));
                        } else {
                            return "?";
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(NgocRongNamecService.class, e, "Error player: " + pl.name);

        }
        return "?";
    }

    public byte findIndex(int id) {
        Integer[] sttMap = {8, 9, 11, 12, 13, 31, 32, 33, 34};
        for (byte i = 0; i < sttMap.length; i++) {
            if (sttMap[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public List<Integer> findIndex(int start, int stop) {
        List<Integer> a = new ArrayList<>();
        Integer[] sttMap = {8, 9, 11, 12, 13, 31, 32, 33, 34};
        if (start < stop) {
            a.addAll(Arrays.asList(sttMap).subList(start, stop));
        } else {
            a.addAll(Arrays.asList(sttMap).subList(stop, start));
        }
        return a;
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(500 - (System.currentTimeMillis() - st), 0));
                    if (this.isReinit && System.currentTimeMillis() >= TIME_OP && System.currentTimeMillis() <= TIME_DELAY) {
                        removeStoneNrNamec();
                        initNgocRongNamec((byte) 0);
                        break;
                    }
                }
            } catch (Exception e) {
                Logger.logException(NgocRongNamecService.class, e);
            }

        }
    }
}
