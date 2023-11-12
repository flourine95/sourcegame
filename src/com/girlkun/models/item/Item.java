package com.girlkun.models.item;

import com.girlkun.data.ItemData;
import com.girlkun.models.Template;
import com.girlkun.models.Template.ItemTemplate;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Item {


    public ItemTemplate template;

    public String info;

    public String content;

    public int quantity;

    public int quantityGD = 0;

    public List<ItemOption> itemOptions;

    public long createTime;

    public boolean isNotNullItem() {
        return this.template != null;
    }

    public Item() {
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public Item(short itemId) {
        this.template = ItemService.gI().getTemplate(itemId);
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public String getInfo() {
        StringBuilder strInfo = new StringBuilder();
        for (ItemOption itemOption : itemOptions) {
            strInfo.append(itemOption.getOptionString()).append("\n");
        }
        return strInfo.toString();
    }

    public String getContent() {
        return "Yêu cầu sức mạnh " + this.template.strRequire + " trở lên";
    }

    public void dispose() {
        this.template = null;
        this.info = null;
        this.content = null;
        if (this.itemOptions != null) {
            for (ItemOption io : this.itemOptions) {
                io.dispose();
            }
            this.itemOptions.clear();
        }
        this.itemOptions = null;
    }

    public int getIDBongTaiAfterUpLevel(int lvbt) {
        return switch (lvbt) {
            case 1 -> 921;
            case 2 -> 1165;
            case 3 -> 1129;
            default -> 0;
        };
    }

    public int getLevelBongTai() {
        return switch (template.id) {
            case 454 -> 1;
            case 921 -> 2;
            case 1165 -> 3;
            case 1129 -> 4;
            default -> 0;
        };
    }


    public static class ItemOption {
        public int param;
        public Template.ItemOptionTemplate optionTemplate;

        public ItemOption() {
        }

        public ItemOption(ItemOption io) {
            this.param = io.param;
            this.optionTemplate = io.optionTemplate;
        }

        public ItemOption(int tempId, int param) {
            this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
            this.param = param;
        }

        public ItemOption(Template.ItemOptionTemplate temp, int param) {
            this.optionTemplate = temp;
            this.param = param;
        }

        public String getOptionString() {
            return Util.replace(this.optionTemplate.name, "#", String.valueOf(this.param));
        }

        public void dispose() {
            this.optionTemplate = null;
        }

        @Override
        public String toString() {
            final String n = "\"";
            return "{"
                    + n + "id" + n + ":" + n + optionTemplate.id + n + ","
                    + n + "param" + n + ":" + n + param + n
                    + "}";
        }

        public boolean isOptionPhapSu() {
            return
                    isAddOptionPhapSu() ||
                            isOptionCheckedPhapSu();
        }

        public boolean isAddOptionPhapSu() {
            return
                    this.optionTemplate.id == 194 ||
                            this.optionTemplate.id == 195 ||
                            this.optionTemplate.id == 196 ||
                            this.optionTemplate.id == 197;
        }

        public boolean isOptionCheckedPhapSu() {
            return this.optionTemplate.id == 198;
        }
    }

    public boolean isDaPhaLe() {
        if (isNotNullItem()) {
            return template.type == 30 || template.id >= 14 && template.id <= 20 || template.id >= 1185 && template.id <= 1191;
        }
        return false;
    }

    public boolean isBuaTayPhapSu() {
        if (isNotNullItem()) {
            return template.id == 1236;
        }
        return false;
    }

    public boolean isManhTinhAn() {
        if (isNotNullItem()) {
            return template.id >= 1232 && template.id <= 1234;
        }
        return false;
    }

    public boolean isDaPhapSu() {
        if (isNotNullItem()) {
            return template.id == 1235;
        }
        return false;
    }


    public boolean isItemPhaLeHoa() {
        if (isNotNullItem()) {
            return (template.type < 5 || template.type == 32) && !isTrangBiHSD();
        }
        return false;
    }

    public boolean isItemPhapSu() {
        if (isNotNullItem()) {
            return (template.type == 5 ||
                    template.type == 11 ||
                    ItemData.list_dapdo.contains((int) template.id)) &&
                    !isTrangBiHSD();
        }
        return false;
    }

    public boolean isPotara() {
        if (isNotNullItem()) {
            return
                    template.id == 454 ||
                            template.id == 921 ||
                            template.id == 1165 ||
                            template.id == 1129;
        }
        return false;
    }

    public boolean isMVBT() {
        if (isNotNullItem()) {
            return template.id == 933;
        }
        return false;
    }

    public boolean isMHBT() {
        if (isNotNullItem()) {
            return template.id == 934;
        }
        return false;
    }

    public boolean isDXL() {
        if (isNotNullItem()) {
            return template.id == 935;
        }
        return false;
    }

    public boolean isSKH() {
        if (isNotNullItem()) {
            return itemOptions.stream().anyMatch(itemOption -> itemOption.optionTemplate.id >= 127 && itemOption.optionTemplate.id <= 135);
        }
        return false;
    }

    public boolean isDTS() {
        if (isNotNullItem()) {
            return this.template.id >= 1048 && this.template.id <= 1062;
        }
        return false;
    }

    public boolean isDTL() {
        if (isNotNullItem()) {
            return this.template.id >= 555 && this.template.id <= 567;
        }
        return false;
    }

    public boolean isCongThuc() {
        if (isNotNullItem()) {
            return this.template.id >= 1071 && this.template.id <= 1073;
        }
        return false;
    }

    public boolean isDHD() {
        if (isNotNullItem()) {
            return this.template.id >= 650 && this.template.id <= 662;
        }
        return false;
    }

    public boolean isManhTS() {
        if (isNotNullItem()) {
            return this.template.id >= 1066 && this.template.id <= 1070;
        }
        return false;
    }

    public boolean haveOption(int idOption) {
        if (this.isNotNullItem()) {
            return this.itemOptions.stream().anyMatch(op -> op != null && op.optionTemplate.id == idOption);
        }
        return false;
    }

    public boolean isTrangBiHSD() {
        return InventoryServiceNew.gI().hasOptionTemplateId(this, 93);
    }

    public String typeName() {
        return switch (this.template.type) {
            case 0 -> "Áo";
            case 1 -> "Quần";
            case 2 -> "Găng";
            case 3 -> "Giày";
            case 4 -> "Rada";
            default -> "";
        };
    }

    public byte typeIdManh() {
        return switch (this.template.id) {
            case 1066 -> 0;
            case 1067 -> 1;
            case 1070 -> 2;
            case 1068 -> 3;
            case 1069 -> 4;
            default -> -1;
        };
    }

    public String typeNameManh() {
        return switch (this.template.id) {
            case 1066 -> "Áo";
            case 1067 -> "Quần";
            case 1070 -> "Găng";
            case 1068 -> "Giày";
            case 1069 -> "Nhẫn";
            default -> "";
        };
    }
}
