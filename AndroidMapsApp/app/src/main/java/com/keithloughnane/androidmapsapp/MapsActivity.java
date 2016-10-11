package com.keithloughnane.androidmapsapp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.os.StrictMode;




import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "|>>";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        Button btnGetForDates = (Button) findViewById(R.id.btnGetForDates);

        btnGetForDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: btnGetForDates");

                getLocations(1457604000);
            }
        });
    }

    public JSONArray getLocations(int unixTime)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String response = "";

        JSONArray jobj = new JSONArray();


        String str = "http://192.168.43.239:3000/activeLocations/date1457604000";
        try {
            URL url = new URL(str);

            URLConnection urlc = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line;
            //JsonParser parser = new JsonParser();

            //JSONArray jsn = new JSONArray(line);
            while((line = br.readLine())!=null)
            {

                response = response + line; //This can be more better

            }
            Log.d(TAG,response);

        }
        catch (Exception e)
        {
            Log.d(TAG, "onClick Net: " + e.toString());
            e.printStackTrace();
        }

        try {
            jobj  = new JSONArray(response);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.toString());
        }
        if(jobj != null) {

           // Log.d(TAG, "getLocations: Returning JSONArray " + jobj.get(0));

            try {

                    for (int i = 0; i < jobj.length(); i++) {
                        Log.d(TAG, "getLocations: Returning JSONArray " + jobj.getJSONObject(i).get("date_created"));

                        mMap.addMarker(new MarkerOptions().position(new LatLng(jobj.getJSONObject(i).getDouble("lat"),jobj.getJSONObject(i).getDouble("lng"))));

                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
        }
        return jobj;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        Latitude	59.436962
        Longitude	24.753574
                */

        // Add a marker in Sydney and move the camera
        LatLng tallinn = new LatLng(58.38062,26.72509);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(tallinn));
        mMap.setMinZoomPreference(10);
    }



}
