package com.teamoptimal.cse110project.data;

public class ReviewItem {
    private String comments;
    private Float rating;
    private String reviewID;

    public ReviewItem(String comments, Float rating, String reviewID){
        super();
        this.rating = rating;
        this.comments = comments;
        this.reviewID = reviewID;
    }

    public String getReviewID() { return reviewID; }
    public void setReviewID(String restroomID) { this.reviewID = reviewID; }

    public String getComments(){
        return this.comments;
    }
    public void setComments(String newComment) { this.comments = newComment; }

    public Float getRating() { return this.rating; }
    public void setRating(Float rating){ this.rating = rating; }
}
