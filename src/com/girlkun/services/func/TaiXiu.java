/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girlkun.services.func;

import com.girlkun.models.player.Player;
import com.girlkun.server.Client;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DEV Ăn Trộm
 */
public class TaiXiu  implements Runnable{
    public int goldTai;
    public int goldXiu;
    public boolean ketquaTai = false;
    public boolean ketquaXiu = false;
    public boolean ketquaTamhoa = false;
    
    public boolean baotri = false;
    public long lastTimeEnd;
    public List<Player> PlayersTai = new ArrayList<>();
    public List<Player> PlayersXiu = new ArrayList<>();
    private static TaiXiu instance;
    public int x,y,z;
    
    public static TaiXiu gI() {
        if (instance == null) {
            instance = new TaiXiu();
        }
        return instance;
    }
    
    public void addPlayerXiu(Player pl){
        if(!PlayersXiu.equals(pl)){
            PlayersXiu.add(pl);
        }
    }
    
    public void addPlayerTai(Player pl){
        if(!PlayersTai.equals(pl)){
            PlayersTai.add(pl);
        }
    }
    
    public void removePlayerXiu(Player pl){
        if(PlayersXiu.equals(pl)){
            PlayersXiu.remove(pl);
        }
    }
    
    public void removePlayerTai(Player pl){
        if(PlayersTai.equals(pl)){
            PlayersTai.remove(pl);
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try{
                if(((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis())/1000) <= 0){
                    int x,y,z;
                    // Thực hiện các hành động sau khi chờ 10 giây
                    if (TaiXiu.gI().goldTai >= TaiXiu.gI().goldXiu){
                        if (Util.isTrue(60, 100)){
                            x = Util.nextInt(1, 2);
                            y = Util.nextInt(1, 3);
                            z = Util.nextInt(1, 3);
                            TaiXiu.gI().x =x ;
                            TaiXiu.gI().y =y ;
                            TaiXiu.gI().z =z ;
                        } else {
                            x = Util.nextInt(3, 6);
                            y = Util.nextInt(4, 6);
                            z = Util.nextInt(4, 6);
                            TaiXiu.gI().x =x ;
                            TaiXiu.gI().y =y ;
                            TaiXiu.gI().z =z ;
                        }
                    } else {
                        if (Util.isTrue(60, 100)){
                            x = Util.nextInt(3, 6);
                            y = Util.nextInt(4, 6);
                            z = Util.nextInt(4, 6);
                            TaiXiu.gI().x =x ;
                            TaiXiu.gI().y =y ;
                            TaiXiu.gI().z =z ;
                        } else {
                            x = Util.nextInt(1, 2);
                            y = Util.nextInt(1, 3);
                            z = Util.nextInt(1, 3);
                            TaiXiu.gI().x =x ;
                            TaiXiu.gI().y =y ;
                            TaiXiu.gI().z =z ;
                        }
                    }
                    int tong = (x + y + z);
                    
                    if (tong > 3 && tong <11) {
                        ketquaTamhoa = false;
                        ketquaXiu = true;
                        ketquaTai = false;
                    }
                    if (tong > 10){
                        ketquaTamhoa = false;
                        ketquaXiu = false;
                        ketquaTai = true;
                    }
                    if (x==y && y==z && z==x){
                        ketquaTamhoa = true;
                        ketquaXiu = false;
                        ketquaTai = false;
                    }
                   
                    if (ketquaTai == true){
                        if( !TaiXiu.gI().PlayersTai.isEmpty()){
                                for (int i=0; i< PlayersTai.size();i++){
                                    Player pl = this.PlayersTai.get(i);
                                    if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        int goldC = pl.goldTai + pl.goldTai * 80 / 100;
                                        Service.gI().sendThongBao(pl, "Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(TÀI)\n\n|1|Bạn đã chiến thắng!!");
                                        Service.gI().sendThongBao(pl, "Chúc mừng bạn đã dành chiến thắng và nhận được " + Util.format(goldC) +" Hồng ngọc");
                                        pl.inventory.ruby += goldC;
                                        pl.taixiu.win += pl.goldTai * 80 / 100;
                                        Service.gI().sendMoney(pl);
                                        InventoryServiceNew.gI().sendItemBags(pl);
                                    }
                                }
                        }for (int i=0; i< PlayersXiu.size();i++){
                                    Player pl = this.PlayersXiu.get(i);
                                    if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        Service.gI().sendThongBao(pl, "Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(TÀI)\n\n|7|Trắng tay gòi, chơi lại đi!!!");
                                    }
                                }
                    } else if (ketquaXiu == true){
                        if (!TaiXiu.gI().PlayersXiu.isEmpty()){
                                for (int i=0; i< PlayersXiu.size();i++){
                                    Player pl = this.PlayersXiu.get(i);
                                if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        int goldC = pl.goldXiu + pl.goldXiu * 80 / 100;
                                        Service.gI().sendThongBao(pl,"Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(XỈU)\n\n|1|Bạn đã chiến thắng!!");
                                        Service.gI().sendThongBao(pl, "Chúc mừng bạn đã dành chiến thắng và nhận được " + Util.format(goldC) +" Hồng ngọc");
                                                pl.inventory.ruby += goldC;
                                        pl.taixiu.win += pl.goldXiu * 80 / 100;
                                                Service.gI().sendMoney(pl);
                                        InventoryServiceNew.gI().sendItemBags(pl);
                                    }
                                }
                        }for (int i=0; i< PlayersTai.size();i++){
                                    Player pl = this.PlayersTai.get(i);
                                    if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        Service.gI().sendThongBao(pl, "Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(XỈU)\n\n|7|Trắng tay gòi, chơi lại đi!!!");
                                    }
                                }
                    } else {
                        for (int i=0; i< PlayersTai.size();i++){
                                    Player pl = this.PlayersTai.get(i);
                                    if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        Service.gI().sendThongBao(pl, "Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(TAM HOA)\n\n|7|Hahaha Nhà cái lụm hết nha!!!");
                                    }
                                }
                        for (int i=0; i< PlayersXiu.size();i++){
                                    Player pl = this.PlayersXiu.get(i);
                                    if(pl != null && Client.gI().getPlayer(pl.name) != null){
                                        Service.gI().sendThongBao(pl, "Số hệ thống quay ra\n" + x + " : "
                                                        + y + " : " + z + "\n|5|Tổng là : " + tong + "\n(TAM HOA)\n\n|7|Hahaha Nhà cái lụm hết nha!!!");
                                    }
                                }
                    }
                    for(int i = 0 ; i < TaiXiu.gI().PlayersTai.size();i++){
                        Player pl = TaiXiu.gI().PlayersTai.get(i);
                        if(pl != null){
                            pl.goldTai = 0;
                        }
                    }
                    for(int i = 0 ; i < TaiXiu.gI().PlayersXiu.size();i++){
                        Player pl = TaiXiu.gI().PlayersXiu.get(i);
                        if(pl != null){
                            pl.goldXiu = 0;
                        }
                    }
                    ketquaXiu = false;
                    ketquaTai = false;
                    ketquaTamhoa = false;
                    TaiXiu.gI().goldTai = 0;
                    TaiXiu.gI().goldXiu = 0;
                    TaiXiu.gI().PlayersTai.clear();
                    TaiXiu.gI().PlayersXiu.clear();
                    TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 100000;
//                    GirlkunDB.executeUpdate("UPDATE player SET Tai_xiu = JSON_REPLACE(JSON_REPLACE(Tai_xiu, '$[0]', 0), '$[1]', 0)");
                }
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }
}
