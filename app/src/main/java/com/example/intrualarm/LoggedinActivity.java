package com.example.intrualarm;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoggedinActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private TextView title, timeView1, timeView2, timeView3, timeView4, timeView5, timeView6, timeView7, timeView8, timeView9, timeView10;
    private Switch AuthorizationSwitch;
    private List<String> times = new ArrayList<>();
    private String currUser;
    private TextView[] chart = new TextView[10];

    private void display(List<String> times, boolean auth){
        if(auth){
            AuthorizationSwitch.setChecked(true);
        }else{
            AuthorizationSwitch.setChecked(false);
        }
        if(times.size() > 10){
            for(int i = 1; i <= 10; i++){
                chart[i-1].setText(times.get(times.size()-i));
            }
        }else{
            for(int i = 1; i <= times.size(); i++){
                chart[i-1].setText(times.get(times.size()-i));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        int userIndex = bundle.getInt("userIndex");
        title = findViewById(R.id.title);
        timeView1 = findViewById(R.id.timeView1);
        timeView2 = findViewById(R.id.timeView2);
        timeView3 = findViewById(R.id.timeView3);
        timeView4 = findViewById(R.id.timeView4);
        timeView5 = findViewById(R.id.timeView5);
        timeView6 = findViewById(R.id.timeView6);
        timeView7 = findViewById(R.id.timeView7);
        timeView8 = findViewById(R.id.timeView8);
        timeView9 = findViewById(R.id.timeView9);
        timeView10 = findViewById(R.id.timeView10);
        chart[0] = timeView1;
        chart[1] = timeView2;
        chart[2] = timeView3;
        chart[3] = timeView4;
        chart[4] = timeView5;
        chart[5] = timeView6;
        chart[6] = timeView7;
        chart[7] = timeView8;
        chart[8] = timeView9;
        chart[9] = timeView10;
        AuthorizationSwitch = findViewById(R.id.AuthorizationSwitch);
        String tempUsername = bundle.getString("Username");
        String tempStr = "Welcome " + tempUsername;
        title.setText(tempStr);
        currUser = "User" + userIndex;
        final DatabaseReference authorizationRef = databaseReference.child("Authorization");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AuthorizationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    authorizationRef.setValue(true);
                    builder.setTitle("Alarm is deactivated");
                    builder.setMessage("you can open door now");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    authorizationRef.setValue(false);
                    builder.setTitle("Alarm activated");
                    builder.setMessage("Alarm will sound if door is opened");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
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
                DataSnapshot timeTemp = dataSnapshot.child("TimeAccess");
                DataSnapshot timeTemp2 = timeTemp.child(currUser);
                DataSnapshot authTemp = dataSnapshot.child("Authorization");
                boolean auth = authTemp.getValue(Boolean.class);
                Iterator<DataSnapshot> timeIte = timeTemp2.getChildren().iterator();
                while(timeIte.hasNext()){
                    String timeStr = timeIte.next().getValue(String.class);
                    times.add(timeStr);
                }
                display(times, auth);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("error", "Failed to read value.", databaseError.toException());
            }
        });

    }
}