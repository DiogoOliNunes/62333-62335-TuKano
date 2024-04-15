package tukano.api;

import jakarta.persistence.*;

@Entity
public class Follow {

    @Id
    private String followId;
    private String follower;
    private String followed;

    public Follow() {}

    public Follow(String followId, String follower, String followed) {
        this.followId = followId;
        this.follower = follower;
        this.followed = followed;
    }

    public String getFollowId() { return followId; }

    public void setFollowId (String followId) { this.followId = followId; }

    public String getFollower() { return follower; }
    public String getFollowed() { return followed; }
}
