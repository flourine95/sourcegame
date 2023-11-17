package com.girlkun.models.matches.pvp;

import com.girlkun.models.player.Player;
import com.girlkun.server.Manager;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
public class DaiHoiVoThuat implements Runnable{
    public ArrayList<Player> listReg = new ArrayList<>();
    public ArrayList<Player> listPlayerWait = new ArrayList<>();
    public String nameCup;
    public String[] time;
    public int gem;
    public int gold;
    public int minStart;
    public int minStartTemp;
    public int minLimit;
    public int round = 1;
    public int hour;
    public int minutes;
    public int second;
    private static DaiHoiVoThuat instance;
    public static DaiHoiVoThuat gI() {
        if (instance == null) {
            instance = new DaiHoiVoThuat();
        }
        return instance;
    }
    
    public DaiHoiVoThuat getDaiHoiNow(){
        for(DaiHoiVoThuat dh : Manager.LIST_DHVT){
            if(dh != null && Util.contains(dh.time, String.valueOf(hour))){
                return dh;
            }
        }
        return null;
    }
    
    public String Info() {
        for(DaiHoiVoThuat daihoi : Manager.LIST_DHVT){
            if (daihoi.gold > 0) {
                return "Lịch thi đấu trong ngày\bGiải " + daihoi.nameCup + ": " + Arrays.toString(daihoi.time).replace("[", "").replace("]", "") + "h\nLệ phí đăng ký thi đấu\bGiải " + daihoi.nameCup + ": " + Util.powerToString(daihoi.gold) + " vàng\b";
            } else if (daihoi.gem > 0) {
                return "Lịch thi đấu trong ngày\bGiải " + daihoi.nameCup + ": " + Arrays.toString(daihoi.time).replace("[", "").replace("]", "") + "h\nLệ phí đăng ký thi đấu\bGiải " + daihoi.nameCup + ": " + Util.powerToString(daihoi.gem) + " ngọc\b";
            }
        }
        return "Không có giải đấu nào được tổ chức\b";
    }
    
    @Override
    public void run() {
        while (true) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            try { 
                second = calendar.get(Calendar.SECOND);
                minutes = calendar.get(Calendar.MINUTE);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                DaiHoiVoThuatService.gI(getDaiHoiNow()).update();
                Thread.sleep(1000);
            }catch(Exception e){
                System.out.println("loi ne dhvt 1 ");
            }
        }
    }
}





















