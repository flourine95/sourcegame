package com.girlkun.models.matches.pvp;

import com.girlkun.models.map.Map;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.server.Manager;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;

/**
 */
public class DaiHoiVoThuatService {
    
    private static DaiHoiVoThuatService instance;

    public static DaiHoiVoThuatService gI(DaiHoiVoThuat dh) {
        if (instance == null) {
            instance = new DaiHoiVoThuatService(dh);
        }
        return instance;
    }
    
    public DaiHoiVoThuat daihoi;
    
    public DaiHoiVoThuatService(DaiHoiVoThuat dh){
        daihoi = dh;
    }
    
    public void Update() {
        if(daihoi == null){
            return;
        }
        int countNull = 0;
        int maxZone = 0;
        if(Util.contains(daihoi.Time, String.valueOf(DaiHoiVoThuat.gI().Hour)) && daihoi.round != 1 ){
            Map map = Manager.MAPS.get(51);
            if(map != null){
                maxZone = map.zones.size();
                for(Zone zones : map.zones){
                    if(zones != null && zones.getPlayers().size() <= 0){
                        countNull++;
                    }
                }
                if(countNull >= maxZone){
                    daihoi.listReg.addAll(daihoi.listPlayerWait);
                    daihoi.listPlayerWait.clear();
                }
            }
        }
        daihoi.listReg.stream().filter((me) -> (me != null && me.zone.map.mapId != 52 && me.zone.map.mapId != 51 && DaiHoiVoThuat.gI().Minutes >= daihoi.min_start_temp)).map((me) -> {
            Service.gI().sendThongBao(me, "Bạn đã bị tước quyền thi đấu do không có mặt kịp giờ");
            return me;
        }).forEachOrdered((me) -> {
            daihoi.listReg.remove(me);
        });
        if (DaiHoiVoThuat.gI().Second == 0 && DaiHoiVoThuat.gI().Minutes < daihoi.min_start_temp) {
            if (daihoi.listReg.size() > 1) {
                Service.gI().sendThongBao(daihoi.listReg, "Vòng " + daihoi.round + " sẽ bắt đầu sau " + (daihoi.min_start_temp - DaiHoiVoThuat.gI().Minutes) + " phút nữa");
            }
        } else if (DaiHoiVoThuat.gI().Minutes >= daihoi.min_start) {
            if (daihoi.listReg.size() > 1) {
                if (daihoi.listReg.size() % 2 == 0) {
                    if(DaiHoiVoThuat.gI().Minutes >= daihoi.min_start_temp){
                        MatchDHVT();
                    }
                } else {
                    Player pl = daihoi.listReg.get(Util.nextInt(0,daihoi.listReg.size() - 1));
                    if(pl != null){
                        daihoi.listPlayerWait.add(pl);
                        daihoi.listReg.remove(pl);
                        Service.gI().sendThongBao(pl, "Chúc mừng bạn đã may mắn lọt vào vòng trong");
                    }
                    MatchDHVT();
                }
            }else if(daihoi.listReg.size() == 1 && daihoi.listPlayerWait.isEmpty() && countNull >= maxZone){
                Service.gI().sendThongBao(daihoi.listReg.get(0), "Bạn đã vô địch giải " + daihoi.NameCup);
                daihoi.round = 1;
                daihoi.listReg.clear();
                daihoi.listPlayerWait.clear();
                daihoi.min_start_temp = daihoi.min_start;
            }
        }
    }
    
