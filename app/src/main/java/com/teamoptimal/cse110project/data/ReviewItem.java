package com.teamoptimal.cse110project.data;

public class ReviewItem {
    private String comments;
    private Float rating;
    private String reviewID;
    private int thumbsUp;
    private int thumbsDown;
    private int action;

    public ReviewItem(String comments, Float rating, String reviewID, int thUp, int thDown, int action) {
        super();
        this.rating = rating;
        this.comments = comments;
        this.reviewID = reviewID;
        this.thumbsUp = thUp;
        this.thumbsDown = thDown;
        this.action = action;
    }

    public String getReviewID() { return reviewID; }
    public void setReviewID(String restroomID) { this.reviewID = reviewID; }

    public String getComments(){
        return this.comments;
    }
    public void setComments(String newComment) { this.comments = newComment; }

    public Float getRating() { return this.rating; }
    public void setRating(Float rating){ this.rating = rating; }

    public int getThumbsUp() { return this.thumbsUp; }
    public void setThumbsUp(int thUpCount){ this.thumbsUp = thUpCount; }

    public int getThumbsDown() { return this.thumbsDown; }
    public void setThumbsDown(int thDownCount){ this.thumbsDown = thDownCount; }

    public int getAction() { return this.action; }
    public void setAction(int action) { this.action = action; }
}
