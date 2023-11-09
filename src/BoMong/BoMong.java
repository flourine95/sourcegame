
package BoMong;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.Template;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BoMong {

    public static final String TEN_SIEU_CAP = "%1";
    public static final String TEN_NGUOI_BAN_HANG = "%2";
    public static final String TEN_SKILL = "%3";

    public Player player;

    public int numPvpWin;
    public int numSkillChuong;
    public int numFly;
    public int numKillMobFly;
    public int numKillNguoiRom;
    public long numHourOnline;
    public int numGivePea;
    public int numSellItem;
    public int numPayMoney;
    public int numKillSieuQuai;
    public int numHoiSinh;
    public int numSkillDacBiet;
    public int numPickGem;

    public List<Boolean> listReceiveGem;

    public BoMong(Player pl) {
        this.player = pl;
        listReceiveGem = new ArrayList<>(Manager.ACHIEVEMENTS.size());
    }

    public void Show() {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(0); // action
            msg.writer().writeByte(listReceiveGem.size()); //numArrchivement
            for (Template.AchievementTemplate temp : Manager.ACHIEVEMENTS) {
               
                msg.writer().writeUTF(temp.getInfo1()); //info1
                msg.writer().writeUTF( //info2
                        this.SwitchName(player, temp.getInfo2())
                        + " (" + Util.numberToMoney(getCount(temp.getIndex()))
                        + "/"
                        + Util.numberToMoney((long) temp.getCount_Purpose()) + ")");
                msg.writer().writeShort(temp.getGem()); //money
                msg.writer().writeBoolean(getCount(temp.getIndex()) >= temp.getCount_Purpose()); //isfinish
                msg.writer().writeBoolean(this.listReceiveGem.get(temp.getIndex())); //isreceiv
            }
            this.player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(BoMong.class, e);
            e.getStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void receiveGem(int index) {
        Template.AchievementTemplate temp = Manager.ACHIEVEMENTS.stream().filter(ac -> ac.getIndex() == index).findFirst().orElse(null);

        if (temp != null) {
            Message msg = null;
            try {
                msg = new Message(-76);
                msg.writer().writeByte(1); // action
                msg.writer().writeByte(index); // index
                this.player.sendMessage(msg);
                msg.cleanup();
            } catch (IOException e) {
                Logger.logException(BoMong.class, e);
                System.out.println("        loi");
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
            this.listReceiveGem.set(index, Boolean.TRUE);
            this.player.inventory.ruby += temp.getGem();
            Service.getInstance().sendMoney(this.player);
            Service.getInstance().sendThongBao(this.player, "Nhận thành công " + temp.getGem() + " hồng ngọc");
        } else {
            Service.getInstance().sendThongBao(this.player, "Không có phần thưởng");
        }
    }

    private String SwitchName(Player player, String text) {
        byte gender = player.gender;
        text = text.replaceAll(TEN_SIEU_CAP, gender == ConstPlayer.TRAI_DAT
                ? "Thần Trái Đất" : (gender == ConstPlayer.NAMEC
                        ? "Thần Namếc" : "Thần Xayda"));
        text = text.replaceAll(TEN_NGUOI_BAN_HANG, gender == ConstPlayer.TRAI_DAT
                ? "Bunma" : (gender == ConstPlayer.NAMEC
                        ? "Dende" : "Appule"));
        text = text.replaceAll(TEN_SKILL, gender == ConstPlayer.TRAI_DAT
                ? "Super Kamejoko" : (gender == ConstPlayer.NAMEC
                        ? "Ma Phong Ba" : "Cadic Liên hoàn chưởng"));
        return text;
    }

    public void plusCount(int indexAchie) {
        switch (indexAchie) {
            case 0:
            case 1:
                this.player.nPoint.power++;
                break;
            case 2:
                this.player.playerTask.taskMain.id++;
                break;
            case 3:
                this.numPvpWin++;
                break;
            case 4:
                this.numSkillChuong++;
                break;
            case 5:
                this.numFly++;
                break;
            case 6:
                this.numKillMobFly++;
                break;
            case 7:
                this.numKillNguoiRom++;
                break;
            case 8:
                this.numHourOnline++;
                break;
            case 9:
                this.numGivePea++;
                break;
            case 10:
                this.numSellItem++;
                break;
            case 11:
                this.numPayMoney++;
                break;
            case 12:
                this.numKillSieuQuai++;
                break;
            case 13:
                this.numHoiSinh++;
                break;
            case 14:
                this.numSkillDacBiet++;
                break;
            case 15:
                this.numPickGem++;
                break;
            default:
                break;
        }
    }

    private long getCount(int indexAchie) {
        return switch (indexAchie) {
            case 0, 1 -> this.player.nPoint.power;
            case 2 -> this.player.playerTask.taskMain.id;
            case 3 -> this.numPvpWin;
            case 4 -> this.numSkillChuong;
            case 5 -> this.numFly;
            case 6 -> this.numKillMobFly;
            case 7 -> this.numKillNguoiRom;
            case 8 -> (this.numHourOnline / 60 / 60);
            case 9 -> this.numGivePea;
            case 10 -> this.numSellItem;
            case 11 -> this.numPayMoney;
            case 12 -> this.numKillSieuQuai;
            case 13 -> this.numHoiSinh;
            case 14 -> this.numSkillDacBiet;
            case 15 -> this.numPickGem;
            default -> 0;
        };
    }

    public void dispose() {
        this.player = null;
    }
}
