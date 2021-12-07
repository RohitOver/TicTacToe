package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private TextView editWins,editLosses;
  private OpenGamesAdapter adapter;
  private ArrayList<Game> list;
  private DatabaseReference userReference,gamesReference;
  private NavController mNavController;
  private String email;
  private int wins,losses;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {

    View view =  inflater.inflate(R.layout.fragment_dashboard, container, false);
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    gamesReference = database.getReference().child("games");

    RecyclerView recyclerView = view.findViewById(R.id.list);

    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    list = new ArrayList<>();
    adapter = new OpenGamesAdapter(getActivity(),list);
    recyclerView.setAdapter(adapter);

    gamesReference.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Game newGame = snapshot.getValue(Game.class);
        if(!newGame.isGameOver()) {
          Log.d(TAG, "onChildAdded: ");
          list.add(newGame);
        }
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Game newGame = snapshot.getValue(Game.class);
        if(!newGame.isGameOver() && !checkGameIDInList(newGame.getGameID())) {
          Log.d(TAG, "onChildChanged: ");
          list.add(newGame);
        }
        adapter.notifyDataSetChanged();

      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {

      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

    return view;
  }



  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    userReference = database.getReference().child("users");
    gamesReference = database.getReference().child("games");


    // TODO if a user is not logged in, go to LoginFragment

    editWins = view.findViewById(R.id.edit_wins);
    editLosses = view.findViewById(R.id.edit_losses);

    email = DashboardFragmentArgs.fromBundle(getArguments()).getEmail();

    Game newGame = new Game(email);

    userReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d(TAG, "onDataChange -snapshot: "+dataSnapshot);
        Users user = dataSnapshot.child(email).getValue(Users.class);
        email = user.getEmail();
        wins = user.getWins();
        losses = user.getLosses();
        editWins.setText(Integer.toString(wins));
        editLosses.setText(Integer.toString(losses));

      }

      @Override
      public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
      }
    });

    MutableLiveData<String> liveDataEndTime = mNavController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("game");
    liveDataEndTime.observe(getViewLifecycleOwner(), s -> {
      if(s.equals("win"))
      {
        wins++;
        userReference.child(email).child("wins").setValue(wins);
        editWins.setText(Integer.toString(wins));
      }
      else if(s.equals("loss"))
      {
        losses++;
        userReference.child(email).child("losses").setValue(losses);
        editLosses.setText(Integer.toString(losses));
      }
    });


    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
          Log.d(TAG, "makeNew2PlayerGame: Created "+newGame.getGameID());
          gamesReference.child(email).setValue(newGame);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(gameType,email,"1");
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "Logout button clicked");
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(action);
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Checks if Game already created
   * @param gameID
   * @return
   */
  public boolean checkGameIDInList(String gameID)
  {
    for(Game game : list)
    {
      if(game.getGameID().equals(gameID))
        return true;
    }
    return false;
  }

}