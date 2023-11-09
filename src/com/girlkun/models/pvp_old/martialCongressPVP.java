//package com.girlkun.models.pvp_old;
//
//import com.girlkun.models.player.Player;
//import com.girlkun.services.Service;
//
///**
// *
// * @Stole By Hoàng Việt💖
// *
// */
//public class martialCongressPVP extends PVP {
//
//    public boolean round;
//    
//    public martialCongressPVP(Player pl1, Player pl2) {
//        this.player1 = pl1;
//        this.player2 = pl2;
//        this.typePVP = TYPE_PVP_MARTIAL_CONGRESS;
//    }
//
//    @Override
//    public void sendResultMatch(Player winer, Player loser, byte typeWin) {
//        switch (typeWin) {
//            case PVP.TYPE_DIE:
//                Service.getInstance().sendThongBao(winer, "Đối thủ đã kiệt sức, bạn thắng được đi vào vòng tiếp theo");
//                Service.getInstance().sendThongBao(loser, "Bạn đã thua vì đã kiệt sức, hãy quay lại vào giải sắp tới");
//                break;
//            case PVP.TYPE_LEAVE_MAP:
//                Service.getInstance().sendThongBao(winer, "Đối thủ sợ quá bỏ chạy, bạn thắng được đi vào vòng tiếp theo");
//                Service.getInstance().sendThongBao(loser, "Đạn bị xử thua vì đã bỏ chạy");
//                break;
//        }
//    }
//
//    @Override
//    public void reward(Player plWin) {
//    }
//
//}
