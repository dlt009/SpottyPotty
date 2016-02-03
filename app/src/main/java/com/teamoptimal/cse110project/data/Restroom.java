package com.teamoptimal.cse110project.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;

@DynamoDBTable(tableName = "Y4R_Restrooms")
public class Restroom{
    private int id;
    private static int currID;
    private double[] loc;
    private String user_email;
    private String name;
    private char[] tags;
    private String floor;
    private String desc;
    private double rating;


    public Restroom (){
        id = 0;
        user_email = null;
        loc = new double[2];
        name = "Uninitialized";
        tags = new char[31];
        this.initializeTags();
        floor = "1";
        desc = "None Given";
        rating = 0.00;
    }

    private void initializeTags(){
        for(char c: tags){
            c='0';
        }
    }

    public void setUser(User maker){
        user_email = maker.getEmail();
    }

    @DynamoDBAttribute (attributeName = "User")
    public String getUser(){return user_email;}

    public void setId(int i){ id = i;}

    public void setLoc(double lo, double la){
        loc[0] = lo;
        loc[1] = la;
    }

    public void setFloor(String level){floor = level;}

    public void setName(String nom){name = nom;}

    public void setTag(int index, boolean choice){
        if(choice) tags[index] = '1';
        else tags[index] = '0';
    }

    public void updateRating(int[] ratings){
        int i = 0;
        int sum =0;
        for(int s:ratings){
            sum += s;
            i++;
        }
        double val = sum/i;
        setRating(val);
    }

    public void setDesc(String descript){ desc = descript;}

    public void setRating(double val){rating = val;}

    @DynamoDBAttribute (attributeName = "Location")
    public double[] getLoc(){return loc;}
    public double getLongit(){return loc[0];}
    public double getLatit(){return loc[1];}

    @DynamoDBHashKey (attributeName = "ID")
    public int getId(){ return id;}

    @DynamoDBAttribute (attributeName = "Name")
    public String getName(){return name;}

    @DynamoDBAttribute (attributeName = "Tags")
    public String getTags(){return tags.toString();}

    @DynamoDBAttribute (attributeName = "Floor")
    public String getFloor(){return floor;}

    @DynamoDBAttribute(attributeName = "Description")
    public String getDesc(){return desc; }

    @DynamoDBAttribute(attributeName = "Rating")
    public double getRating(){return rating;}

    public boolean isInitialized(){
        if(loc[0] != && user_email != null && !name.equals("Uninitialized")){
            setId(++currID);
            return true;
        }
        else return false;
    }

}