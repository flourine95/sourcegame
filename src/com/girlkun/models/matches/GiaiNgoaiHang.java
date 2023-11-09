package com.girlkun.models.matches;

import com.girlkun.server.ServerManager;

import java.util.ArrayList;
import java.util.List;


public class GiaiNgoaiHang implements Runnable {

    private static GiaiNgoaiHang I;

    public static GiaiNgoaiHang gI() {
        if (GiaiNgoaiHang.I == null) {
            GiaiNgoaiHang.I = new GiaiNgoaiHang();
            new Thread(GiaiNgoaiHang.I, "Update giải đấu ngoại hạng").start();
        }
        return GiaiNgoaiHang.I;
    }

    private List<Long> subscribers;

    public GiaiNgoaiHang() {
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void run() {
        this.update();
    }

    private void update() {
        while (ServerManager.isRunning) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("loi ne giaingoaihang 1 ");
            }
        }
    }
}

/**
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức.
 */
