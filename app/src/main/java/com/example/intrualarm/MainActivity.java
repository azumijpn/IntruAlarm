package com.example.intrualarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private TextView title;
    private TextView errorMessage;
    private EditText userName;
    private EditText password;
    private Button logIn;
    private Button signUp;
    private List<String> users = new ArrayList<>();
    private List<String> passwords = new ArrayList<>();
    private int userIndex = 0;
    private boolean login(String username, String password, List<String> users, List<String> passwords){
        for(String user : users){
            if(user.equals(username)){
                if(passwords.get(userIndex).equals(password)){
                    return true;
                }
            }
            userIndex++;
        }
        //failed login
       return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        title = findViewById(R.id.title);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        logIn = findViewById(R.id.logIn);
        signUp = findViewById(R.id.signUp);
        errorMessage = findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.INVISIBLE);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference temp = databaseReference.child("CurrUser");
                boolean result = login(userName.getText().toString(), password.getText().toString(), users,passwords);
                if(result){
                    errorMessage.setVisibility(View.INVISIBLE);
                    temp.setValue(userName.getText().toString());
                    Intent i =  new Intent(MainActivity.this, LoggedinActivity.class);
                    i.putExtra("Username",userName.getText().toString());
                    i.putExtra("userIndex", userIndex+1);
                    startActivity(i);
                }
                else{
                    errorMessage.setVisibility(View.VISIBLE);
                }
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                DataSnapshot userTemp = dataSnapshot.child("Users");
                DataSnapshot passwordTemp = dataSnapshot.child("Passwords");
                Iterator<DataSnapshot> pwIte = passwordTemp.getChildren().iterator();
                Iterator<DataSnapshot> usersIte = userTemp.getChildren().iterator();
                while(usersIte.hasNext()){
                    String tempStr = usersIte.next().getValue(String.class);
                    users.add(tempStr);
                }
                while(pwIte.hasNext()){
                    String pwTempStr = pwIte.next().getValue(String.class);
                    passwords.add(pwTempStr);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("error", "Failed to read value.", databaseError.toException());
            }
        });
    }


}
