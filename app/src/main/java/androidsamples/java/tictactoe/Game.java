package androidsamples.java.tictactoe;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static final int GRID_SIZE = 9;
    private String gameID;
    private String grid0,grid1,grid2,grid3,grid4,grid5,grid6,grid7,grid8;
    private boolean turn;
    private String status;
    private boolean gameOver;

    public Game(){
    }

    public Game(String gameID) {
        this.gameID = gameID;
        grid0 = "-";
        grid1 = "-";
        grid2 = "-";
        grid3 = "-";
        grid4 = "-";
        grid5 = "-";
        grid6 = "-";
        grid7 = "-";
        grid8 = "-";
        status = "UNDECIDED";
        gameOver = false;
        turn = true;


    }

    public String getGameID() { return gameID;}

    public String getGrid0() {
        return grid0;
    }

    public String getGrid1() {
        return grid1;
    }

    public String getGrid2() {
        return grid2;
    }

    public String getGrid3() {
        return grid3;
    }

    public String getGrid4() {
        return grid4;
    }

    public String getGrid5() {
        return grid5;
    }

    public String getGrid6() {
        return grid6;
    }

    public String getGrid7() {
        return grid7;
    }

    public String getGrid8() {
        return grid8;
    }

    public String getStatus() { return status;}

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean getTurn() {
        return turn;
    }


    public void setGrid0(String val) {
        grid0 = val;
    }

    public void setGrid1(String val) {
        grid1 = val;
    }

    public void setGrid2(String val) {
        grid2 = val;
    }

    public void setGrid3(String val) {
        grid3 = val;
    }

    public void setGrid4(String val) {
        grid4 = val;
    }

    public void setGrid5(String val) {
        grid5 = val;
    }

    public void setGrid6(String val) {
        grid6 = val;
    }

    public void setGrid7(String val) {
        grid7 = val;
    }

    public void setGrid8(String val) {
        grid8 = val;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public void setStatus(String val) { status = val;}

    public void setGameOver(boolean val) { gameOver = val;}

    public void flipTurn()
    {
        turn = !(turn);
    }
}

