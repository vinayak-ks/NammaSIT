package campuschat.wifi.activity;

import campuschat.wifi.BaseActivity;
import campuschat.wifi.R;

import android.os.Bundle;
import android.view.MenuItem;

public class AboutActivity extends BaseActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initEvents();
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initEvents() {
        setTitle("Vicasin Techies");
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    // actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    } 

}
