package com.girlkun.services.func;

import com.girlkun.database.GirlkunDB;
import com.girlkun.server.Manager;
import com.girlkun.utils.Logger;

import java.sql.Connection;

public class TopService implements Runnable {
    private static final int DELAY_TOP = 0;
    private static TopService i;

    public static TopService gI() {
        if (i == null) {
            i = new TopService();
        }
        return i;
    }


    @Override
    public void run() {
        while (true) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(1000 - (System.currentTimeMillis() - st), 0));
                    if (Manager.timeRealTop + (DELAY_TOP) < System.currentTimeMillis()) {
                        Manager.timeRealTop = System.currentTimeMillis();
                        try (Connection con = GirlkunDB.getConnection()) {
                            Manager.topNV = Manager.realTop(Manager.queryTopNV, con);
                            Manager.topSM = Manager.realTop(Manager.queryTopSM, con);
                            Manager.topSB = Manager.realTop(Manager.querytopSB, con);
                            Manager.topSK = Manager.realTop(Manager.querytopSK, con);
                            Manager.topPVP = Manager.realTop(Manager.queryTopPVP, con);
                            Manager.topNHS = Manager.realTop(Manager.queryTopNHS, con);
                            Manager.topVND = Manager.realTop(Manager.queryTopVND, con);
                            Manager.topHP = Manager.realTop(Manager.queryTopHP, con);
                            Manager.topKI = Manager.realTop(Manager.queryTopKI, con);
                            Manager.topSD = Manager.realTop(Manager.queryTopSD, con);
                        } catch (Exception e) {
                            Logger.logException(TopService.class, e, "Lỗi đọc top");
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logException(TopService.class, e);
            }
        }
    }
}
