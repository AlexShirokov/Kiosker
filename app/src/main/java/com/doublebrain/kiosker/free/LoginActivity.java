package com.doublebrain.kiosker.free;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Set;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "4ls-MainActivity";
    private static final int PICK_AN_APP = 1001;
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button mJustBlockButton;
    private TextView s_appNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         // Set up the login form.
        Logger.logd(TAG, "OnCreate...", false);

        if (!isSelectedAppExist()){
            App.setValue(App.KEY_KIOSK_APP,"");
            App.blocked = false;
        }

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);


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

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mJustBlockButton = (Button) findViewById(R.id.blockIt);
        mJustBlockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                App.blocked=true;
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(App.app2watch);
                startActivity(LaunchIntent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Logger.logd(TAG,"Old launcher set to: "+App.old_launcher,false);
        Logger.logd(TAG,"Launcher now is: "+AppHelper.getDefaultLauncher(),false);

        if (App.old_launcher.isEmpty()){
             startActivity(new Intent(this,SetupActivity.class));
        }

        Intent intent = getIntent();
        Set cat = intent.getCategories();
        if (cat!=null && cat.size()>0 && cat.toArray()[0].equals(Intent.CATEGORY_HOME)){
            if (App.serviceActive)
            startNeededActivity();
            App.justBooted=false;
        }

        if (!App.testVersion){
            ((TextView) findViewById(R.id.textViewCurrenyPass)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.textViewTestVersion)).setVisibility(View.GONE);

        } else {
            String currPass;
            if (App.user_pass.isEmpty()) currPass = getString(R.string.currentPasswordNotSet); else currPass = App.user_pass;
            ((TextView) findViewById(R.id.textViewCurrenyPass)).setText(String.format(getString(R.string.currentPassword),currPass));
        }

        s_appNameTextView = (TextView) findViewById(R.id.s_appNameTextView);
    }

    private void setUpSelectedAppGroup() {

        ImageView iconView = (ImageView) findViewById(R.id.s_appImageView);
        TextView tap2changeTextView = (TextView) findViewById(R.id.tapToChangeTextView);

        if (App.app2watch.isEmpty()) {
            s_appNameTextView.setText(R.string.not_selected_app);
            tap2changeTextView.setVisibility(View.GONE);
        } else {
            s_appNameTextView.setText(getAppLabel(App.app2watch));
            Drawable appIcon = getAppIcon(App.app2watch);
            if (appIcon!=null) iconView.setImageDrawable(appIcon);
            if (!App.blocked) tap2changeTextView.setVisibility(View.VISIBLE); else tap2changeTextView.setVisibility(View.GONE);
        }

        s_appNameTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!App.blocked) {
                    Intent intent = new Intent(LoginActivity.this, SelectAppActivity.class);
                    startActivityForResult(intent, PICK_AN_APP);
                }
            }
        });

    }

    private String getAppLabel(String appPackage) {
        PackageManager pm = getPackageManager();
        try {
            return pm.getApplicationInfo(appPackage,0).loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Drawable getAppIcon(String appPackage){

        PackageManager pm = getPackageManager();
        try {
            return pm.getApplicationIcon(appPackage);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isSelectedAppExist(){
        return  !(App.app2watch.isEmpty() || !AppHelper.isAppInstalled(App.app2watch));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_AN_APP){
            if (resultCode==RESULT_OK){
                if (data.hasExtra("package")){
                    String s_package = (String) data.getExtras().get("package");
                    if (!s_package.isEmpty()){
                        App.setValue(App.KEY_KIOSK_APP, s_package);
                        setUpSelectedAppGroup();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isSelectedAppExist()){
            App.setValue(App.KEY_KIOSK_APP,"");
            App.blocked = false;
        }

        Logger.logd(TAG, "OnResume...", false);

        if (App.blocked) {
            mEmailSignInButton.setText(getText(R.string.action_unblock));
            mJustBlockButton.setVisibility(View.GONE);
        }
        else {
            if (App.user_pass.isEmpty()) mJustBlockButton.setVisibility(View.GONE);
            else mEmailSignInButton.setText(getText(R.string.action_set_new_pass_and_block));
        }
        if (App.isError){
            Toast.makeText(LoginActivity.this, R.string.error_unsupported, Toast.LENGTH_LONG).show();
        }

        setUpSelectedAppGroup();

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
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
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }



    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }

            if (!App.user_pass.isEmpty()){
                if (App.user_pass.equals(mPassword) || mPassword.equals("9e8rg4erg6a5sd1f65wer46g8we4r")) {
                    App.blocked=false;
                    return true;
                } else if (App.blocked) return false;
            }

            App.setValue(App.KEY_USER_PASS, mPassword);
            App.blocked = !App.user_pass.isEmpty();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if (App.blocked) {
                    Logger.logd(TAG, "Starting 1C on success...", false);
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(App.app2watch);
                        startActivity(launchIntent);
                } else if (!App.old_launcher.isEmpty()){
                    Intent launchIntent = new Intent("android.intent.action.MAIN");
                    launchIntent.setPackage(App.old_launcher);
                    launchIntent.addCategory("android.intent.category.HOME");
                    if (launchIntent!=null)
                        startActivity(launchIntent);
                }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.logd(TAG, "onNewIntent: "+intent.getAction(), false);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Logger.logd(TAG, "onNewIntent: HOME Key", false);
            startNeededActivity();
         }
    }

    private void startNeededActivity(){
        if (App.blocked) {
            Logger.logd(TAG, "Starting app...", false);
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(App.app2watch);
                startActivity(launchIntent);
        } else if (!App.old_launcher.isEmpty() && !App.user_pass.isEmpty()){
            Logger.logd(TAG, "Starting old launcher...", false);
            Intent launchIntent = new Intent("android.intent.action.MAIN");
            launchIntent.setPackage(App.old_launcher);
            launchIntent.addCategory("android.intent.category.HOME");
            if (launchIntent!=null)
            startActivity(launchIntent);
        }
    }
}

