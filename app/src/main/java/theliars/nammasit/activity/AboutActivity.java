package theliars.nammasit.activity;

import theliars.nammasit.BaseActivity;
import theliars.nammasit.R;
import theliars.nammasit.adapter.AboutAdapter;
import theliars.nammasit.adapter.AvatarAdapter;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

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
