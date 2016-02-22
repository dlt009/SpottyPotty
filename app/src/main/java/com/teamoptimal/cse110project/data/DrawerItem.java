package com.teamoptimal.cse110project.data;

/**
 * Created by dltan on 2/21/2016.
 */
public class DrawerItem {
    String itemTitle;
    String itemDist;
    double itemRate;

    public DrawerItem(String itemTitle, String itemDist,
                      double itemRate) {
        super();
        this.itemTitle = itemTitle;
        this.itemDist = itemDist;
        this.itemRate = itemRate;
    }

    public String getItemTitle() {
        return itemTitle;
    }
    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemDist() {
        return itemDist;
    }
    public void setItemDist(String itemDist) {
        this.itemDist = itemDist;
    }

    public double getItemRate() {
        return itemRate;
    }
    public void setItemRate(double itemRate) {
        this.itemRate = itemRate;
    }

}
