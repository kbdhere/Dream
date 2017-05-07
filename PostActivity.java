package a12developer.projectalpha20;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import Api.Api;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostActivity extends AppCompatActivity {
    EditText etPostTitle, etPostContent;
    //ViewPager vpPost;
    GridView gvPostItem;
    String user_id;
    String imageFilePath;
    AmazonS3 amazonS3;
    Intent postIntent;
    TextView tvLogoPost;
    ArrayList<Uri> photoUris;
    double latitude, longitude;
    int category;
    ArrayList<String> imageFileEndPoint;
    static final int REQUEST_ITEM = 1005;
    String amazonAddress = "https://s3.ap-northeast-2.amazonaws.com/ybtimage/";
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        token = FirebaseInstanceId.getInstance().getToken();



        etPostTitle = (EditText) findViewById(R.id.et_posttitle);
        etPostContent = (EditText) findViewById(R.id.et_postcontent);
        imageFileEndPoint = new ArrayList<>();

        postIntent = getIntent();
        photoUris = postIntent.getParcelableArrayListExtra("photouris");
        latitude = postIntent.getDoubleExtra("latitude",0);
        longitude = postIntent.getDoubleExtra("longitude",0);
        category = postIntent.getIntExtra("category",0);
        user_id = MainActivity.user_post_id;
    }

    @Override
    public void onBackPressed() {
        photoUris = null;

        super.onBackPressed();

    }

    public ArrayList<Uri> getContentUrisFromFileUris(ArrayList<Uri> uris){
        ArrayList<Uri> contentUri = new ArrayList<>();
        for(int i = 0 ; i < uris.size() ; i++){
            Uri newUri = null;
            try {
                newUri = getContentUriFromFileUri(new File(uris.get(i).toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            contentUri.add(newUri);
        }
        return contentUri;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    private Uri getContentUriFromFileUri(File file) throws FileNotFoundException {
        Uri capturedImage = Uri.parse(
                android.provider.MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        file.getAbsolutePath(), null, null));
        return capturedImage;
    }


    public void onPostBtnClicked(View v) throws IOException {
        post(etPostTitle.getText().toString(), etPostContent.getText().toString(), photoUris, latitude, longitude, category);


    }


    private void post(String titleString, String contentString, ArrayList<Uri> uris, double mLatitude, double mLongitude, int mCategory) {


        PrepareAsync prepareAsync = new PrepareAsync(titleString, contentString, uris, mLatitude, mLongitude, mCategory);
        PostTask postTask = new PostTask();
        postTask.execute(prepareAsync);

    }

    private class PrepareAsync {
        String title;
        String content;
        ArrayList<Uri> arrUris;
        double latitude;
        double longitude;
        int category;

        public PrepareAsync(String title, String content, ArrayList<Uri> arrUris, double latitude, double longitude, int category) {
            this.title = title;
            this.content = content;
            this.arrUris = arrUris;
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public ArrayList<Uri> getArrUris() {
            return arrUris;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getCategory() {
            return category;
        }
    }

    private void amazonUpload(ArrayList<Uri> uris) throws IOException {
        for (int i = 0; i < uris.size(); i++) {
            Bitmap bitmap = getBitmapFromUri(uris.get(i));
            File imageFile = createFileFromBitmap(bitmap, i);
            String fileName = imageFile.getName();
            uploadFile(imageFile, fileName);
            imageFileEndPoint.add("https://s3.ap-northeast-2.amazonaws.com/ybtimage/" + fileName);
        }
    }


    private class PostTask extends AsyncTask<PrepareAsync, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(PostActivity.this, "게시물 업로드 중..", "잠시만 기다려 주세요", true, false);
        }

        @Override
        protected Boolean doInBackground(PrepareAsync... prepareAsyncs) {
            String title = prepareAsyncs[0].getTitle();
            String content = prepareAsyncs[0].getContent();
            ArrayList<Uri> mUris = prepareAsyncs[0].getArrUris();
            double latitude = prepareAsyncs[0].getLatitude();
            double longitude = prepareAsyncs[0].getLongitude();
            int category = prepareAsyncs[0].getCategory();

            ArrayList<Uri> uris = getContentUrisFromFileUris(mUris);



            String id = user_id;

            try {

                amazonUpload(uris);

                Log.d("uploadedFileName", imageFileEndPoint.get(0));

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = null;
                if (imageFileEndPoint.size() == 1) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("passcode",Api.SET_POST_PASS)
                            .addFormDataPart("user_id", id)
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("lat", String.valueOf(latitude))
                            .addFormDataPart("lng", String.valueOf(longitude))
                            .addFormDataPart("category", String.valueOf(category))
                            .addFormDataPart("imgUrl", imageFileEndPoint.get(0))
                            .addFormDataPart("token", token)
                            .build();
                }
                if (imageFileEndPoint.size() == 2) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("passcode",Api.SET_POST_PASS)
                            .addFormDataPart("user_id", id)
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("lat", String.valueOf(latitude))
                            .addFormDataPart("lng", String.valueOf(longitude))
                            .addFormDataPart("category", String.valueOf(category))
                            .addFormDataPart("imgUrl", imageFileEndPoint.get(0))
                            .addFormDataPart("imgUrl_2", imageFileEndPoint.get(1))
                            .addFormDataPart("token", token)
                            .build();
                }
                if (imageFileEndPoint.size() == 3) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("passcode",Api.SET_POST_PASS)
                            .addFormDataPart("user_id", id)
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("lat", String.valueOf(latitude))
                            .addFormDataPart("lng", String.valueOf(longitude))
                            .addFormDataPart("category", String.valueOf(category))
                            .addFormDataPart("imgUrl", imageFileEndPoint.get(0))
                            .addFormDataPart("imgUrl_2", imageFileEndPoint.get(1))
                            .addFormDataPart("imgUrl_3", imageFileEndPoint.get(2))
                            .addFormDataPart("token", token)
                            .build();
                }
                if (imageFileEndPoint.size() == 4) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("passcode",Api.SET_POST_PASS)
                            .addFormDataPart("user_id", id)
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("lat", String.valueOf(latitude))
                            .addFormDataPart("lng", String.valueOf(longitude))
                            .addFormDataPart("category", String.valueOf(category))
                            .addFormDataPart("imgUrl", imageFileEndPoint.get(0))
                            .addFormDataPart("imgUrl_2", imageFileEndPoint.get(1))
                            .addFormDataPart("imgUrl_3", imageFileEndPoint.get(2))
                            .addFormDataPart("imgUrl_4", imageFileEndPoint.get(3))
                            .addFormDataPart("token", token)
                            .build();
                }



                Request request = new Request.Builder()
                        .url(Api.SET_POST_TEST)
                        .post(requestBody)
                        .build();
                Response response = okHttpClient.newCall(request).execute();

                if (response.code() == 200) return true;
                else return false;

            } catch (IOException e) {
                Log.d("PostTask", "post failed", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
            progressDialog.dismiss();
            if (aBoolean) {
                Toast.makeText(PostActivity.this, "게시물이 업로드 되었습니다.", Toast.LENGTH_SHORT).show();
                Log.d("PostTask", "Success");
                startActivityForResult(mainIntent, 1002);
            } else {
                Log.d("PostTask", "Failed");
                Toast.makeText(PostActivity.this, "게시물 업로드 실패!!", Toast.LENGTH_SHORT).show();
                startActivityForResult(mainIntent, 1002);
            }
        }
    }


    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        //리사이즈
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);

        Uri photoUri = uri;

        int width = opts.outWidth;
        int height = opts.outHeight;
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = (int) getSampleRatio(width, height);

        Bitmap resizedBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);

        parcelFileDescriptor.close();

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(photoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        File file = new File(picturePath);
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());

        int orientation = ExifInterface.ORIENTATION_NORMAL;

        if (exif != null) {
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                resizedBitmap = rotateImage(resizedBitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                resizedBitmap = rotateImage(resizedBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                resizedBitmap = rotateImage(resizedBitmap, 270);
                break;
        }
        return resizedBitmap;
    }

    private File createFileFromBitmap(Bitmap bitmap, int i) throws IOException {
        File newFile = new File(getFilesDir(), makeImageFilePath(i));
        FileOutputStream fileOutputStream = new FileOutputStream(newFile);

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

        fileOutputStream.close();

        return newFile;
    }

    private String makeImageFilePath(int i) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        Date date = new Date();
        String strDate = simpleDateFormat.format(date);
        return strDate + i + ".png";

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private float getSampleRatio(int width, int height) {
        int targetSize = 360;
        float ratio;
        if (width > height) {
            //가로형
            if (width > targetSize) {
                ratio = (float) width / targetSize;
            } else {
                ratio = 1f;
            }
        } else {
            //세로형
            if (height > targetSize) {
                ratio = (float) height / targetSize;
            } else ratio = 1f;
        }
        return Math.round(ratio);
    }

    public void uploadFile(File file, String fileName) {
        imageFilePath = fileName;
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:efee9220-69f7-44e3-8e77-a3a3875e2352", // Identity Pool ID
                Regions.AP_NORTHEAST_2);
        amazonS3 = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = new TransferUtility(amazonS3, getApplicationContext());

        amazonS3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        amazonS3.setEndpoint(Api.S3_ENDPOINT);

        TransferObserver observer = transferUtility.upload(
                "ybtimage",     /* 업로드 할 버킷 이름 */
                imageFilePath,    /* 버킷에 저장할 파일의 이름 */
                file/* 버킷에 저장할 파일  */
        );
    }
}
