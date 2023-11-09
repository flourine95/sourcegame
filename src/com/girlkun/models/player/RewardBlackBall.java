package com.girlkun.models.player;

import com.girlkun.models.map.blackball.BlackBallWar;
import com.girlkun.services.Service;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.Date;

public class RewardBlackBall {

    private static final int TIME_REWARD = 79200000;

    public static final int R1S_1 = 20;
    public static final int R1S_2 = 15;
    public static final int R2S_1 = 15;
    public static final int R2S_2 = 20;
    public static final int R3S_1 = 20;
    public static final int R3S_2 = 10;
    public static final int R4S_1 = 10;
    public static final int R4S_2 = 20;
    public static final int R5S_1 = 20;
    public static final int R5S_2 = 20;
    public static final int R5S_3 = 20;
    public static final int R6S_1 = 50;
    public static final int R6S_2 = 20;
    public static final int R7S_1 = 10;
    public static final int R7S_2 = 15;

    public static final int TIME_WAIT = 3600000;
    public static long time8h;
    private Player player;

    public long[] timeOutOfDateReward;
    public int[] quantilyBlackBall;
    public long[] lastTimeGetReward;

    public RewardBlackBall(Player player) {
        this.player = player;
        this.timeOutOfDateReward = new long[7];
        this.lastTimeGetReward = new long[7];
        this.quantilyBlackBall = new int[7];
        time8h = BlackBallWar.TIME_OPEN;
    }

    public void reward(byte star) {
        if (this.timeOutOfDateReward[star - 1] > time8h) {
            quantilyBlackBall[star - 1]++;
        }
        this.timeOutOfDateReward[star - 1] = System.currentTimeMillis() + TIME_REWARD;
        Service.getInstance().point(player);
    }

    public void getRewardSelect(byte select) {
        int index = 0;
        for (int i = 0; i < timeOutOfDateReward.length; i++) {
            if (timeOutOfDateReward[i] > System.currentTimeMillis()) {
                index++;
                if (index == select + 1) {
                    getReward(i + 1);
                    break;
                }
            }
        }
    }

    private void getReward(int star) {
        if (timeOutOfDateReward[star - 1] > System.currentTimeMillis()
                && Util.canDoWithTime(lastTimeGetReward[star - 1], TIME_WAIT)) {
            switch (star) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
//                    Service.getInstance().sendThongBao(player, "Phần thưởng chỉ số tự động nhận");
                    break;
            }
        } else {
            Service.getInstance().sendThongBao(player, "Chưa thể nhận phần quà ngay lúc này, vui lòng đợi "
                    + TimeUtil.diffDate(new Date(lastTimeGetReward[star - 1]), new Date(lastTimeGetReward[star - 1] + TIME_WAIT),
                    TimeUtil.MINUTE) + " phút nữa");
        }
    }

    public void dispose() {
        this.player = null;
    }
}
