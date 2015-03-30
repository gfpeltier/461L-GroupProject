package com.funbetweenus.funbetweenus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funbetweenus.funbetweenus.utils.OnTaskCompleted;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends PlusBaseActivity implements LoaderCallbacks<Cursor>, OnTaskCompleted {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mEmailLoginFormView;
    private SignInButton mPlusSignInButton;
    private View mSignOutButtons;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find the Google+ sign in button.
        mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
        if (supportsGooglePlayServices()) {
            // Set a listener to connect the user when the G+ button is clicked.
            mPlusSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
        } else {
            // Don't offer G+ sign in if the app's version is too low to support Google Play
            // Services.
            mPlusSignInButton.setVisibility(View.GONE);
            return;
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailLoginFormView = findViewById(R.id.email_login_form);
        mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);
    }

    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }


    public void launchMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    protected void onPlusClientSignIn() {
        //Set up sign out and disconnect buttons.
        Button signOutButton = (Button) findViewById(R.id.plus_sign_out_button);
        signOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        Button disconnectButton = (Button) findViewById(R.id.plus_disconnect_button);
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                revokeAccess();
            }
        });
        new CheckExistingUserTask().execute();
        launchMainActivity();
    }

    @Override
    protected void onPlusClientBlockingUI(boolean show) {
        showProgress(show);
    }

    @Override
    protected void updateConnectButtonState() {
        //TODO: Update this logic to also handle the user logged in by email.
        boolean connected = getPlusClient().isConnected();

        mSignOutButtons.setVisibility(connected ? View.VISIBLE : View.GONE);
        mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
        mEmailLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPlusClientRevokeAccess() {
        // TODO: Access to the user's G+ account has been revoked.  Per the developer terms, delete
        // any stored user data here.
    }

    @Override
    protected void onPlusClientSignOut() {

    }

    /**
     * Check if the device supports Google Play Services.  It's best
     * practice to check first rather than handling this as an error case.
     *
     * @return whether the device supports Google Play Services
     */
    private boolean supportsGooglePlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onTaskCompleted(JSONObject obj) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    class CheckExistingUserTask extends AsyncTask<Void, Void, String>{
        TelephonyManager telephonyManager;
        String deviceId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();

        }

        @Override
        protected String doInBackground(Void... arg0) {
            /*
             * Will make http call here This call will download required data
             * before launching the app
             * example:
             * 1. Downloading and storing in SQLite
             * 2. Downloading images
             * 3. Fetching and parsing the xml / json
             * 4. Sending device information to server
             * 5. etc.,
             */
            URL url;
            HttpURLConnection conn = null;
            BufferedReader read = null;
            String strUrl = "http://" + getString(R.string.serverIPAddress) + "/php/checkDevice.php?deviceId=" + deviceId;
            InputStream output = null;
            StringBuilder builder = new StringBuilder();
            String charset = "UTF-8";
            try{
                url = new URL(strUrl);
                Log.v("Query", url.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(15*1000);
                conn.connect();
                //conn.setRequestProperty("Accept-Charset", charset);
                //byte[] out = new byte[1024];
                //output = conn.getInputStream();
                read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln = null;
                while((ln = read.readLine()) != null){
                    builder.append(ln);
                }
                Log.i("Message",conn.getResponseMessage());
                Log.i("Code", ""+conn.getResponseCode());
                return builder.toString();
                //int amt = output.read();
                //Log.v("NUMBYTESREAD", ""+amt);
                //byte[] xmlOut = new byte[100000];
                //output.read(xmlOut);
                //String oString = new String(xmlOut, "UTF-8");
                //Log.v("XML",oString);
                //output.close();
                //return oString;
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR", ""+e.getMessage());
                Log.e("ERROR", "" + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("CheckDBResult", result);
            // After completing http call
            // will close this activity and lauch main activity
            JSONObject rootOfResult;
            JSONObject userObject = null;
            String code = "";
            try{
                rootOfResult = new JSONObject(result);
                userObject = rootOfResult.getJSONObject("object");
                code = (String) rootOfResult.get("code");
                Log.i("ResultCode", code);
            }catch(Exception e){
                e.printStackTrace();
            }

            if(code.equals("success")){
                User currentUser = null;
                assert userObject != null;
                try {
                    currentUser = new User(userObject.getString("user_name"), userObject.getString("device_id"), userObject.getString("user_email"), userObject.getString("user_id"), Integer.parseInt(userObject.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent main = new Intent(LoginActivity.this, MainActivity.class).putExtra("currentUser", currentUser);
                startActivity(main);
            }else{
                new CreateUserEntry().execute(deviceId);
            }
        }
    }


    class CreateUserEntry extends AsyncTask<String, Void, String>{

        String deviceId;
        String uEmail = null;
        Person user = null;

        @Override
        protected void onPreExecute(){
            while(!mGoogleApiClient.isConnected()){
                try {
                    if(!isPlusClientConnecting()){
                        mGoogleApiClient.connect();
                    }
                    Thread.sleep(2000);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
                //TODO: Need to fix this with Necessary code for GoogleApiClient
                if(baseUser != null){
                    user = baseUser;
                    uEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
                }
                //user = mPlusClient.;
                //uEmail = mPlusClient.getAccountName();
            }
        }

        @Override
        protected String doInBackground(String... arg0){
            deviceId = arg0[0];
            String uId = null;
            String uName = null;

            if(user != null && user.hasId()){
                uId = user.getId();
            }
            if(user != null && user.hasName()){
                uName = user.getName().getGivenName();
            }
            URL url;
            HttpURLConnection conn = null;
            BufferedReader read = null;
            String strUrl = "http://" + getString(R.string.serverIPAddress) + "/php/addUser.php?deviceId=" + deviceId + "&userEmail=" + uEmail + "&userName=" + uName +"&userId=" + uId;
            InputStream output = null;
            StringBuilder builder = new StringBuilder();
            String charset = "UTF-8";
            try{
                url = new URL(strUrl);
                Log.v("Query", url.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(15*1000);
                conn.connect();
                //conn.setRequestProperty("Accept-Charset", charset);
                //byte[] out = new byte[1024];
                //output = conn.getInputStream();
                read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln = null;
                while((ln = read.readLine()) != null){
                    builder.append(ln);
                }
                Log.i("Message",conn.getResponseMessage());
                Log.i("Code", ""+conn.getResponseCode());
                return builder.toString();
                //int amt = output.read();
                //Log.v("NUMBYTESREAD", ""+amt);
                //byte[] xmlOut = new byte[100000];
                //output.read(xmlOut);
                //String oString = new String(xmlOut, "UTF-8");
                //Log.v("XML",oString);
                //output.close();
                //return oString;
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR", ""+e.getMessage());
                Log.e("ERROR", "" + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.i("CheckDBAddResult", result);
            JSONObject rootOfResult = null;
            JSONObject userObject = null;
            String code = "";
            try{
                rootOfResult = new JSONObject(result);
                userObject = rootOfResult.getJSONObject("result");
                code = (String) rootOfResult.get("code");
                Log.i("AddResultCode", code);
            }catch(Exception e){
                e.printStackTrace();
            }

            if(code.equals("success")){
                try {
                    Log.i("UserAddSuccess", (String) rootOfResult.get("result"));
                }catch(Exception e){
                    e.printStackTrace();
                }
                User currentUser = null;
                assert userObject != null;
                try {
                    currentUser = new User(userObject.getString("user_name"), userObject.getString("device_id"), userObject.getString("user_email"), userObject.getString("user_id"), Integer.parseInt(userObject.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("CurrentUserName", currentUser.getName());
                Intent main = new Intent(LoginActivity.this, MainActivity.class).putExtra("currentUser", currentUser);
                startActivity(main);
            }else{
                try{
                    Log.i("UserAddFailed", (String) rootOfResult.get("result"));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }

    }


    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }



        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



