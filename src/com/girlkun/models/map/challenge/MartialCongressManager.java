package com.girlkun.models.map.challenge;

import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BTH sieu cap vippr0 
 */
public class MartialCongressManager {

    private static MartialCongressManager i;
    private long lastUpdate;
    private static List<MartialCongress> list = new ArrayList<>();
    private static List<MartialCongress> toRemove = new ArrayList<>();

    public static MartialCongressManager gI() {
        if (i == null) {
            i = new MartialCongressManager();
        }
        return i;
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            synchronized (list) {
                for (MartialCongress mc : list) {
                    try {
                        mc.update();
                    } catch (Exception e) {
                        System.out.println("kkkkk");
                    }
                }
                list.removeAll(toRemove);
            }
        }
    }

    public void add(MartialCongress mc) {
        synchronized (list) {
            list.add(mc);
        }
    }

    public void remove(MartialCongress mc) {
        synchronized (toRemove) {
            toRemove.add(mc);
        }
    }
}
