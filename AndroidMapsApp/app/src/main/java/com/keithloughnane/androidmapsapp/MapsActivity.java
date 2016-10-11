package com.keithloughnane.androidmapsapp;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
//import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.os.StrictMode;
import android.content.DialogInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private static final String TAG = "|>>";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btnGetForDates = (Button) findViewById(R.id.btnGetForDates);
        //Button Click
        btnGetForDates.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick: btnGetForDates");
                EditText strDate = (EditText) findViewById(R.id.editDate);
                EditText strTime = (EditText) findViewById(R.id.editTime);
                getLocations(getDateTimeFromString(strDate.getText().toString(), strTime.getText().toString()));
            }
        });
    }

    public long getDateTimeFromString(String date, String time)
    {
        //Takes messy human input, checks it and returns unix time.
        Date ddate = new Date();
        int returnTime = -1;
        try
        {
            ddate = new Date(date + " " + time);
            Log.d(TAG, "getDateTimeFromString: date=" + date + ", ddate " + date.toString());
        } catch (Exception e)
        {
            Log.d(TAG, "getDateTimeFromString: " + e.toString());
            String dateErr = getResources().getString(R.string.dateErr);
            alert(dateErr);
        }
        Log.d(TAG, "getDateTimeFromString: ddate=" + ddate.getTime());
        //returnTime = Math.round(ddate.getTime());
        return ddate.getTime();
    }

    public JSONArray getLocations(long unixTime)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /* Ok, Yeah. The above isn't great. I should write this in a way that won't look up instead
            of turning off wise warnings. I just wanted to get it working first.
         */
        String response = "";
        JSONArray jobj = new JSONArray();
        String serverAdd = getResources().getString(R.string.serveraddress);
        String str = serverAdd + "/activeLocations/date" + String.valueOf(unixTime);
        Log.d(TAG, "getLocations URL: " + str);
        try
        {
            URL url = new URL(str);
            URLConnection urlc = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
            {
                response = response + line; //This can be more better
            }
            Log.d(TAG, response);
        } catch (Exception e)
        {
            Log.d(TAG, "onClick Net: " + e.toString());
            e.printStackTrace();
        }

        try
        {
            jobj = new JSONArray(response);
        } catch (Exception e)
        {
            Log.d(TAG, e.toString());
        }
        if (jobj != null)
        {
            try
            {

                for (int i = 0; i < jobj.length(); i++)
                {
                    Log.d(TAG, "getLocations: Returning JSONArray " + jobj.getJSONObject(i).get("date_created"));

                    mMap.addMarker(new MarkerOptions().position(new LatLng(jobj.getJSONObject(i).getDouble("lat"), jobj.getJSONObject(i).getDouble("lng"))));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return jobj;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        /*
        Latitude	59.436962
        Longitude	24.753574
                */

        LatLng gpsStartPoint = new LatLng(58.38062, 26.72509);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsStartPoint));
        mMap.setMinZoomPreference(10);

        final ArrayList<Marker> selectedMarkers = new ArrayList<>();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {

            @Override
            public boolean onMarkerClick(Marker marker)
            {

                Log.d(TAG, "onMarkerClick: Clicked");
                //double aa = marker.getPosition().latitude;
                //double bb = marker.getPosition().longitude;
                if (selectedMarkers.contains(marker))
                {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    selectedMarkers.remove(marker);
                } else
                {
                    selectedMarkers.add(marker);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
                if (selectedMarkers.size() == 2)//if two selected show distance
                {
                    Log.d(TAG, "onMarkerClick: 2 Selected, getting distance");
                    String dist = getDistance(selectedMarkers.get(0).getPosition(), selectedMarkers.get(1).getPosition());
                    alert("  " + dist);
                }
                return false;
            }
        });
    }

    public String getDistance(LatLng P1, LatLng P2)
    {

        JSONObject requestJSON = new JSONObject();
        try
        {
            JSONObject point1 = new JSONObject();
            point1.put("lat", P1.latitude);
            point1.put("lng", P1.longitude);
            //Log.d(TAG, "getDistance: " + P1.latitude );
            JSONObject point2 = new JSONObject();
            point2.put("lat", P2.latitude);
            point2.put("lng", P2.longitude);
            requestJSON.put("point1", point1);
            requestJSON.put("point2", point2);
        } catch (Exception e)
        {
            Log.d(TAG, "getDistance E : " + e);
        }

        String jsonString = requestJSON.toString();
        Log.d(TAG, "getDistance F : " + jsonString);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String response = "";
        String returnValue = "";

        JSONObject jobj = new JSONObject();
        String serverAdd = getResources().getString(R.string.serveraddress);
        String str = serverAdd + "/getDistance/coords" + jsonString;
        try
        {
            URL url = new URL(str);

            URLConnection urlc = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
            {
                response = response + line; //This can be more better
            }
            Log.d(TAG, response);
        } catch (Exception e)
        {
            Log.d(TAG, "onClick Net: " + e.toString());
            e.printStackTrace();
        }
        try
        {
            jobj = new JSONObject(response);
        } catch (Exception e)
        {
            Log.d(TAG, e.toString());
        }
        if (jobj != null)
        {

            try
            {
                Log.d(TAG, "getDistance: Returning" + jobj.getString("distance"));
                returnValue = jobj.getString("distance");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    public void alert(String alertMsg)
    {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(alertMsg);

        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //dismiss the dialog
            }
        });
    }
}
