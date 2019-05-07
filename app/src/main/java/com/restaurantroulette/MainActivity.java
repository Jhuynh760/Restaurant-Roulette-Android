package com.restaurantroulette;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference rollHistoryRef;
    private RestaurantRoulette rrInstance;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.rollHistoryRef = FirebaseDatabase.getInstance().getReference("rollHistory");
        this.rrInstance = new RestaurantRoulette(this.rollHistoryRef);
        this.gson = new Gson();
        this.rrInstance.clearRollHistoryRef();
    }

    public void getRandomRestaurant(View view){
        Intent intentDetailsActivity = new Intent(getBaseContext(), restaurantDetailsActivity.class);

        TextView errorText = findViewById(R.id.errorText);
        String errorMessage = "";
        errorText.setText(errorMessage);

        EditText editAddress = findViewById(R.id.addressText);
        String address = editAddress.getText().toString();
        Log.d("randomButton", "ADDRESS TEXT: " + address);
        EditText editRadius = findViewById(R.id.radiusText);
        String radiusStr = editRadius.getText().toString();
        if (!radiusStr.equals("")){
            int radius = Integer.parseInt(radiusStr);
            Log.d("randomButton", "Radius TEXT: " + radiusStr);
            rrInstance.setTravelRadius(radius);
        }
        if (address.equals("")){
            errorMessage += "ERROR:No address provided";
            Log.d("randomButton", "Address Error: no address provided.");
            errorText.setText(errorMessage);
        }
        else{
            Log.d("randomButton", "Address: " + address);
            this.rrInstance.setAddress(address);
            RestaurantRouletteThread rrThread = new RestaurantRouletteThread(this.rrInstance);
            rrThread.start();
            try{
                rrThread.join();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            HashMap<String, String> restData = this.rrInstance.getRandRestaurantInfo();
            this.passIntentDetail(intentDetailsActivity, "restData", this.gson.toJson(restData));

            startActivity(intentDetailsActivity);
        }

    }

    public void historyButton(View view){
        Intent historyIntent = new Intent(getBaseContext(), historyActivity.class);
        startActivity(historyIntent);
    }

    private void passIntentDetail(Intent intent, String name, String data){
        intent.putExtra(name, data);
        Log.d("randomButton", "Passed Intent - Name: " + name + " " + data);
    }

}
