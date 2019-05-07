package com.restaurantroulette;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;

public class restaurantDetailsActivity extends AppCompatActivity {
    private Gson gson;

    private HashMap<String, String> restData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        this.gson = new Gson();
        this.restData = this.gson.fromJson(getIntent().getStringExtra("restData"), HashMap.class);

        TextView restName = findViewById(R.id.restName);
        restName.setText(this.restData.get("restName"));

        TextView restID = findViewById(R.id.restID);
        String restIDString = this.restData.get("restID");
        restID.setText(restIDString);

        TextView restRating = findViewById(R.id.restRating);
        String ratingString = this.restData.get("restRating") +  "/5 Stars";
        restRating.setText(ratingString);

        TextView restPhoneNumber = findViewById(R.id.restPhoneNumber);
        String phoneString = this.restData.get("restPhoneNumber");
        restPhoneNumber.setText(phoneString);

        TextView restAddress = findViewById(R.id.restAddress);
        String addressString = this.restData.get("restAddress");
        restAddress.setText(addressString);

        TextView restWebsite = findViewById(R.id.restWebsite);
        String websiteString = this.restData.get("website");
        restWebsite.setText(websiteString);

        TextView restGoogleMapsURL = findViewById(R.id.googleMapsURL);
        String mapsString = this.restData.get("gMapsURL");
        restGoogleMapsURL.setText(mapsString);

        TextView restHours = findViewById(R.id.restHours);
        String hoursString = this.restData.get("restHours");
        restHours.setText(hoursString);
    }

    public void reroll(View view){
        finish();
    }

    public void historyButton(View view){
        Intent intent = new Intent(getBaseContext(), historyActivity.class);
        startActivity(intent);
    }
}
