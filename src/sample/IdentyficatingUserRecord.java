package sample;

/**
 * Created by Kamil on 2016-12-07.
 */
public class IdentyficatingUserRecord {
    private char id;
    private String challenge;
    private String userName;

    public char getId() {
        return id;
    }

    public void setId(char id) {
        this.id = id;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}