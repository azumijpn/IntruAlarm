package com.example.intrualarm;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class SignUpActivity extends AppCompatActivity {
    private Button signUpButton;
    private TextView title;
    private EditText userName;
    private EditText password;
    private int numUsers = 0;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signUpButton = findViewById(R.id.signUpButton);
        title = findViewById(R.id.title);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                DataSnapshot userTemp = dataSnapshot.child("Users");
                Iterator<DataSnapshot> usersIte = userTemp.getChildren().iterator();
                while(usersIte.hasNext()){
                    String tempStr = usersIte.next().getValue(String.class);
                    numUsers++;
                }
                numUsers++;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("error", "Failed to read value.", databaseError.toException());
            }
        });
        final DatabaseReference userRef = databaseReference.child("Users");
        final DatabaseReference passwordRef = databaseReference.child("Passwords");
        final DatabaseReference numAccessRef = databaseReference.child("NumAccess");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child("User" + numUsers).setValue(userName.getText().toString());
                passwordRef.child("User" + numUsers).setValue(password.getText().toString());
                numAccessRef.child("User" + numUsers).setValue(0);
                databaseReference.child("NewUser").setValue(true);
                builder.setTitle("Sign up successful!");
                builder.setMessage("Please login now!");
                AlertDialog dialog = builder.create();
                dialog.show();
                Intent i =  new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
