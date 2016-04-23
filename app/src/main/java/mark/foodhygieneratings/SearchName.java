package mark.foodhygieneratings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchName extends AppCompatActivity {

    private String id;
    private String BusinessName;
    private String AddressLine1;
    private String AddressLine2;
    private String AddressLine3;
    private String PostCode;
    private String RatingValue;
    private String responseBody;
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_name);

        // Set the three navigation buttons
        ImageButton btn1 = (ImageButton) findViewById(R.id.location);
        if (btn1 != null) {
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchName.this, SearchLocation.class));
                }
            });
        }
        ImageButton btn2 = (ImageButton) findViewById(R.id.name);
        if (btn2 != null) {
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchName.this, SearchName.class));
                }
            });
        }
        ImageButton btn3 = (ImageButton) findViewById(R.id.postcode);
        if (btn3 != null) {
            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SearchName.this, SearchPostcode.class));
                }
            });
        }
    }


    /**
     * Method called when the submit button is clicked
     * Gets the text from the user input text box and invokes the method to show the locations
     *
     * @param view
     */
    public void submit_onClick(View view) {

        String input;
        // The TextBox where the user enters in the name to search for
        EditText userInput = (EditText) findViewById(R.id.userInput);
        if (userInput != null) {

            // Clear any error message if it exists
            TextView error = (TextView) findViewById(R.id.error);
            if (error != null) { error.setText(""); }

            // Get the user inputted text
            input = userInput.getText().toString();

            // Pass the user input to the method to get the locations
            get_locations(input);
        }
    }


    /**
     * Method to query the server using the postcode provided by
     * the user and put the response on the screen
     */
    private void get_locations(String input) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpURLConnection urlConnection = null;

            // Using URLEncoder to ensure that spaces are properly formatted and the entire query is passed in
            String query = URLEncoder.encode(input);
            String address = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_name&name=" + query;

            try {
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader ins = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader in = new BufferedReader(ins);
                // read the input stream as for normal I/O
                String line;
                responseBody = "";
                while ((line = in.readLine()) != null) {
                    responseBody = responseBody + line;
                }
                ins.close();
                in.close();
                // we should now have one big string with the entire fetched resource

                // If there are no results, inform the user
                if (responseBody.equals("[]")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText(R.string.error);
                        error.setTextAppearance(this, R.style.Error);
                    }
                }
                // If the server returns anything other than an array, it's probably an error
                if (!responseBody.startsWith("[")) {
                    TextView error = (TextView) findViewById(R.id.error);
                    if (error != null) {
                        error.setText(responseBody);
                        error.setTextAppearance(this, R.style.Error);
                    }
                }

                // Pass the responseBody to the parseJSON method for analysis
                parseJSON(responseBody);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }
        } else {
            // display error
        }
    }


    /**
     * Method takes the responseBody that comes back from the server and
     * loops through the array splitting up the key value pairs.
     * For each loop, it calls the createTable method to insert the data into the table
     *
     * @param responseBody
     */
    private void parseJSON(String responseBody) {
        try {

            // The response from the server is in JSON format
            JSONArray data = new JSONArray(responseBody);

            // Table where the results will be displayed, first remove the previous results
            table = (TableLayout) findViewById(R.id.table_location);
            if (table != null) {
                table.removeAllViews();
            }

            // Loops through all the results in the JSON array, extracts the relevant fields
            // and then invokes the table method where the rows are added
            for (int i = 0; i < data.length(); i++) {
                id = data.getJSONObject(i).getString("id");
                BusinessName = data.getJSONObject(i).getString("BusinessName");
                if (BusinessName.length() > 25) {
                    BusinessName = BusinessName.substring(0, 25) + "...";
                }
                AddressLine1 = data.getJSONObject(i).getString("AddressLine1");
                AddressLine2 = data.getJSONObject(i).getString("AddressLine2");
                AddressLine3 = data.getJSONObject(i).getString("AddressLine3");
                // If AddressLine1 from the results is empty, the other two address fields are moved up
                if (AddressLine1 == null || AddressLine1.isEmpty()) {
                    AddressLine1 = data.getJSONObject(i).getString("AddressLine2");
                    AddressLine2 = data.getJSONObject(i).getString("AddressLine3");
                    AddressLine3 = "";
                }
                // If the address field is too long, it is truncated
                if (AddressLine1.length() > 35) {
                    AddressLine1 = AddressLine1.substring(0, 25) + "...";
                }
                PostCode = data.getJSONObject(i).getString("PostCode");
                String Rating = data.getJSONObject(i).getString("RatingValue");
                if (Rating.equals("-1")) {
                    Rating = "exempt";
                }
                // Convert the Rating value to the relevant filename to fetch the right picture
                RatingValue = "rating" + Rating + ".png";

                // Insert each row into the table one by one
                createTable();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }


    /**
     * Method to add the locations to the table rows and the rows to the table
     */
    private void createTable() {

        // Create a row in the table for the Business Name
        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the Business Name into the table row
        TextView name = new TextView(this);
        name.setText(BusinessName);
        name.setTextAppearance(this, R.style.BusinessName);
        tr1.addView(name);
        // Insert the rating value into the table row
        ImageView rating = new ImageView(this);
        try {
            InputStream stream = getAssets().open(RatingValue);
            Drawable d = Drawable.createFromStream(stream, null);
            rating.setImageDrawable(d);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int res = getResources().getIdentifier(RatingValue, "drawable", getPackageName());
//        rating.setImageResource(res);
        rating.setPadding(30, 0, 0, 0);
        tr1.addView(rating);
        tr1.setClickable(true);
        tr1.setTag(id);
        tr1.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 1
        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert the address line 1 and postcode into the table row
        TextView address1 = new TextView(this);
        address1.setText(AddressLine1);
        tr2.addView(address1);
        tr2.setClickable(true);
        tr2.setTag(id);
        tr2.setOnClickListener(tableRowOnClickListener);

        // Create a row in the table for address line 2
        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        // Insert address line 2
        TextView address2 = new TextView(this);
        address2.setText(AddressLine2);
        tr3.addView(address2);

        // Add the rows to the table
        table.addView(tr1);
        table.addView(tr2);
        table.addView(tr3);

        // Only add address line 3 if it exists
        if (AddressLine3 != null && !AddressLine3.isEmpty()) {
            TableRow tr4 = new TableRow(this);
            tr4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            TextView address3 = new TextView(this);
            address3.setText(AddressLine3);
            tr4.addView(address3);
            table.addView(tr4);
        }

        // Create a row in the table for the postcode
        TableRow tr5 = new TableRow(this);
        tr5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TextView postcode = new TextView(this);
        postcode.setText(PostCode);
        tr5.addView(postcode);
        tr5.setPadding(0,0,0,20);
        table.addView(tr5);


        // Padding the left of the table to bring it away from the edge
        // Padding the bottom of the table because otherwise the last row is obscured
        table.setPadding(20, 0, 0, 50);
    }


    /**
     * Method called when the user clicks on one of the rows in the table.
     * It redirects to the MapsActivity where they can see the locations and ratings on a map
     */
    private View.OnClickListener tableRowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SearchName.this, MapsActivity.class);

            // Put the JSON array as a string into the intent
            intent.putExtra("JSON", responseBody);

            // Pass the id of the row so the selected location can be identified later
            String id = v.getTag().toString();
            intent.putExtra("id", id);
            startActivity(intent);
        }
    };
}
