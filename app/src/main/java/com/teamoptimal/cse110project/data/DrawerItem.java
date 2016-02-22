package com.teamoptimal.cse110project.data;

import android.media.Rating;
import android.widget.RatingBar;

/**
 * Created by dltan on 2/21/2016.
 */
public class DrawerItem {
    String itemTitle;
    String itemDist;
    double itemRate;
    int imgResID;

    public DrawerItem(String itemTitle, String itemDist,
                      double itemRate, int imgResID) {
        super();
        this.itemTitle = itemTitle;
        this.itemDist = itemDist;
        this.itemRate = itemRate;
        this.imgResID = imgResID;
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

    public int getImgResID() {
        return imgResID;
    }
    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }
}
