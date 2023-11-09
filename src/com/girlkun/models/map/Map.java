package com.girlkun.models.map;

import com.girlkun.consts.ConstMap;
import com.girlkun.models.Template;
import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.map.MapMaBu.MapMaBu;
import com.girlkun.models.map.bdkb.BanDoKhoBau;
import com.girlkun.models.map.blackball.BlackBallWar;
import com.girlkun.models.map.doanhtrai.DoanhTrai;
import com.girlkun.models.map.doanhtrai.DoanhTraiService;
import com.girlkun.models.map.gas.Gas;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcFactory;
import com.girlkun.models.player.Player;
import com.girlkun.server.Manager;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Map implements Runnable {

    private static final int SIZE = 24;

    public int mapId;
    public String mapName;

    public byte planetId;
    public String planetName;

    public byte tileId;
    public byte bgId;
    public byte bgType;
    public byte type;

    private final int[][] tileMap;
    public int[] tileTop;
    public int mapWidth;
    public int mapHeight;

    public List<Zone> zones;
    public List<WayPoint> wayPoints;
    public List<Npc> npcs;

    public Map(int mapId, String mapName, byte planetId,
               byte tileId, byte bgId, byte bgType, byte type, int[][] tileMap,
               int[] tileTop, int zones, int maxPlayer, List<WayPoint> wayPoints) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.planetId = planetId;
        this.planetName = Service.getInstance().getHanhTinh(planetId);
        this.tileId = tileId;
        this.bgId = bgId;
        this.bgType = bgType;
        this.type = type;
        this.tileMap = tileMap;
        this.tileTop = tileTop;
        this.zones = new ArrayList<>();
        this.wayPoints = wayPoints;
        try {
            this.mapHeight = tileMap.length * SIZE;
            this.mapWidth = tileMap[0].length * SIZE;
        } catch (Exception e) {
            Logger.logException(Map.class, e, "Map error: " + mapName + "(" + mapId + ")");

        }
        this.initZone(zones, maxPlayer);
        // init item
        for (Zone zone : this.zones) {
            ItemMap itemMap = switch (this.mapId) {
                case 21 -> new ItemMap(zone, 74, 1, 633, 315, -1);
                case 22 -> new ItemMap(zone, 74, 1, 56, 315, -1);
                case 23 -> new ItemMap(zone, 74, 1, 633, 320, -1);
                case 42, 44 -> new ItemMap(zone, 78, 1, 70, 288, -1);
                case 43 -> new ItemMap(zone, 78, 1, 70, 264, -1);
                default -> null;
            };
        }
        // init trap map
        for (Zone zone : this.zones) {
            TrapMap trap;
            if (this.mapId == 135) {
                trap = new TrapMap();
                trap.x = 260;
                trap.y = 960;
                trap.w = 740;
                trap.h = 72;
                trap.effectId = 49; //xiên
                zone.trapMaps.add(trap);
            }
        }
    }

    private void initZone(int nZone, int maxPlayer) {
        switch (this.type) {
            case ConstMap.MAP_OFFLINE -> nZone = 1;
            case ConstMap.MAP_BLACK_BALL_WAR -> nZone = BlackBallWar.AVAILABLE;
            case ConstMap.MAP_MA_BU -> nZone = MapMaBu.AVAILABLE;
            case ConstMap.MAP_DOANH_TRAI -> nZone = DoanhTrai.AVAILABLE;
            case ConstMap.MAP_KHI_GAS -> nZone = Gas.MAX_AVAILABLE;
            case ConstMap.MAP_BAN_DO_KHO_BAU -> nZone = BanDoKhoBau.MAX_AVAILABLE;
        }

        for (int i = 0; i < nZone; i++) {
            Zone zone = new Zone(this, i, maxPlayer);
            this.zones.add(zone);
            switch (this.type) {
                case ConstMap.MAP_DOANH_TRAI -> DoanhTraiService.gI().addMapDoanhTrai(i, zone);
                case ConstMap.MAP_KHI_GAS -> Gas.addZone(i, zone);
                case ConstMap.MAP_BAN_DO_KHO_BAU -> BanDoKhoBau.addZone(i, zone);
            }
        }
    }

    public void initNpc(byte[] npcId, short[] npcX, short[] npcY) {
        this.npcs = new ArrayList<>();
        for (int i = 0; i < npcId.length; i++) {
            this.npcs.add(NpcFactory.createNPC(this.mapId, 1, npcX[i], npcY[i], npcId[i]));
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(1000 - (System.currentTimeMillis() - st), 0));
                    for (Zone zone : this.zones) {
                        zone.update();
                    }
                }
            } catch (Exception e) {
                Logger.logException(Map.class, e, "Map error: " + mapName + "(" + mapId + ")");
            }
        }
    }

    public void initMob(byte[] mobTemp, byte[] mobLevel, double[] mobHp, short[] mobX, short[] mobY) {
        for (int i = 0; i < mobTemp.length; i++) {
            int mobTempId = mobTemp[i];
            Template.MobTemplate temp = Manager.getMobTemplateByTemp(mobTempId);
            if (temp != null) {
                Mob mob = new Mob();
                mob.id = i;
                mob.tempId = mobTemp[i];
                mob.level = mobLevel[i];
                mob.point.setHpFull(mobHp[i]);
                mob.location.x = mobX[i];
                mob.location.y = mobY[i];
                mob.point.sethp(Util.DoubleGioihan(mob.point.getHpFull()));
                mob.pDame = temp.percentDame;
                mob.pTiemNang = temp.percentTiemNang;
                mob.setTiemNang();
                for (Zone zone : this.zones) {
                    Mob mobZone = new Mob(mob);
                    mobZone.zone = zone;
                    zone.mobs.add(mobZone);
                }
            }
        }
    }

    public void initBoss() {
        for (Zone zone : zones) {
            short bossId = switch (this.mapId) {
                case 114 -> BossID.DRABURA;
                case 115 -> BossID.DRABURA_2;
                case 117 -> BossID.BUI_BUI;
                case 118 -> BossID.BUI_BUI_2;
                case 119 -> BossID.YA_CON;
                case 120 -> BossID.MABU_12H;
                default -> -1;
            };
            if (bossId != -1) {
                Boss boss = BossManager.gI().createBoss(bossId);
                boss.zoneFinal = zone;
                boss.joinMapByZone(zone);
            }
        }
    }

    public short mapIdNextMabu(short mapId) {
        return switch (mapId) {
            case 114 -> 115;
            case 115 -> 117;
            case 117 -> 118;
            case 118 -> 119;
            case 119 -> 120;
            default -> -1;
        };
    }

    public Npc getNPC(Player player, int tempId) {
        for (Npc npc : npcs) {
            if (npc.tempId == tempId && Util.getDistance(player, npc) <= 60) {
                return npc;
            }
        }
        return null;
    }

    public int yPhysicInTop(int x, int y) {
        try {
            int rX = x / SIZE;
            int rY = 0;
            if (isTileTop(tileMap[y / SIZE][rX])) {
                return y;
            }
            for (int i = y / SIZE; i < tileMap.length; i++) {
                if (isTileTop(tileMap[i][rX])) {
                    rY = i * SIZE;
                    break;
                }
            }
            return rY;
        } catch (Exception e) {
            Logger.logException(Map.class, e, "Map error: " + mapName + "(" + mapId + ")");
            return y;
        }
    }

    private boolean isTileTop(int tileMap) {
        for (int j : tileTop) {
            if (j == tileMap) {
                return true;
            }
        }
        return false;
    }
}
