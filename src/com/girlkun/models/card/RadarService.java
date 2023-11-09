package com.girlkun.models.card;

import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dev Duy Peo
 */
public class RadarService {
    public List<RadarCard> RADAR_TEMPLATE = new ArrayList<>(); 
    
    private static RadarService instance;
    public static RadarService gI() {
        if (instance == null) {
            instance = new RadarService();
        }
        return instance;
    }
    
    public void sendRadar(Player pl,List<Card> cards){
        try{
            Message m = new Message(127);
            m.writer().writeByte(0);
            m.writer().writeShort(RadarService.gI().RADAR_TEMPLATE.size());
            for(RadarCard radar : RadarService.gI().RADAR_TEMPLATE){
                Card card = cards.stream().filter(c -> c.Id == radar.Id).findFirst().orElse(null);
                if (card == null){
                    card = new Card(radar.Max , radar.Options);
                }
                m.writer().writeShort(radar.Id);
                m.writer().writeShort(radar.IconId);
                m.writer().writeByte(radar.Rank);
                m.writer().writeByte(card.Amount);  //amount
                m.writer().writeByte(card.MaxAmount);  //max_amount
                m.writer().writeByte(radar.Type);  //type 0: monster, 1: charpart
                switch (radar.Type)
                {
                    case 0 : 
                        m.writer().writeShort(radar.Template); //Monster
                        break;
                    case 1:
                        m.writer().writeShort(radar.Head); //Head
                        m.writer().writeShort(radar.Body); //Body
                        m.writer().writeShort(radar.Leg); //Leg
                        m.writer().writeShort(radar.Bag); //bag
                        break;
                }
                m.writer().writeUTF(radar.Name);  //name
                m.writer().writeUTF(radar.Info);  //info
                m.writer().writeByte(card.Level);  //LEvel
                m.writer().writeByte(card.Used);  //use
                m.writer().writeByte(radar.Options.size());  //option radar
                for(OptionCard option : radar.Options){
                    m.writer().writeByte(option.id);  //id
                    m.writer().writeShort(option.param);  //param
                    m.writer().writeByte(option.active);  //ActiveCard
                }
            }
            m.writer().flush();
            pl.sendMessage(m);
            m.cleanup();
        }catch (Exception e){
                System.out.println("loi ne IOException 1 ");
        }
    }
    
    public void Radar1(Player pl,short id, int use){
        try{
            Message message = new Message(127);
            message.writer().writeByte(1);
            message.writer().writeShort(id);
            message.writer().writeByte(use);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        }catch (Exception e){
                System.out.println("loi ne IOException 2 ");
        }
    }
    
    public void RadarSetLevel(Player pl ,int id, int level)
    {
        try{
            Message message = new Message(127);
            message.writer().writeByte(2);
            message.writer().writeShort(id);
            message.writer().writeByte(level);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        }catch (Exception e){
        }
    }
    
    public void RadarSetAmount(Player pl,int id, int amount, int max_amount)
    {
        try{
            Message message = new Message(127);
            message.writer().writeByte(3);
            message.writer().writeShort(id);
            message.writer().writeByte(amount);
            message.writer().writeByte(max_amount);
            message.writer().flush();
            pl.sendMessage(message);
            message.cleanup();
        }catch (Exception e){
                System.out.println("loi ne IOException 3 ");
        }
    }
}
