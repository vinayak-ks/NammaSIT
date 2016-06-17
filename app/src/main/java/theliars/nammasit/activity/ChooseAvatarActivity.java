package theliars.nammasit.activity;

import theliars.nammasit.BaseActivity;
import theliars.nammasit.adapter.AvatarAdapter;
import theliars.nammasit.R;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ChooseAvatarActivity extends BaseActivity implements OnItemClickListener {
    private GridView gridView;
    AvatarAdapter adapter;


    private final static int[] images = new int[] { R.drawable.avatar11, R.drawable.avatar2,
            R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
            R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10,
            R.drawable.avatar11, R.drawable.avatar12, };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setavater);
        initViews();
        initData();
        initEvents();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        intent.putExtra("result", position);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void initViews() {
        gridView = (GridView) findViewById(R.id.gridview);
    }

    private void initData() {
        setTitle(getString(R.string.setting_text_myprofile_avatar));
        adapter = new AvatarAdapter(images, this);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void initEvents() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        gridView.setOnItemClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
