package com.girlkun.models.skill;

import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.Service;

import java.util.ArrayList;
import java.util.List;


public class PlayerSkill {

    private Player player;
    public List<Skill> skills;
    public Skill skillSelect;
    
    public static final int TIME_MUTIL_CHUONG = 60000;

    public PlayerSkill(Player player) {
        this.player = player;
        skills = new ArrayList<>();
    }

    public Skill getSkillbyId(int id) {
        for (Skill skill : skills) {
            if (skill.template.id == id) {
                return skill;
            }
        }
        return null;
    }

    public byte[] skillShortCut = new byte[5];

    public void sendSkillShortCut() {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 61);
            msg.writer().writeUTF("KSkill");
            msg.writer().writeInt(skillShortCut.length);
            msg.writer().write(skillShortCut);
            player.sendMessage(msg);
            msg.cleanup();
            msg = Service.getInstance().messageSubCommand((byte) 61);
            msg.writer().writeUTF("OSkill");
            msg.writer().writeInt(skillShortCut.length);
            msg.writer().write(skillShortCut);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    
    

    public boolean prepareQCKK;
    public boolean prepareTuSat;
    public boolean prepareLaze;
    
    public long lastTimePrepareQCKK;
    public long lastTimePrepareTuSat;
    public long lastTimePrepareLaze;

    public byte getIndexSkillSelect() {
        return switch (skillSelect.template.id) {
            case Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAIOKEN, Skill.LIEN_HOAN -> 1;
            case Skill.KAMEJOKO, Skill.ANTOMIC, Skill.MASENKO -> 2;
            default -> 3;
        };
    }

    public byte getSizeSkill() {
        byte size = 0;
        for (Skill skill : skills) {
            if (skill.skillId != -1) {
                size++;
            }
        }
        return size;
    }
    
    public void dispose(){
        if(this.skillSelect != null){
            this.skillSelect.dispose();
        }
        if(this.skills != null){
            for(Skill skill : this.skills){
                skill.dispose();
            }
            this.skills.clear();
        }
        this.player = null;
        this.skillSelect = null;
        this.skills = null;
    }
}
