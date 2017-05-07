package a12developer.projectalpha20;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;

import java.util.ArrayList;

import layout.RadiusImageView;

public class UploadOption extends AppCompatActivity implements View.OnClickListener{
    ArrayList<Uri> photoUris;
    GridView gv_ImgSelect;
    LinearLayout llImgSelect, llLocationSelect, llGridContainer;
    TextView tvCategory, tvAddress;
    int category;
    View vLine;
    Activity act = this;
    public static final int REQUEST_PHOTO_SELECT = 1001;
    public static final int REQUEST_LOCATION_SELECT = 1002;
    public static final int REQUEST_CAMERA = 1003;
    public static final int REQUEST_GALLERY = 1004;
    double getLatitude = 0;
    double getLongitude = 0;
    String address;
    Button btn_Back, btn_Logo, btn_Next, btn_Photo, btn_location;
    RadioGroup rgCategoryUp;
    MyGridViewAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_option);

        btn_Back = (Button) findViewById(R.id.btn_backarrow);
        btn_Logo = (Button) findViewById(R.id.btn_logo);
        llLocationSelect = (LinearLayout) findViewById(R.id.ll_locationselect);


        btn_Next = (Button) findViewById(R.id.btn_next);
        btn_Photo = (Button) findViewById(R.id.btn_imgselect);
        btn_location = (Button) findViewById(R.id.btn_locationselect);
        llImgSelect = (LinearLayout) findViewById(R.id.ll_imgselect);
        llGridContainer = (LinearLayout) findViewById(R.id.ll_gridContainer);
        tvCategory = (TextView) findViewById(R.id.tv_category);
        tvAddress = (TextView) findViewById(R.id.tv_addressoption);
        vLine = findViewById(R.id.v_line);
        rgCategoryUp = (RadioGroup) findViewById(R.id.rg_categoryup);
        adapter = new MyGridViewAdapter(this);
        photoUris = null;

        btn_Back.setOnClickListener(this);
        btn_location.setOnClickListener(this);
        btn_Photo.setOnClickListener(this);
        btn_Next.setOnClickListener(this);

        rgCategoryUp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.rb_childup :
                        category = 1;
                        break;
                    case R.id.rb_furnitureup :
                        category = 2;
                        break;
                    case R.id.rb_electricup :
                        category = 3;
                        break;
                    case R.id.rb_clothup :
                        category = 4;
                        break;
                    case R.id.rb_booksup :
                        category = 5;
                        break;
                    case R.id.rb_computerup :
                        category = 6;
                        break;
                    case R.id.rb_etcup :
                        category = 7;
                        break;

                }
            }
        });

        if(tvAddress.getText().length() > 1){
            llLocationSelect.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_backarrow :
                onBackPressed();
                break;
            case R.id.btn_logo:
                Intent mainIntent = new Intent(this,MainActivity.class);
                startActivity(mainIntent);
                break;
            case R.id.btn_imgselect :
                startTedPicker();
                //startGetPhoto();
                break;
            case R.id.btn_locationselect :
                startLocationSelect();
                break;


            case R.id.btn_next :
                if(category == 0){
                    Toast.makeText(this, "카테고리를 지정하여 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(photoUris == null){
                    Toast.makeText(this, "사진을 등록해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(getLatitude == 0 && getLongitude == 0){
                    Toast.makeText(this, "위치를 등록해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent toPostIntent = new Intent(this, PostActivity.class);
                toPostIntent.putExtra("category", category);
                toPostIntent.putExtra("latitude", getLatitude);
                toPostIntent.putExtra("longitude", getLongitude);
                toPostIntent.putParcelableArrayListExtra("photouris", photoUris);
                startActivity(toPostIntent);
                break;

        }
    }
    private AlertDialog startGetPhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진선택방법")
                .setItems(R.array.photoSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogOption(i);
                    }
                });
        return builder.create();
    }
    private void dialogOption(int i){
        switch (i){
            case 0 :
                cameraStart();
                break;
            case 1 :
                galleryStart();
                break;
            case 2 :
                break;
        }
    }
    public void cameraStart(){

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(this.getPackageManager()) != null){
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }
    public void galleryStart(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_GALLERY);
        }
    }

    private void startTedPicker() {
        Config config = new Config();
        config.setSelectionMin(1);
        config.setSelectionLimit(4);
        //config.setTabBackgroundColor(R.color.Orange);
        //config.setTabBackgroundColor(R.color.Orange);

        ImagePickerActivity.setConfig(config);
        Intent photoIntent  = new Intent(this, ImagePickerActivity.class);
        startActivityForResult(photoIntent,REQUEST_PHOTO_SELECT);
    }
    private void startLocationSelect(){
        tvAddress.setText("");
        Intent locationIntent = new Intent(this, GoogleMapsActivity.class);
        startActivityForResult(locationIntent, REQUEST_LOCATION_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PHOTO_SELECT && resultCode == Activity.RESULT_OK){
            photoUris = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
            gv_ImgSelect = (GridView) findViewById(R.id.gv_imgselect);
            gv_ImgSelect.setAdapter(adapter);
            llGridContainer.setVisibility(View.VISIBLE);
        }
        if(requestCode == REQUEST_LOCATION_SELECT && resultCode == RESULT_OK){
            getLatitude = data.getDoubleExtra("latitude",0);
            getLongitude = data.getDoubleExtra("longitude",0);
            address = data.getStringExtra("address");
            llLocationSelect.setVisibility(View.VISIBLE);
            tvAddress.setText(address);
        }
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            photoUris.add(data.getData());

        }
    }
    class MyGridViewAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;

        public MyGridViewAdapter(Context context){
            this.context = context;
            inflater = (LayoutInflater) act.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return photoUris.size();
        }

        @Override
        public Object getItem(int i) {
            return photoUris.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {



            RadiusImageView imageView = new RadiusImageView(context);
            int maxHeight = gv_ImgSelect.getWidth() / 4;
            imageView.setMaxHeight(maxHeight);
            imageView.setPadding(8,8,8,8);
            imageView.setMinimumHeight(maxHeight);

            Glide.with(getApplicationContext())
                    .load(photoUris.get(i).toString())
                    .crossFade()
                    .centerCrop()
                    .into(imageView);


            return imageView;
        }

    }
}

