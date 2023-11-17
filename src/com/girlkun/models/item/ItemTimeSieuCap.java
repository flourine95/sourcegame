package com.girlkun.models.item;

import com.girlkun.models.player.Player;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;


public class ItemTimeSieuCap {

    //id item text
    public static final int TIME_ITEM3 = 600000;
    public static final int TIME_KEO = 1800000;
    public static final int TIME_XI_MUOI = 600000;
    public static final int TIME_TRUNGTHU = 3600000;
    public static final int TIME_EAT_MEAL = 600000;

    private Player player;

    public boolean isUseBoHuyet3;
    public boolean isUseBoKhi3;
    public boolean isUseGiapXen3;
    public boolean isUseCuongNo3;
    public boolean isUseAnDanh3;
    
    public long lastTimeBoHuyet3;
    public long lastTimeBoKhi3;
    public long lastTimeGiapXen3;
    public long lastTimeCuongNo3;
    public long lastTimeAnDanh3;

    public boolean isUseXiMuoi;
    public long lastTimeUseXiMuoi;
    
    public boolean isUseTrungThu;
    public long lastTimeUseBanh;
    public int iconBanh;
    
    public boolean isKeo;
    public long lastTimeKeo;


    public boolean isEatMeal;
    public long lastTimeMeal;
    public int iconMeal;

    public ItemTimeSieuCap(Player player) {
        this.player = player;
    }

    public void update() {
        if (isEatMeal) {
            if (Util.canDoWithTime(lastTimeMeal, TIME_EAT_MEAL)) {
                isEatMeal = false;
                Service.gI().point(player);
            }
        }
        if (isUseBoHuyet3) {
            if (Util.canDoWithTime(lastTimeBoHuyet3, TIME_ITEM3)) {
                isUseBoHuyet3 = false;
                Service.gI().point(player);
//                Service.getInstance().Send_Info_NV(this.player);
            }
        }
        
        if (isUseBoKhi3) {
            if (Util.canDoWithTime(lastTimeBoKhi3, TIME_ITEM3)) {
                isUseBoKhi3 = false;
                Service.gI().point(player);
            }
        }
       
        if (isUseGiapXen3) {
            if (Util.canDoWithTime(lastTimeGiapXen3, TIME_ITEM3)) {
                isUseGiapXen3 = false;
            }
        }
        if (isUseCuongNo3) {
            if (Util.canDoWithTime(lastTimeCuongNo3, TIME_ITEM3)) {
                isUseCuongNo3 = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh3) {
            if (Util.canDoWithTime(lastTimeAnDanh3, TIME_ITEM3)) {
                isUseAnDanh3 = false;
            }
        }
       
        if (isKeo) {
            if (Util.canDoWithTime(lastTimeKeo, TIME_KEO)) {
                isKeo = false;
            }
        }
        if (isUseXiMuoi) {
            if (Util.canDoWithTime(lastTimeUseXiMuoi, TIME_XI_MUOI)) {
                isUseXiMuoi = false;
            }
        }
        if (isUseTrungThu) {
            if (Util.canDoWithTime(lastTimeUseBanh, TIME_TRUNGTHU)) {
                isUseTrungThu = false;
            }
        }
    }
    
    public void dispose(){
        this.player = null;
    }
}
