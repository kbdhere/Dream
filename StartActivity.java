package a12developer.projectalpha20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.model.people.Person;

public class StartActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    SignInButton sibLogin;
    GoogleApiClient mGoogleApiClient;
    TextView tvGoogleId;
    Intent backIntent;
    String id, token, email, userId;
    ProgressBar pbStart;
    SharedPreferences sharedPreferences;
    public static final int RC_SIGN_IN = 9001;
    private static final int RC_GET_TOKEN = 9002;
    private static final int RC_GET_TOKEN_USED = 9003;
    private static final String TAG = "IdTokenActivity";
    private static final String SHARED_PREF_FIRST_SIGN_IN = "7001";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        sibLogin = (SignInButton) findViewById(R.id.sib_login);
        tvGoogleId = (TextView) findViewById(R.id.tv_googleid);
        sibLogin.setOnClickListener(this);
        backIntent = getIntent();
        pbStart = (ProgressBar) findViewById(R.id.pb_start);
        pbStart.getIndeterminateDrawable().setColorFilter(0xFF5b9bd4, android.graphics.PorterDuff.Mode.SRC_ATOP);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        int firstSignIn = sharedPreferences.getInt(SHARED_PREF_FIRST_SIGN_IN, -1);

        if(firstSignIn == 1){
            getIdTokenUsed();
            sibLogin.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sib_login:
                getIdToken();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    private void getIdTokenUsed() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }
    private void getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_GET_TOKEN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

            if (result.isSuccess()) {
                String idToken = result.getSignInAccount().getIdToken();
                // TODO(developer): send token to server and validate
                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(SHARED_PREF_FIRST_SIGN_IN, 1);
                editor.commit();
            }
            // [END get_id_token]

            handleSignInResult(result);
        }
        if (requestCode == RC_GET_TOKEN_USED){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

            if (result.isSuccess()) {
                String idToken = result.getSignInAccount().getIdToken();
                // TODO(developer): send token to server and validate
            }
            // [END get_id_token]

            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            id = acct.getDisplayName();
            userId = acct.getId();
            token = acct.getIdToken();
            email = acct.getEmail();
            tvGoogleId.setText(acct.getDisplayName()+"님 환영합니다.");
            StartActivityAsync startActivityAsync = new StartActivityAsync();
            startActivityAsync.execute(id,userId,token,email);
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, "로그인 정보가 이상해요", Toast.LENGTH_SHORT).show();
        }
    }
    class StartActivityAsync extends AsyncTask<String, Void, Void>{
        Intent intent;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbStart.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String user_displayName = strings[0];
            String user_id = strings[1];
            String user_token = strings[2];
            String user_email = strings[3];

            intent = new Intent();
            intent.putExtra("user_displayName",user_displayName);
            intent.putExtra("user_id",user_id);
            intent.putExtra("user_token",user_token);
            intent.putExtra("user_email",user_email);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
