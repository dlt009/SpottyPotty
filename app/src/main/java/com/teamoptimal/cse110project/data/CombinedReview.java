package com.teamoptimal.cse110project.data;

/**
 * Created by Tony on 2/9/2016.
 */
public class CombinedReview {

    private String users;
    private String comments;
    private Double rating;

    public CombinedReview(String users, Double rating, String comments){
        super();
        this.users = users;
        this.rating = rating;
        this.comments = comments;
    }

    public String getUsers(){
        return users;
    }

    public String getComments(){
        return comments;
    }

    public void setUsers(String newUser) {
        this.users = newUser;
    }

    public void setComments(String newComment){
        this.comments = newComment;
    }
}
