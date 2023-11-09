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
    public String NameCup;
    public String[] Time;
    public int gem;
    public int gold;
    public int min_start;
    public int min_start_temp;
    public int min_limit;
    public int round = 1;
    
    public int Hour;
    public int Minutes;
    public int Second;
    
    private static DaiHoiVoThuat instance;    

    public static DaiHoiVoThuat gI() {
        if (instance == null) {
            instance = new DaiHoiVoThuat();
        }
        return instance;
    }
    
    public DaiHoiVoThuat getDaiHoiNow(){
        for(DaiHoiVoThuat dh : Manager.LIST_DHVT){
            if(dh != null && Util.contains(dh.Time, String.valueOf(Hour))){
                return dh;
            }
        }
        return null;
    }
    
    public String Info() {
        for(DaiHoiVoThuat daihoi : Manager.LIST_DHVT){
            if (daihoi.gold > 0) {
                return "Lịch thi đấu trong ngày\bGiải " + daihoi.NameCup + ": " + Arrays.toString(daihoi.Time).replace("[", "").replace("]", "") + "h\nLệ phí đăng ký thi đấu\bGiải " + daihoi.NameCup + ": " + Util.powerToString(daihoi.gold) + " vàng\b";
            } else if (daihoi.gem > 0) {
                return "Lịch thi đấu trong ngày\bGiải " + daihoi.NameCup + ": " + Arrays.toString(daihoi.Time).replace("[", "").replace("]", "") + "h\nLệ phí đăng ký thi đấu\bGiải " + daihoi.NameCup + ": " + Util.powerToString(daihoi.gem) + " ngọc\b";
            }
        }
        return "Không có giải đấu nào được tổ chức\b";
    }
    
    @Override
    public void run() {
        while (true) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            try { 
                Second = calendar.get(Calendar.SECOND);
                Minutes = calendar.get(Calendar.MINUTE);
                Hour = calendar.get(Calendar.HOUR_OF_DAY);
               
                DaiHoiVoThuatService.gI(getDaiHoiNow()).Update();
                Thread.sleep(1000);
            }catch(Exception e){
                System.out.println("loi ne dhvt 1 ");
            }
        }
    }
}





















