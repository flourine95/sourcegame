package com.girlkun.models.map.doanhtrai;

import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.clan.Clan;
import com.girlkun.models.map.Zone;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.player.Player;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DoanhTrai {

    //bang hội đủ số người mới đc mở
    public static final List<DoanhTrai> DOANH_TRAI;
    public static final int N_PLAYER_CLAN = 5;
    //số người đứng cùng khu
    public static final int N_PLAYER_MAP = 2;
    public static final int AVAILABLE = 150;
    public static final int TIME_DOANH_TRAI = 1800000;

    List<Integer> listMap = Arrays.asList(53, 58, 59, 60, 61, 62, 55, 56, 54, 57);
    private int currentIndexMap = -1;
    private List<Boss> bossDoanhTrai;

    static {
        DOANH_TRAI = new ArrayList<>();
        for (int i = 0; i < AVAILABLE; i++) {
            DOANH_TRAI.add(new DoanhTrai(i));
        }
    }

    private int id;
    private List<Zone> zones;
    private Clan clan;
    public boolean isOpened;

    private long lastTimeOpen;

    public DoanhTrai(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
        this.bossDoanhTrai = new ArrayList<>();
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    public void openDoanhTrai(Player player) {
        this.lastTimeOpen = System.currentTimeMillis();
        this.clan = player.clan;
        player.clan.doanhTrai = this;
        player.clan.doanhTrai_playerOpen = player.name;
        player.clan.doanhTrai_lastTimeOpen = this.lastTimeOpen;
        player.clan.timeOpenDoanhTrai = this.lastTimeOpen;
        //Khởi tạo quái, boss
        this.init();
        //Đưa thành viên vào doanh trại
        for (Player pl : player.clan.membersInGame) {
            if (pl == null || pl.zone == null || !player.zone.equals(pl.zone)) {
                continue;
            }
            ChangeMapService.gI().changeMapInYard(pl, 53, -1, 60);
            ItemTimeService.gI().sendTextDoanhTrai(pl);
        }
    }

    private void init() {
        long totalDame = 0;
        long totalHp = 0;
        for (Player pl : this.clan.membersInGame) {
            totalDame += pl.nPoint.dame;
            totalHp += pl.nPoint.hpMax;
        }
        //Hồi sinh quái
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                mob.point.dame = Util.DoubleGioihan(totalHp / 20);
                mob.point.maxHp = Util.DoubleGioihan(totalDame * 20);
                mob.hoiSinh();
            }
        }

        try {
            long totalDame1 = 0;
            long totalHp1 = 0;
            for (Player play : this.clan.membersInGame) {
                totalDame1 += play.nPoint.dame;
                totalHp1 += play.nPoint.hpMax;
            }
            long dame = (totalHp1 / 20) * 5;
            long hp = (totalDame1 * 4) * 5;
            for (int i = 0; i < 8; i++) {
                bossDoanhTrai.add(BossManager.gI().createBossDoanhTrai(this.clan.doanhTrai.getMapById(getIdMap(BossID.TRUNG_UY_TRANG - i)), BossID.TRUNG_UY_TRANG - i, (long) dame, (long) hp));
            }
        } catch (Exception e) {
            System.out.println("   Loi tao Boss Doanh Trai");
        }
    }

    private int getIdMap(int bossID) {
        switch (bossID) {
            case BossID.TRUNG_UY_TRANG:
                return 59;
            case BossID.TRUNG_UY_XANH_LO:
                return 62;
            case BossID.TRUNG_UY_THEP:
                return 55;
            case BossID.NINJA_AO_TIM:
                return 54;
            case BossID.ROBOT_VE_SI_1:
            case BossID.ROBOT_VE_SI_2:
            case BossID.ROBOT_VE_SI_3:
            case BossID.ROBOT_VE_SI_4:
                return 57; // check vệ sĩ
            default:
                return -1;
        }
    }

    public void dispose() {
        for (Boss b : bossDoanhTrai) {
            if (b != null) {
                b.leaveMap();
                BossManager.gI().removeBoss(b);
                b = null;
            }
        }
        this.clan = null;
        this.bossDoanhTrai.clear();
//        currentIndexMap = -1;
    }
}

/**
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức
 */