    public void MatchDHVT(){
        int rOld = daihoi.round;
        if(rOld == daihoi.round){
            int countMatch = daihoi.listReg.size() / 2;
            for(int i = 0; i < countMatch; i++) {
                Map map = Manager.MAPS.get(51);
                Zone z = null;
                if(map != null){
                    for(Zone zones : map.zones){
                        if(zones != null && zones.getHumanoids().size() <= 0){
                            z = zones;
                        }
                    }
                }
                Player pl1 = daihoi.listReg.get(Util.nextInt(0 , daihoi.listReg.size() - 1));
                if(pl1 != null && pl1.isPl()&& pl1.zone.map.mapId == 52){
                    pl1.isWin = false;
                    ChangeMapService.gI().changeMap(pl1, z, 385, 312);
                    pl1.nPoint.setFullHpMp();
                    Service.gI().point(pl1);
                    daihoi.listReg.remove(pl1);
                }else{
                    daihoi.listReg.remove(pl1);
                }
                Player pl2 = daihoi.listReg.get(Util.nextInt(0 , daihoi.listReg.size() - 1));
                if(pl2 != null && pl2.isPl()&& pl2.zone.map.mapId == 52){
                    pl2.isWin= false;
                    ChangeMapService.gI().changeMap(pl2, z, 385, 312);
                    pl2.nPoint.setFullHpMp();
                    Service.gI().point(pl2);
                    daihoi.listReg.remove(pl2);
                }else{
                    daihoi.listReg.remove(pl2);
                }
                PVPDaiHoi thachDau = new PVPDaiHoi(pl1, pl2, daihoi.gold,daihoi,System.currentTimeMillis());
                // đoạn này kéo 2 thằng lên sàn này
            }
            daihoi.round += 1;
            daihoi.min_start_temp += 2;
        }
    }
    
    public boolean CanReg(Player pl) {
        return daihoi != null &&pl.isPl()&& Util.contains(daihoi.Time, String.valueOf(DaiHoiVoThuat.gI().Hour)) && DaiHoiVoThuat.gI().Minutes <= daihoi.min_limit && !PlayerExits((int)pl.id);
    }
    
    public String Giai(Player pl) {
        if (daihoi != null && PlayerExits((int)pl.id)) {
            return "Đại hội võ thuật sẽ bắt đầu sau "+(daihoi.min_start_temp - DaiHoiVoThuat.gI().Minutes)+" phút nữa";
        } else if (daihoi != null && Util.contains(daihoi.Time, String.valueOf(DaiHoiVoThuat.gI().Hour)) && DaiHoiVoThuat.gI().Minutes <= daihoi.min_limit) {
            return "Chào mừng bạn đến với đại hội võ thuật\nGiải " + daihoi.NameCup + " đang có " + daihoi.listReg.size() + " người đăng ký thi đấu";
        }
        return "Đã hết thời gian đăng ký vui lòng đợi đến giải đấu sau";
    }

    public boolean PlayerExits(int id) {
        if(daihoi != null){
            for(int i = 0 ; i < daihoi.listReg.size();i++){
                Player pl = daihoi.listReg.get(i);
                if(pl != null && pl.isPl() && pl.id == id){
                    return true;
                }
            }
        }
        return false;
    }
    
    public void removePlayer(Player pl){
        if(daihoi != null && daihoi.listReg.contains(pl)){
            daihoi.listReg.remove(pl);
        }
    }
    
    public void removePlayerWait(Player pl){
        if(daihoi != null && daihoi.listPlayerWait.contains(pl)){
            daihoi.listPlayerWait.remove(pl);
        }
    }

    public void Reg(Player player) {
        if(daihoi == null){
            return;
        }
        boolean isReg = false;
        if (daihoi.gem > 0) {
            if (player.inventory.ruby >= daihoi.gem) {
                player.inventory.ruby -= daihoi.gem;
                isReg = true;
            } else if (player.inventory.gem >= daihoi.gem) {
                player.inventory.gem -= daihoi.gem;
                isReg = true;
            } else {
                Service.gI().sendThongBao(player, "Bạn Không Đủ Ngọc Để Đăng Ký");
                isReg = false;
            }
        } else if (daihoi.gold > 0) {
            if (player.inventory.gold >= daihoi.gold) {
                player.inventory.gold -= daihoi.gold;
                isReg = true;
            } else {
                Service.gI().sendThongBao(player, "Bạn Không Đủ Vàng Để Đăng Ký");
                isReg = false;
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn Không Thể Đăng Ký Giải Đấu Này");
        }
        if (isReg) {
            if(player.isPl()){
                daihoi.listReg.add(player);
                Service.gI().sendMoney(player);
                Service.gI().sendThongBao(player, "Bạn đã đăng ký thành công!Vui lòng không rời khỏi đại hội võ thuật để tránh bị tước quyền thi đấu!!");
            }
        }
    }
}
