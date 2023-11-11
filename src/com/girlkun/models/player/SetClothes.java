package com.girlkun.models.player;

import com.girlkun.models.item.Item;

public class SetClothes {
    private Player player;


    public byte songoku;
    public byte thienXinHang;
    public byte kirin;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;

    public byte kakarot;
    public byte cadic;
    public byte nappa;

    public byte tabi;
    public byte setDHD;
    public byte setDTS;
    public byte setDTL;
    public byte tinhan;
    public byte nguyetan;
    public byte nhatan;

    public boolean godClothes;
    public int ctHaiTac = -1;

    public SetClothes(Player player) {
        this.player = player;
    }

    public void setup() {
        setDefault();
        setupSKT();
        setupAN();
        setupDTS();
        setupDHD();
        setupDTL();
        this.godClothes = true;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id > 567 || item.template.id < 555) {
                    this.godClothes = false;
                    break;
                }
            } else {
                this.godClothes = false;
                break;
            }
        }
        Item ct = this.player.inventory.itemsBody.get(5);
        if (ct.isNotNullItem()) {
            switch (ct.template.id) {
                case 618, 619, 620, 621, 622, 623, 624, 626, 627 -> this.ctHaiTac = ct.template.id;
            }
        }
    }

    private void setupSKT() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 129, 141 -> {
                            isActSet = true;
                            songoku++;
                        }
                        case 127, 139 -> {
                            isActSet = true;
                            thienXinHang++;
                        }
                        case 128, 140 -> {
                            isActSet = true;
                            kirin++;
                        }
                        case 131, 143 -> {
                            isActSet = true;
                            ocTieu++;
                        }
                        case 132, 144 -> {
                            isActSet = true;
                            pikkoroDaimao++;
                        }
                        case 130, 142 -> {
                            isActSet = true;
                            picolo++;
                        }
                        case 135, 138 -> {
                            isActSet = true;
                            nappa++;
                        }
                        case 133, 136 -> {
                            isActSet = true;
                            kakarot++;
                        }
                        case 134, 137 -> {
                            isActSet = true;
                            cadic++;
                        }
                    }
                    if (isActSet) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupAN() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSett = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 34 -> {
                            isActSett = true;
                            tinhan++;
                        }
                        case 35 -> {
                            isActSett = true;
                            nguyetan++;
                        }
                        case 36 -> {
                            isActSett = true;
                            nhatan++;
                        }
                    }
                    if (isActSett) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }

    private void setupDTS() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 21) {
                        if (io.param == 120) {
                            setDTS++;
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupDHD() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 21) {
                        if (io.param == 80) {
                            setDHD++;
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupDTL() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 21) {
                        if (io.param == 15) {
                            setDTL++;
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.setDHD = 0;
        this.setDTS = 0;
        this.setDTL = 0;
        this.tabi = 0;
        this.tinhan = 0;
        this.nhatan = 0;
        this.nguyetan = 0;
        this.godClothes = false;
        this.ctHaiTac = -1;
    }

    public void dispose() {
        this.player = null;
    }
}
