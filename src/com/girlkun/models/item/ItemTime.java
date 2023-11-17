package com.girlkun.models.item;

import com.girlkun.models.player.Player;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;


public class ItemTime {

    //id item text
    public static final byte DOANH_TRAI = 0;
    public static final byte BAN_DO_KHO_BAU = 1;
     public static final byte KHI_GASS= 2;
    public static final int TIME_ITEM = 600000;
    public static final int TIME_BI_NGO = 1800000;
    public static final int TIME_MAY_DO = 1800000;
    public static final int TIME_MAY_DO2 = 1200000;
    public static final int TIME_DUOI_KHI = 600000;

    private Player player;

    public boolean isUseBoHuyet;
    public boolean isUseBoKhi;
    public boolean isUseGiapXen;
    public boolean isUseCuongNo;
    public boolean isUseAnDanh;
    
    public long lastTimeBoHuyet;
    public long lastTimeBoKhi;
    public long lastTimeGiapXen;
    public long lastTimeCuongNo;
    public long lastTimeAnDanh;

    public boolean isUseMayDo;
    public long lastTimeUseMayDo;//lastime de chung 1 cai neu time = nhau
    public boolean isUseMayDo2;
    public long lastTimeUseMayDo2;
    
    public boolean isBiNgo;
    public long lastTimeBiNgo;

    public boolean isUseTDLT;
    public long lastTimeUseTDLT;
    public int timeTDLT;

    public boolean isDuoikhi;
    public long lastTimeDuoikhi;
    public int iconDuoi;

    public ItemTime(Player player) {
        this.player = player;
    }

    public void update() {
        if (isDuoikhi) {
            if (Util.canDoWithTime(lastTimeDuoikhi, TIME_DUOI_KHI)) {
                isDuoikhi = false;
                Service.gI().point(player);
            }
        }
        if (isUseBoHuyet) {
            if (Util.canDoWithTime(lastTimeBoHuyet, TIME_ITEM)) {
                isUseBoHuyet = false;
                Service.gI().point(player);
//                Service.getInstance().Send_Info_NV(this.player);
            }
        }
        
        if (isUseBoKhi) {
            if (Util.canDoWithTime(lastTimeBoKhi, TIME_ITEM)) {
                isUseBoKhi = false;
                Service.gI().point(player);
            }
        }
       
        if (isUseGiapXen) {
            if (Util.canDoWithTime(lastTimeGiapXen, TIME_ITEM)) {
                isUseGiapXen = false;
            }
        }
        if (isUseCuongNo) {
            if (Util.canDoWithTime(lastTimeCuongNo, TIME_ITEM)) {
                isUseCuongNo = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh) {
            if (Util.canDoWithTime(lastTimeAnDanh, TIME_ITEM)) {
                isUseAnDanh = false;
            }
        }
        if (isBiNgo) {
            if (Util.canDoWithTime(lastTimeBiNgo, TIME_BI_NGO)) {
                isBiNgo = false;
            }
        }
        if (isUseMayDo) {
            if (Util.canDoWithTime(lastTimeUseMayDo, TIME_MAY_DO)) {
                isUseMayDo = false;
            }
        }
        if (isUseMayDo2) {
            if (Util.canDoWithTime(lastTimeUseMayDo2, TIME_MAY_DO2)) {
                isUseMayDo2 = false;
            }
        }
        if (isUseTDLT) {
            if (Util.canDoWithTime(lastTimeUseTDLT, timeTDLT)) {
                this.isUseTDLT = false;
                ItemTimeService.gI().sendCanAutoPlay(this.player);
            }
        }
    }
    
    public void dispose(){
        this.player = null;
    }
}
