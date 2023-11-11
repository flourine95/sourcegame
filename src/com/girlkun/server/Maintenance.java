package com.girlkun.server;

import com.girlkun.models.map.Map;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;


public class Maintenance extends Thread {

    public static boolean isRuning = false;

    private static Maintenance i;

    private int min;

    private Maintenance() {

    }

    public static Maintenance gI() {
        if (i == null) {
            i = new Maintenance();
        }
        return i;
    }

    public void start(int min) {
        if (!isRuning) {
            isRuning = true;
            this.min = min;
            this.start();
        }
    }

    @Override
    public void run() {
        while (this.min > 0) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(1000 - (System.currentTimeMillis() - st), 0));
                    this.min--;
                    Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + min
                            + " giây nữa, vui lòng thoát game để tránh mất vật phẩm");
                }
            } catch (Exception e) {
                Logger.logException(Map.class, e);
            }

        }
        Logger.log(Logger.CYAN, "BEGIN MAINTENANCE\n");
        ServerManager.gI().close();
    }

}
