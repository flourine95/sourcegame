package com.girlkun.models.player;


public class EffectFlagBag {
    public boolean useVoOc;
    public boolean useCayKem;
    public boolean useCaHeo;
    public boolean useConDieu;
    public boolean useDieuRong;
    public boolean useMeoMun;
    public boolean useXienCa;
    public boolean usePhongHeo;

    public void reset() {
        this.useVoOc = false;
        this.useVoOc = false;
        this.useCayKem = false;
        this.useCaHeo = false;
        this.useConDieu = false;
        this.useDieuRong = false;
        this.useMeoMun = false;
        this.useXienCa = false;
        this.usePhongHeo = false;
    }
    
    public void dispose(){
    }
}
