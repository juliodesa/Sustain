package com.project.sustain.controllers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.project.sustain.R;
import com.project.sustain.model.Address;
import com.project.sustain.model.Report;
import com.project.sustain.model.User;
import com.project.sustain.model.WaterPurityReport;
import com.project.sustain.model.WaterSourceReport;
import com.project.sustain.services.FetchAddressConstants;
import com.project.sustain.services.FetchAddressIntentService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static com.project.sustain.services.FetchAddressConstants.LOCATION_RESULT_LATITUDE;
import static com.project.sustain.services.FetchAddressConstants.LOCATION_RESULT_LONGITUDE;
import static com.project.sustain.services.FetchAddressConstants.RESULT_DATA_KEY;

/**
 * Activity that enables user to set the address for a report.
 * @author Marcia
 */

public class SetAddressActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private User mUser;
    private android.location.Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested;
    private boolean mLocationPermissionGranted;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = SetAddressActivity.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 30;
    private String requestedAction = "";
    private EditText editPlaceName;
    private EditText editStreetAddress1;
    private EditText editStreetAddress2;
    private EditText editCity;
    private EditText editState;
    private EditText editCountry;
    private EditText editZipCode;
    private EditText editLatitude;
    private EditText editLongitude;

    private LocationRequest mLocationRequest;
    private Report mReport;
    // --Commented out by Inspection (4/10/2017 21:14 PM):private boolean puritySelected = false;
    private final Calendar currentCalendar = new GregorianCalendar();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentCalendar.setTimeZone(TimeZone.getTimeZone("EST"));
        mUser = (User) getIntent().getSerializableExtra("user");
        if ((mUser == null) || "".equals(mUser.getUserId())) {
            Toast.makeText(getApplicationContext(), "User profile not found.\n" +
            "Please complete your profile before reporting.", Toast.LENGTH_LONG).show();
            //go back to MainActivity
            finish();
        }
        setContentView(R.layout.activity_get_address);

        //make different report type options visible only if user is not "USER" type.
        final RadioGroup reportOptions = (RadioGroup) findViewById(R.id.radReportType);
        reportOptions.setVisibility((mUser.getUserPermissions()
                .isAbleToCreatePurityReports() ? View.VISIBLE : View.GONE));
        if (reportOptions.getVisibility() == View.VISIBLE) {
            reportOptions.check(R.id.radBtnWaterReport); //set Water Report as default.
        } else {
            mReport = new WaterSourceReport();
        }

        editPlaceName = (EditText) findViewById(R.id.editName);
        editStreetAddress1 = (EditText) findViewById(R.id.editStreetAddress1);
        editStreetAddress2 = (EditText) findViewById(R.id.editStreetAddress2);
        editCity = (EditText) findViewById(R.id.editCity);
        editState = (EditText) findViewById(R.id.editState);
        editCountry = (EditText) findViewById(R.id.editCountry);
        editZipCode = (EditText) findViewById(R.id.editZipCode);
        editLatitude = (EditText) findViewById(R.id.editLatitude);
        editLongitude = (EditText) findViewById(R.id.editLongitude);

        TextView txtReporterName = (TextView) findViewById(R.id.txtReporterName);
        txtReporterName.setText(mUser.getUserName());
        Button btnContinue = (Button) findViewById(R.id.btnContinue);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        ImageButton btnGetMyLocation = (ImageButton) findViewById(R.id.btnGetMyLocation);
        Button btnGetLatLong = (Button) findViewById(R.id.btnGetLatLong);


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();


        /*
         * Sends the selected report to the appropriate activity
         * for the rest of the data to be entered.
         */
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPlaceName.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter a friendly name " +
                        "for this place.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //send report to next activity
                Intent intent;
                if (reportOptions.getVisibility() == View.VISIBLE) {
                    if (reportOptions.getCheckedRadioButtonId() == R.id.radBtnPurityReport) {
                        intent = new Intent(SetAddressActivity.this, PurityReportActivity.class);
                        mReport = new WaterPurityReport();
                    } else {
                        intent = new Intent(SetAddressActivity.this, SourceReportActivity.class);
                        mReport = new WaterSourceReport();
                    }
                } else {
                    intent = new Intent(SetAddressActivity.this, SourceReportActivity.class);
                    mReport = new WaterSourceReport();
                }
                Address address = saveAddress();
                mReport.setAddress(address);
                mReport.setDateOfReport(obtainDate());
                mReport.setTimeOfReport(obtainTime());
                mReport.setReporterName(mUser.getUserName());
                mReport.setReporterUserId(mUser.getUserId());

                intent.putExtra("report", mReport);
                startActivity(intent);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close and go back to Reports Screen
                finish();
            }
        });

        View.OnClickListener ButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only start the service to fetch the address if GoogleApiClient is
                // connected.
                if (mGoogleApiClient.isConnected()) {
                    if (mLastLocation == null) {
                        requestPermissionForLocation();
                    }
                    if (v.getId() == R.id.btnGetMyLocation) {
                        //get Address from Lat/Long of current location
                        if (mLastLocation != null) {
                            Log.d(TAG, "Starting intent service");
                            requestedAction = FetchAddressConstants.ACTION_FETCH_ADDRESS;
                            startIntentService(requestedAction);
                            editLatitude.setText(String.format(Locale.US, "%f",
                                    mLastLocation.getLatitude()));
                            editLongitude.setText(String.format(Locale.US, "%f",
                                    mLastLocation.getLongitude()));
                        }
                    } else if (v.getId() == R.id.btnGetLatLong) {
                        //get Lat/Long from user-entered address
                        requestedAction = FetchAddressConstants.ACTION_FETCH_LAT_LONG;
                          startIntentService(requestedAction);
                    }
                } else {
                    // If GoogleApiClient isn't connected, process the user's request by
                    // setting mAddressRequested to true. Later, when GoogleApiClient connects,
                    // launch the service to fetch the address. As far as the user is
                    // concerned, pressing the Fetch Address button
                    // immediately kicks off the process of getting the address.
                    mAddressRequested = true;
                }
            }
        };

        btnGetMyLocation.setOnClickListener(ButtonListener);
        btnGetLatLong.setOnClickListener(ButtonListener);

        mResultReceiver = new AddressResultReceiver(new Handler());

    }

    private Address saveAddress() {
        Address address = new Address();
        address.setPlaceName(editPlaceName.getText().toString());
        address.setStreetAddress1(editStreetAddress1.getText().toString());
        address.setStreetAddress2(editStreetAddress2.getText().toString());
        address.setCity(editCity.getText().toString());
        address.setStateOrProvince(editState.getText().toString());
        address.setCountry(editCountry.getText().toString());
        address.setZipCode(editZipCode.getText().toString());
        if ((editLatitude.getText().length() > 0) && (editLongitude.getText().length() > 0)) {
            com.project.sustain.model.Location location = new com.project.sustain.model.Location(
                    Double.parseDouble(editLatitude.getText().toString()),
                    Double.parseDouble(editLongitude.getText().toString()));
            address.setLocation(location);

        }
        return address;
    }

    private String obtainDate() {
        Log.d(TAG, "date: " + DateFormat.getDateInstance().format(new Date()));
        return DateFormat.getDateInstance().format(new Date());

    }

    private String obtainTime() {

        int hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = currentCalendar.get(Calendar.MINUTE);
        int seconds = currentCalendar.get(Calendar.SECOND);
        String hourFormatted = ((hour < 10 ? "0" : "") + hour);
        String minuteFormatted = ((minute < 10 ? "0" : "") + minute);
        String secondsFormatted = ((seconds < 10 ? "0" : "") + seconds);
        return hourFormatted + ":" + minuteFormatted + ":" + secondsFormatted;
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        mReport = null;
        super.onResume();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.

        requestPermissionForLocation();
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "No geocoder available",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startIntentService(requestedAction);
            }
        } else {
            Log.d(TAG, "Unable to get last location.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * gets the address data based on editLatitude and editLongitude of current location.
     * data returned in callback AddressResultReceiver.onReceiveResult.
     * @param action FetchAddressIntentService constant indicating the type of data to retrieve
     *               (Address or LatLong).
     */
    private void startIntentService(String action) {
        Log.d(TAG, "Starting address fetching service");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.setAction(action);
        intent.putExtra(FetchAddressConstants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressConstants.LOCATION_DATA_EXTRA, mLastLocation);
        if (action.equals(FetchAddressConstants.ACTION_FETCH_LAT_LONG)) {
            Address address = saveAddress();
            intent.putExtra(FetchAddressConstants.LOCATION_ADDRESS_EXTRA,
                    address.toString());
        } else {
            intent.putExtra(FetchAddressConstants.LOCATION_ADDRESS_EXTRA, "");
        }
        startService(intent);

    }

    /*
  * Requests location permission, so that we can get the location of the device.
  * The result of the permission request is handled by a callback,
  * onRequestPermissionsResult.
  */
    private void requestPermissionForLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        Log.d(TAG, "Permission granted status: " + mLocationPermissionGranted);

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }


    }

    /*
     * Handles the result of the request for location permissions.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult fired");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


    /**
     * Attempts to display the lines of the address information received in the
     * correct spaces on the Submit Report form.
     * @param addressParts array list of lines of the address.
     */
    private void displayAddressOutput(ArrayList<String> addressParts) {
        //try to guess where the parts go.
        if (!addressParts.isEmpty()) {
            this.editStreetAddress1.setText(addressParts.get(0));
        }
        if (addressParts.size() > 2) {
            String[] cityStateZip = addressParts.get(1).split(",");

            this.editCity.setText(cityStateZip[0]);
            if (cityStateZip.length > 1) {
                String[] stateZip = cityStateZip[1].split(" ");

                if (stateZip.length > 1) {
                    editState.setText(stateZip[1]);
                }
            }
            this.editCountry.setText(addressParts.get(2));

        }
    }

    /**
     * Displays error message received as result of address lookup.
     * @param error string containing error message
     */
    private void displayAddressError(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    private void displayLatLongOutput(double latitude, double longitude) {
        editLatitude.setText(String.valueOf(latitude));
        editLongitude.setText(String.valueOf(longitude));
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressConstants.SUCCESS_RESULT) {
                if (requestedAction.equals(FetchAddressConstants.ACTION_FETCH_ADDRESS)) {
                    // Display the address string
                    // or an error message sent from the intent service.
                    ArrayList<String> address = resultData.
                            getStringArrayList(
                                    FetchAddressConstants.LOCATION_RESULT_ADDRESS_AS_LIST);


                    displayAddressOutput(address);
                } else if (requestedAction.equals(FetchAddressConstants.ACTION_FETCH_LAT_LONG)) {
                    double latitude = resultData.getDouble(LOCATION_RESULT_LATITUDE);
                    double longitude = resultData.getDouble(LOCATION_RESULT_LONGITUDE);
                    displayLatLongOutput(latitude, longitude);
                }
            } else {
                displayAddressError(resultData.getString(RESULT_DATA_KEY));
            }


        }
    }
}
