package com.girlkun.services;

import com.girlkun.models.player.NPoint;
import com.girlkun.models.player.Pet;
import com.girlkun.models.player.Player;


public class OpenPowerService {

    public static final int COST_SPEED_OPEN_LIMIT_POWER = 500000000;

    private static OpenPowerService i;

    private OpenPowerService() {

    }

    public static OpenPowerService gI() {
        if (i == null) {
            i = new OpenPowerService();
        }
        return i;
    }

    public boolean openPowerSpeed(Player player) {
        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
            if (player.nPoint.power >= 17900000000L) {
            player.nPoint.limitPower++;
            if (player.nPoint.limitPower > NPoint.MAX_LIMIT) {
                player.nPoint.limitPower = NPoint.MAX_LIMIT;
            }
            if (!player.isPet) {
                Service.getInstance().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên 1 bậc");
            } else {
                Service.getInstance().sendThongBao(((Pet) player).master, "Giới hạn sức mạnh của đệ tử đã được tăng lên 1 bậc");
            }
            return true;
            } else {
                if (!player.isPet) {
                    Service.getInstance().sendThongBao(player, "Sức mạnh của bạn không đủ để thực hiện");
                } else {
                    Service.getInstance().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử không đủ để thực hiện");
                }
                return false;
            }
        } else {
            if (!player.isPet) {
                Service.getInstance().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            } else {
                Service.getInstance().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử đã đạt tới mức tối đa");
            }
            return false;
        }
    }

}
