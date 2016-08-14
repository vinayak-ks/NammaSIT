package campuschat.wifi.activity;

import campuschat.wifi.BaseActivity;
import campuschat.wifi.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WelcomeActivity extends BaseActivity  {

    private Button mBtnLogin;
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        //mActionBar = getActionBar();
        //mActionBar.hide();
       // mBtnLogin = (Button) findViewById(R.id.welcome_btn_login);
    }

    @Override
    protected void initEvents() {

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }



}
