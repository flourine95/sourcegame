package com.girlkun.models.player;

//import com.girlkun.database.GirlkunDB;
import com.girlkun.utils.TimeUtil;

public class TimeReset {

    public static long TIME_RESET;
    public static long CLOSE_RESET;

    public static final byte HOUR_RESET = 1;
    public static final byte MIN_RESET = 0;
    public static final byte SECOND_RESET = 0;

    public static final byte HOUR_CLOSE = 1;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 2;

    public int hotong;
    public byte chuyensinh;
    public long toptaixiu;
    public int win;
    public int bongtai;
    public long MaxGoldTradeDay;

    private static TimeReset i;
    private int day = -1;

    public static TimeReset gI() {
        if (i == null) {
            i = new TimeReset();
        }
        i.setTime();
        return i;
    }

    public void setTime() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                TimeReset.TIME_RESET = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_RESET + ":" + MIN_RESET + ":" + SECOND_RESET, "dd/MM/yyyy HH:mm:ss");
                TimeReset.CLOSE_RESET = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CLOSE + ":" + MIN_CLOSE + ":" + SECOND_CLOSE, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {
            }
        }
    }

    public void update(Player player) {
        try {
            long now = System.currentTimeMillis();
            if (player != null && (now > TIME_RESET && now < CLOSE_RESET)) {
                if(player.taixiu.hotong != 0){
                    player.taixiu.hotong = 0;
                }
                if(player.taixiu.MaxGoldTradeDay != 0){
                    player.taixiu.MaxGoldTradeDay = 0;
                }
                if(player.taixiu.bongtai != 0){
                    player.taixiu.bongtai = 0;
                }
//                GirlkunDB.executeUpdate("UPDATE player SET Tai_xiu = JSON_REPLACE(JSON_REPLACE(JSON_REPLACE(Tai_xiu, '$[0]', 0), '$[4]', 0), '$[5]', 0)");
            }
        } catch (Exception ex) {
            System.out.println("=============================Loi reset thoi gian===============");
        }
    }

}
