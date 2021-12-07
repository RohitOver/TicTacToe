package androidsamples.java.tictactoe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {

  private Context context;
  private ArrayList<Game> list;

  public OpenGamesAdapter(Context context,ArrayList<Game> list) {
    // FIXME if needed
    this.context = context;
    this.list = list;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

    // TODO bind the item at the given position to the holder
    Game game = list.get(position);
    holder.txtShowGameID.setText("Game Created By:");
    holder.txtGameID.setText(game.getGameID());

  }

  @Override
  public int getItemCount() {
    return list == null ? 0 : list.size(); // FIXME
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView txtShowGameID;
    public final TextView txtGameID;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      txtShowGameID = view.findViewById(R.id.txt_show_game_id);
      txtGameID = view.findViewById(R.id.txt_game_id);
      mView.setOnClickListener(this::launch2Game);
    }

    public void launch2Game(View view)
    {
      NavController navController = Navigation.findNavController(view);
      DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(context.getString(R.string.two_player),txtGameID.getText().toString(),"2");
      navController.navigate(action);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + txtGameID.getText() + "'";
    }
  }
}