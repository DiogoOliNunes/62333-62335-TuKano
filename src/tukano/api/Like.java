package tukano.api;

import jakarta.persistence.*;

@Entity
public class Like {

    @Id
    private String likeId;
    private String shortLiked;
    private String user;

    public Like() {}

    public Like (String likeId, String shortLiked, String user) {
        this.likeId = likeId;
        this.shortLiked = shortLiked;
        this.user = user;
    }

    public String getLikeId() {
        return likeId;
    }

    public String getShortLiked() {
        return shortLiked;
    }

    public void setShortLiked(String shortLiked) {
        this.shortLiked = shortLiked;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
