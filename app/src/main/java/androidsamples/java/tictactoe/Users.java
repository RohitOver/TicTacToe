package androidsamples.java.tictactoe;

public class Users {
    private String email;
    private String password;
    private int wins;
    private int losses;

    public Users() {}

    public Users(String email, String password) {
        this.email = email;
        this.password = password;
        losses = 0;
        wins = 0;
    }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public int getWins() { return wins;}

    public int getLosses() { return losses;}

    public void increaseCntWins()
    {
        wins++;
    }

    public void increaseCntLosses()
    {
        losses++;
    }
}
