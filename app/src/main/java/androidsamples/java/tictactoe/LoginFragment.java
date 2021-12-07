package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private TextView editEmail,editPassword;
    private String email;
    private String password;
    private DatabaseReference userAuthReference,userReference;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userAuthReference = database.getReference().child("usersAuth");
        userReference = database.getReference().child("users");

        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    // TODO implement sign in logic
                    Log.d(TAG, "onCreateView: Button clicked");
                    editEmail = view.findViewById(R.id.edit_email);
                    editPassword = view.findViewById(R.id.edit_password);
                    email = editEmail.getText().toString();
                    password = editPassword.getText().toString();
                    signInUser(email,password);
                });

        return view;
    }

    /**
     * Signs in the User
     * @param email
     * @param password
     */
    public void signInUser(String email, String password)
    {
        userAuthReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(email).exists())
                {
                    Users user = dataSnapshot.child(email).getValue(Users.class);
                    if(user != null && user.getPassword().equals(password))
                    {
                        LoginFragmentDirections.ActionLoginSuccessful action = LoginFragmentDirections.actionLoginSuccessful(user.getEmail());
                        navController.navigate(action);
                        Log.d(TAG, "onDataChange: Valid Password");
                    }
                    else
                        Toast.makeText(getContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Users newUser = new Users(email,password);
                    userAuthReference.child(email).setValue(newUser);
                    userReference.child(email).setValue(newUser);

                    LoginFragmentDirections.ActionLoginSuccessful action = LoginFragmentDirections.actionLoginSuccessful(newUser.getEmail());
                    navController.navigate(action);
                    Log.d(TAG, "onDataChange: New User");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // No options menu in login fragment.
}