package mark.foodhygieneratings;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String SelectedID;
    private ArrayList<String[]> allCoordinates = new ArrayList<>();

    /**
     * Method called when the MapsActivity first starts
     * It gets the data from the Intent Extra, extracts the relevant details and
     * puts them into an array to used in the onMapReady method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the locations that were passed with the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                // The locations were passed as responseBody String so need to convert them to JSON again
                JSONArray data = new JSONArray(extras.getString("JSON"));
                // The id passed in will be used to identify the selected business when the map is displayed
                String id = extras.getString("id");

                for (int i = 0; i < data.length(); i++) {
                    String[] latlon     = new String[6];
                    String latitude     = data.getJSONObject(i).getString("Latitude");
                    String longitude    = data.getJSONObject(i).getString("Longitude");
                    String businessName = data.getJSONObject(i).getString("BusinessName");
                    String rating       = data.getJSONObject(i).getString("RatingValue");
                    String ratingDate   = data.getJSONObject(i).getString("RatingDate");
                    String idFromArray  = data.getJSONObject(i).getString("id");

                    // The data needed for the markers is stored in an arrayList of arrays
                    latlon[0] = latitude;
                    latlon[1] = longitude;
                    latlon[2] = businessName;
                    latlon[3] = rating;
                    latlon[4] = ratingDate;
                    latlon[5] = idFromArray;
                    allCoordinates.add(latlon);

                    // Where the id being looped through from the array is compared with
                    // the one passed through the Intent
                    if (idFromArray.equals(id)) {
                        SelectedID = id;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method which draws the marker pins on the map.
     * It uses custom marker pins which identify the rating that the business received.
     * It also automatically highlights and centres the location that the user selected.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;
        LatLng selectedLocation = null;

        // Used to store the co-ordinates to see if any markers will be on top of another
        float[] a = new float[20];

        for (int i = 0; i < allCoordinates.size(); i++) {
            // Get the values back out of the array and convert the coordinates to float
            String latitude     = allCoordinates.get(i)[0];
            String longitude    = allCoordinates.get(i)[1];
            float lat = Float.parseFloat(latitude);
            float lon = Float.parseFloat(longitude);

            // Some of the businesses in the database have the same co-ordinates.
            // Here the co-ordinates are checked to see if they have already been used and
            // if so, a random (but close by) alternative is computed
            for (float j : a) {
                if (lat == j || lon == j) {
                    lat = lat + (float)(Math.random() / 5000);
                    lon = lon + (float)(Math.random() / 5000);
                }
            }
            a[i] = lat; a[i+1] = lon;

            // Get the rest of the data
            String BusinessName = allCoordinates.get(i)[2];
            String rating       = allCoordinates.get(i)[3];
            String ratingDate   = allCoordinates.get(i)[4];
            String id           = allCoordinates.get(i)[5];

            // Identify from the rating value which marker pin should be used
            int pin = 0;
            switch (rating) {
                case "-1": pin  = R.drawable.pinexempt;
                    break;
                case "0": pin   = R.drawable.pin0;
                    break;
                case "1": pin   = R.drawable.pin1;
                    break;
                case "2": pin   = R.drawable.pin2;
                    break;
                case "3": pin   = R.drawable.pin3;
                    break;
                case "4": pin   = R.drawable.pin4;
                    break;
                case "5": pin   = R.drawable.pin5;
                    break;
            }

            // Creates a new LatLng object which tells the map where the marker should go
            LatLng location = new LatLng(lat, lon);

            // For the marker, select which items need to be displayed
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(BusinessName)
                    .snippet("Rating Date: " + ratingDate)
                    .icon((BitmapDescriptorFactory.fromResource(pin))));

            // The selected location coordinates will be used to centre the map
            // and show the relevant info window
            float SelLat; float SelLon;
            if (SelectedID.equals(id)) {
                SelLat = lat;
                SelLon = lon;
                marker.showInfoWindow();
                selectedLocation = new LatLng(SelLat, SelLon);
            }
        }

        // Centers the map on the selected location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLocation));

        // Options to set the zoom level for the map and show zoom controls
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14.5f));
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
    }

}
