package com.girlkun.models.npc.specialnpc;

import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.utils.Logger;


public class Timedua {

//    private static final long DEFAULT_TIME_DONE = 7776000000L;
    private static final long DEFAULT_TIME_DON = 7200000;

    private Player player;
    public long lastTimeCreate;
    public long timeDone;

    private final short id = 51;

    public Timedua(Player player, long lastTimeCreate, long timeDone) {
        this.player = player;
        this.lastTimeCreate = lastTimeCreate;
        this.timeDone = timeDone;
    }

    public static void createTimedua(Player player) {
        player.timedua = new Timedua(player, System.currentTimeMillis(), DEFAULT_TIME_DON);
    }

    public void sendTimedua() {
        Message msg;
        try {
//            Message msg = new Message(-117);
//            msg.writer().writeByte(100);
//            player.sendMessage(msg);
//            msg.cleanup();

            msg = new Message(-122);
            msg.writer().writeShort(this.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(4672);
            msg.writer().writeByte(0);
            msg.writer().writeInt(this.getSecondDone());
            this.player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception r) {
            Logger.logException(Timedua.class, r);
        }
    }

    public int getSecondDone() {
        int seconds = (int) ((lastTimeCreate + timeDone - System.currentTimeMillis()) / 1000);
        return seconds > 0 ? seconds : 0;
    }

    public void opendua(int gender) {
//        if (this.player.pet != null) {
//            try {
//                destroydua();
//                Thread.sleep(4000);
////                if (this.player.pet == null) {
////                    PetService.gI().createMabuPet(this.player, gender);
////                } else {
////                    PetService.gI().changeMabuPet(this.player, gender);
////                }
//                ChangeMapService.gI().changeMapInYard(this.player, this.player.gender * 7, -1, Util.nextInt(300, 500));
//                player.timedua = null;
//            } catch (Exception e) {
//            }
//        } else {
//            Service.getInstance().sendThongBao(player, "Yêu cầu phải có đệ tử");
//        }
    }

    public void destroydua() {
        try {
            Message msg = new Message(-117);
            msg.writer().writeByte(101);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
        this.player.timedua = null;
    }

    public void subTimeDone(int d, int h, int m, int s) {
        this.timeDone -= ((d * 24 * 60 * 60 * 1000) + (h * 60 * 60 * 1000) + (m * 60 * 1000) + (s * 1000));
        this.sendTimedua();
    }
    
    public void dispose(){
        this.player = null;
    }
}
