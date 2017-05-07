package a12developer.projectalpha20;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener listener;
    double latitude, longitude;
    Button btnCur, btnChoice, btnFixed, btnYes, btnNo, btnToOption;
    Location curLocation;
    LatLng curLatLng;
    private final double fixedLatitude = 37.5757166;
    private final double fixedLongitude = 126.976883;
    TextView tvAddress, tvStatus;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = getIntent();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        btnCur = (Button) findViewById(R.id.btn_cur);
        btnChoice = (Button) findViewById(R.id.btn_choice);
        btnFixed = (Button) findViewById(R.id.btn_fixed);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnYes = (Button) findViewById(R.id.btn_yes);
        btnNo = (Button) findViewById(R.id.btn_no);
        btnToOption = (Button) findViewById(R.id.btn_backtooption);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng getLatlng = place.getLatLng();
                latitude = getLatlng.latitude;
                longitude = getLatlng.longitude;
                addMarker(getLatlng.latitude,getLatlng.longitude);
                tvAddress.setText(place.getAddress());

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(GoogleMapsActivity.this, "오류가 발생하였습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                Log.i("googlePlace", "An error occurred: " + status);
            }
        });

        btnCur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMapPoint();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
                addMarker(curLatLng.latitude, curLatLng.longitude);
                tvAddress.setText(getGeocode(curLatLng.latitude, curLatLng.longitude));
                dicisionToMake();

            }
        });
        btnChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askToChoose();
            }
        });
        btnFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                latitude = fixedLatitude;
                longitude = fixedLongitude;
                addMarker(latitude,longitude);
                tvAddress.setText(getGeocode(latitude, longitude));
                dicisionToMake();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentBack();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                askToChoose();
            }
        });
        btnToOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentBack();
            }
        });
    }

    private void intentBack() {
        if(tvAddress.getText().length() > 1){
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("address", tvAddress.getText());
            setResult(RESULT_OK, intent);
            finish();
        }
        else{
            Toast.makeText(this, "위치를 선택해주세요", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMapPoint() {
        long minTime = 10000;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime , 0 , listener);

        curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(curLocation == null){
            curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if(curLocation != null) {
            latitude = curLocation.getLatitude();
            longitude = curLocation.getLongitude();
        }
        else{
            latitude = 37.576209;
            longitude = 126.976901;
            Toast.makeText(this, "일시적으로 위치를 확인하지 못하여\n위치를 임의로 지정합니다.", Toast.LENGTH_LONG).show();
        }
    }
    public void addMarker(Double mLatitude, Double mLongitude){
        mMap.clear();
        LatLng markerPoint = new LatLng(mLatitude, mLongitude);
        String markerGeocode = getGeocode(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(markerPoint).title(markerGeocode));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPoint,16));
        dicisionToMake();
    }
    private void dicisionToMake(){
        btnYes.setVisibility(View.VISIBLE);
        btnNo.setVisibility(View.VISIBLE);
        tvStatus.setText("이 위치로 결정하시겠습니까?");
    }
    private void askToChoose(){
        mMap.clear();
        tvAddress.setText("");
        btnYes.setVisibility(View.INVISIBLE);
        btnNo.setVisibility(View.INVISIBLE);
        tvStatus.setText("위치를 선택해주세요");

    }



    private String getGeocode(Double latitude, Double longitude) {
        String area = null;
        Geocoder myGeocoder = new Geocoder(getApplicationContext(), Locale.KOREA);
        List<Address> list=null;
        try {
            list = myGeocoder.getFromLocation(latitude, longitude, 1);
            if(list.size()>0){
                Address mAddress = list.get(0);
                area = null;
                StringBuffer strBf = new StringBuffer();
                String buf;
                for(int i = 0 ;  (buf = mAddress.getAddressLine(i)) != null; i++){
                    strBf.append(buf+"\n");
                }
                area = strBf.toString();
                return area;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return area;
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
        getMapPoint();
        // Add a marker in Sydney and move the camera
        curLatLng = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 16));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                addMarker(latLng.latitude, latLng.longitude);
                tvAddress.setText(getGeocode(latLng.latitude, latLng.longitude));
            }
        });
    }



    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Log.d("location", longitude+" :: " + latitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
