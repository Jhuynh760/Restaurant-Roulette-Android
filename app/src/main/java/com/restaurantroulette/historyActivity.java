package com.restaurantroulette;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Collections;

public class historyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        final TextView historyView = findViewById(R.id.historyView);

        DatabaseReference rollHistoryRef = FirebaseDatabase.getInstance().getReference("rollHistory");
        Query query = rollHistoryRef.orderByKey().limitToLast(10);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String timeRolled;
                String restName;
                String restAddress;
                String mapsURL;

                String historyText = "";
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    timeRolled = snapshot.child("timeRolled").getValue().toString();
                    restName = snapshot.child("restName").getValue().toString();
                    restAddress = snapshot.child("restAddress").getValue().toString();
                    mapsURL = snapshot.child("gMapsURL").getValue().toString();

                    historyText = String.format("Time Rolled: %s\n" +
                                                "\t\t\tRestaurant: %s\n" +
                                                "\t\t\tAddress: %s\n" +
                                                "\t\t\tMaps: %s\n\n\n", timeRolled, restName, restAddress, mapsURL) + historyText;
                }
                historyView.setText(historyText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void backButton(View view){
        finish();
    }

    public void rerollButton(View view){
        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
        finish();
    }

    public void clearHistoryButton(View view){
        DatabaseReference rollHistoryRef = FirebaseDatabase.getInstance().getReference("rollHistory");
        rollHistoryRef.setValue(null);
        final TextView historyView = findViewById(R.id.historyView);
        historyView.setText("");
    }
}
