package common;

public class Session {
    private int userId;

    public boolean isLoggedIn() {
        return userId > 0;
    }

    public int getUserId() {
        return userId;
    }

    public void login(int userId) {
        this.userId = userId;
    }

    public void logout() {
        this.userId = 0;
    }
}
