package com.teamoptimal.cse110project.data;

/**
 * Created by Tony on 2/9/2016.
 */
public class ReviewItem {
    private String comments;
    private Float rating;

    public ReviewItem(String comments, Float rating){
        super();
        this.rating = rating;
        this.comments = comments;
    }

    public String getComments(){
        return this.comments;
    }
    public void setComments(String newComment) { this.comments = newComment; }

    public Float getRating() { return this.rating; }
    public void setRating(Float rating){ this.rating = rating; }
}
