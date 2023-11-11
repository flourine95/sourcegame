package com.girlkun.services.func;

import com.girlkun.models.item.Item;

import java.util.ArrayList;
import java.util.List;


public class CombineNew {

    public long lastTimeCombine;

    public List<Item> itemsCombine;
    public int typeCombine;

    public int goldCombine;
    public int gemCombine;
    public float ratioCombine;
    public int countDaNangCap;
    public short countDaBaoVe;

    public int diemNangCap;
    public int daNangCap;
    public float tiLeNangCap;
    public int rubyCombine;

    public CombineNew() {
        this.itemsCombine = new ArrayList<>();
    }

    public void setTypeCombine(int type) {
        this.typeCombine = type;
    }

    public void clearItemCombine() {
        this.itemsCombine.clear();
    }

    public void clearParamCombine() {
        this.goldCombine = 0;
        this.gemCombine = 0;
        this.ratioCombine = 0;
        this.countDaNangCap = 0;
        this.countDaBaoVe = 0;
        this.diemNangCap = 0;
        this.daNangCap = 0;
        this.tiLeNangCap = 0;
        this.rubyCombine = 0;
    }

    public void dispose() {
        this.itemsCombine = null;
    }
}
