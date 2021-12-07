package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private String gameType;
  private final Button[] mButtons = new Button[GRID_SIZE];

  private Game game;
  private String gameID;
  private NavController mNavController;

  private int playerNum;
  private boolean requiredTurn;

  private boolean clickedForfeit;

  private DatabaseReference gameReference;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    gameReference = database.getReference().child("games");

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    Log.d(TAG, "New game type = " + args.getGameType());
    gameType = args.getGameType();
    gameID = args.getGameID();
    playerNum = Integer.parseInt(args.getPlayerNum());
    if(playerNum == 1)
      requiredTurn = true;
    else if(playerNum == 2)
      requiredTurn = false;

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        // TODO show dialog only when the game is still in progress
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.confirm)
            .setMessage(R.string.forfeit_game_dialog_message)
            .setPositiveButton(R.string.yes, (d, which) -> {
              // TODO update loss count
              if(gameType.equals(getString(R.string.one_player))) {
                mNavController.getPreviousBackStackEntry().getSavedStateHandle().set("game", "loss");
                mNavController.popBackStack();
              }
              else if(gameType.equals(getString(R.string.two_player))) {
                gameReference.child(gameID).child("gameOver").setValue(true);
                clickedForfeit = true;
              }
            })
            .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
            .create();
        dialog.show();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    mNavController = Navigation.findNavController(view);

    clickedForfeit = false;

    if(gameType.equals(getString(R.string.one_player)))
    {
      game = new Game(gameID);
    }
    else if(gameType.equals(getString(R.string.two_player)))
    {
      gameReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          Log.d(TAG, "onDataChange -snapshot: "+dataSnapshot);
          game = dataSnapshot.child(gameID).getValue(Game.class);
          updateGridUI();

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Log.w(TAG, "Failed to read value.", error.toException());
        }
      });

      gameReference.child(gameID).child("gameOver").addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          Log.d(TAG, "onDataChange: Single Value Event"+snapshot);
          boolean over = snapshot.getValue(boolean.class);
          if(over)
            showDialogFor2Player();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });
    }

    if(gameType.equals(getString(R.string.one_player))) {
      for (int i = 0; i < mButtons.length; i++) {
        int finalI = i;
        mButtons[finalI].setOnClickListener(v -> {
          Log.d(TAG, "Button " + finalI + " clicked");
          // TODO implement listeners

          boolean draw = false;

          if(getGrid(finalI).equals("-"))
          {
            mButtons[finalI].setText("X");
            setGrid(finalI,"X");

            if(findWinner() == 0)
            {
              winDialog();
            }
            else if(findWinner() == 2)
            {
              drawDialog();
              draw = true;
            }
            computerPlay();
            if(findWinner() == 1)
            {
              lossDialog();
            }
            else if(!draw && findWinner() == 2)
            {
              drawDialog();
            }
          }

        });
      }
    }
    else if(gameType.equals(getString(R.string.two_player)))
    {
      String myToken = "";
      if(playerNum == 1)
        myToken = "X";
      else if(playerNum == 2)
        myToken = "O";
      for (int i = 0; i < mButtons.length; i++) {
        int finalI = i;
        String finalMyToken = myToken;
        mButtons[i].setOnClickListener(v -> {
          Log.d(TAG, "Button " + finalI + " clicked");
          // TODO implement listeners
          if(game.getTurn() == requiredTurn && getGrid(finalI).equals("-"))
          {
            setGrid(finalI, finalMyToken);
            mButtons[finalI].setText(finalMyToken);
            updateGridInDB();
            if(findWinner() != -1)
              updateGameOverInDB();
            flipTurnInDB();
          }

        });
      }
    }

  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  /**
   * Makes random moves on grid
   */
  public void computerPlay()
  {

    for (int i = 0; i < GRID_SIZE; i++) {
      Log.d(TAG, "computerPlay: "+getGrid(i));
      if (getGrid(i).equals("-")) {
        setGrid(i,"O");
        mButtons[i].setText("O");
        break;
      }
    }

  }

  /**
   * Dialog specific to 2 player game
   */
  public void showDialogFor2Player()
  {
    if(findWinner() == 0)
    {
      winDialog();
    }
    else if(findWinner() == 1)
    {
      lossDialog();
    }
    else if(findWinner() == 2)
    {
      drawDialog();
    }
    else
    {
      if(clickedForfeit)
        lossDialog();
      else
        winDialog();
    }
  }

  /**
   * Find Winner of game
   * @return index corresponding to winner
   */
  public int findWinner()
  {
    String[] vals = new String[2];
    if(playerNum == 1)
    {
      vals[0] = "X";
      vals[1] = "O";
    }
    else if(playerNum == 2)
    {
      vals[0] = "O";
      vals[1] = "X";
    }
    for (int i = 0; i < 2; i++) {
      String marker = vals[i];
      if(getGrid(0).equals(marker) && getGrid(1).equals(marker) && getGrid(2).equals(marker))
        return i;
      if(getGrid(3).equals(marker) && getGrid(4).equals(marker) && getGrid(5).equals(marker))
        return i;
      if(getGrid(6).equals(marker) && getGrid(7).equals(marker) && getGrid(8).equals(marker))
        return i;

      if(getGrid(0).equals(marker) && getGrid(3).equals(marker) && getGrid(6).equals(marker))
        return i;
      if(getGrid(1).equals(marker) && getGrid(4).equals(marker) && getGrid(7).equals(marker))
        return i;
      if(getGrid(2).equals(marker) && getGrid(5).equals(marker) && getGrid(8).equals(marker))
        return i;

      if(getGrid(0).equals(marker) && getGrid(4).equals(marker) && getGrid(8).equals(marker))
        return i;
      if(getGrid(2).equals(marker) && getGrid(4).equals(marker) && getGrid(6).equals(marker))
        return i;
    }

    for (int i = 0; i < GRID_SIZE; i++) {
      if(getGrid(i).equals("-"))
        return -1;
    }
    return 2;
  }

  /**
   * Setter for grid
   * @param pos
   * @param inp
   */
  public void setGrid(int pos,String inp)
  {
    switch (pos)
    {
      case 0:
      {
        game.setGrid0(inp);
        break;
      }
      case 1:
      {
        game.setGrid1(inp);
        break;
      }
      case 2:
      {
        game.setGrid2(inp);
        break;
      }
      case 3:
      {
        game.setGrid3(inp);
        break;
      }
      case 4:
      {
        game.setGrid4(inp);
        break;
      }
      case 5:
      {
        game.setGrid5(inp);
        break;
      }
      case 6:
      {
        game.setGrid6(inp);
        break;
      }
      case 7:
      {
        game.setGrid7(inp);
        break;
      }
      case 8:
      {
        game.setGrid8(inp);
        break;
      }
    }
  }

  /**
   * Getter for grid
   * @param pos
   * @return
   */
  public String getGrid(int pos)
  {
    switch (pos)
    {
      case 0:
      {
        return game.getGrid0();
      }
      case 1:
      {
        return game.getGrid1();
      }
      case 2:
      {
        return game.getGrid2();
      }
      case 3:
      {
        return game.getGrid3();
      }
      case 4:
      {
        return game.getGrid4();
      }
      case 5:
      {
        return game.getGrid5();
      }
      case 6:
      {
        return game.getGrid6();
      }
      case 7:
      {
        return game.getGrid7();
      }
      case 8:
      {
        return game.getGrid8();
      }
    }
    return "";
  }

  /**
   * Updates UI of grid
   */
  public void updateGridUI()
  {
    for (int i = 0; i < GRID_SIZE; i++) {
      if(!getGrid(i).equals("-"))
        mButtons[i].setText(getGrid(i));
    }
  }

  /**
   * Updates grid values in Database
   */
  public void updateGridInDB()
  {
    Log.d(TAG, "updateGridInDB: ");
    gameReference.child(gameID).child("grid0").setValue(getGrid(0));
    gameReference.child(gameID).child("grid1").setValue(getGrid(1));
    gameReference.child(gameID).child("grid2").setValue(getGrid(2));
    gameReference.child(gameID).child("grid3").setValue(getGrid(3));
    gameReference.child(gameID).child("grid4").setValue(getGrid(4));
    gameReference.child(gameID).child("grid5").setValue(getGrid(5));
    gameReference.child(gameID).child("grid6").setValue(getGrid(6));
    gameReference.child(gameID).child("grid7").setValue(getGrid(7));
    gameReference.child(gameID).child("grid8").setValue(getGrid(8));

  }

  /**
   * Changes value of gameOver in Database
   */
  public void updateGameOverInDB()
  {
    Log.d(TAG, "updateGameOverInDB: ");
    gameReference.child(gameID).child("gameOver").setValue(true);
  }

  /**
   * Changes value of turn in Database
   */
  public void flipTurnInDB()
  {
    Log.d(TAG, "flipTurnInDB: ");
    game.flipTurn();
    gameReference.child(gameID).child("turn").setValue(game.getTurn());
  }

  /**
   * Displays WIN dialog box
   */
  public void winDialog()
  {
    Log.d(TAG, "winDialog: ");
    AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.congrats)
            .setMessage(R.string.congrats_dialog_message)
            .setNegativeButton(R.string.ok, (d, which) -> {
              mNavController.getPreviousBackStackEntry().getSavedStateHandle().set("game", "win");
              mNavController.popBackStack();
            })
            .create();
    dialog.show();
  }

  /**
   * Displays LOSS dialog box
   */
  public void lossDialog()
  {
    Log.d(TAG, "lossDialog: ");
    AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.sorry)
            .setMessage(R.string.sorry_dialog_message)
            .setNegativeButton(R.string.ok, (d, which) -> {
              mNavController.getPreviousBackStackEntry().getSavedStateHandle().set("game", "loss");
              mNavController.popBackStack();
            })
            .create();
    dialog.show();
  }

  /**
   * Displays DRAW dialog box
   */
  public void drawDialog()
  {
    Log.d(TAG, "drawDialog: ");
    AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.draw)
            .setMessage(R.string.draw_dialog_message)
            .setNegativeButton(R.string.ok, (d, which) -> mNavController.popBackStack())
            .create();
    dialog.show();
  }
}