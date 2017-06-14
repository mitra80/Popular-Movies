package com.dipakkr.github.moviesapi.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dipakkr.github.moviesapi.MainActivity;
import com.dipakkr.github.moviesapi.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;


/**
 * Created by deepak on 6/13/17.
 */

public class Authentication extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 100;
    private String TAG = Authentication.class.getSimpleName();
    Button mSkip;

    //Facebook API
    LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker mTokentracker;
    private ProfileTracker profileTracker;
    private AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        checkForLoginStatus();
        FacebookSdk.sdkInitialize(this);

        SignInButton signInButton = (SignInButton) findViewById(R.id.bt_google);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
        mSkip = (Button) findViewById(R.id.bt_skip);
        mSkip.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Facebook SDk
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        loginButton.setReadPermissions("email");

        mTokentracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                nextActivity(currentProfile);
            }
        };
        mTokentracker.startTracking();
        profileTracker.startTracking();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void loginWithFacebook(){

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Toast.makeText(getApplicationContext(), "Login Succesful", Toast.LENGTH_SHORT).show();

                    Profile profile = Profile.getCurrentProfile();
                    nextActivity(profile);
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(Authentication.this, "Error in Login", Toast.LENGTH_SHORT).show();
                }
            });
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
            LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList("user_friends"));
    }

    private void nextActivity(Profile profile){
        if(profile != null){
            Intent main = new Intent(Authentication.this, MainActivity.class);
            main.putExtra("name", profile.getFirstName());
            main.putExtra("surname", profile.getLastName());
            main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
            startActivity(main);
        }
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            //successful login
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(signInResult);
        }

        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void handleSignInResult(GoogleSignInResult signInResult){

        if(signInResult.isSuccess()){
            GoogleSignInAccount account = signInResult.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getPhotoUrl().toString();
            Intent googleLogin = new Intent(Authentication.this,MainActivity.class);
            googleLogin.putExtra("user_name",name);
            googleLogin.putExtra("google_email",email);
            startActivity(googleLogin);
        }else{
            Toast.makeText(this, "Failed Sign in ", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.bt_skip :
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;

            case R.id.bt_google :
                signIn();
                break;

            case R.id.login_button :
                loginWithFacebook();
                break;
        }
    }

    private void checkForLoginStatus(){
        mAccessToken = AccessToken.getCurrentAccessToken();

        if(mAccessToken != null){
            startActivity(new Intent(this,MainActivity.class));
        }
    }

}