package sample;

import java.util.Date;

/**
 * Created by Kamil on 2016-12-26.
 */
public class ServicingUserEntry {
    private String username;
    private byte[] challenge;
    private Date time;
    private char id;

    public ServicingUserEntry(String username, byte[] challenge, char id) {
        this.username = username;
        this.challenge = challenge;
        this.time = new Date();
        this.id = id;
    }

    public char getId() {
        return id;
    }

    public void setId(char id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

}