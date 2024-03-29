package campuschat.wifi.activity;

import campuschat.wifi.ActivitiesManager;
import campuschat.wifi.BaseActivity;
import campuschat.wifi.BaseFragment;
import campuschat.wifi.R;
import campuschat.wifi.activity.maintabs.FriendsFragment;
import campuschat.wifi.activity.maintabs.MessageFragment;
import campuschat.wifi.activity.maintabs.SettingFragment;
import campuschat.wifi.adapter.TabPagerAdapter;
import campuschat.wifi.socket.udp.IPMSGConst;
import campuschat.wifi.socket.udp.UDPMessageListener;
import campuschat.wifi.socket.udp.UDPMessageListener.OnNewMsgListener;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.viewbadger.BadgeView;

public class MainTabActivity extends BaseActivity implements ActionBar.TabListener,
        OnNewMsgListener {

    private List<BaseFragment> fragments;
    private TabPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    public BadgeView mBadgeView;
    private View mTabView;
    private long ExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintabs);
        mUDPListener = UDPMessageListener.getInstance(getApplicationContext());
        mUDPListener.addMsgListener(this);
        initEvents();
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUDPListener.removeMsgListener(this);

    }

    @Override
    protected void initEvents() {
        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        fragments = new ArrayList<BaseFragment>();
        fragments.add(new FriendsFragment(mContext));
        fragments.add(new MessageFragment(mContext));
        fragments.add(new SettingFragment(mContext));

        mPagerAdapter = new TabPagerAdapter(getFragmentManager(), fragments);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });


        for (int i = 0; i < 3; i++) {
            mTabView = getLayoutInflater().inflate(R.layout.actionbar_tab_layout, null);
            TextView tvTabText = (TextView) mTabView.findViewById(R.id.tab_text);
            mBadgeView = (BadgeView) mTabView.findViewById(R.id.tab_badge);
            mBadgeView.hide();

            switch (i) {
                case 0:
                    tvTabText.setText(getString(R.string.maintab_text_nearby));
                    break;

                case 1:
                    tvTabText.setText(getString(R.string.maintab_text_message));
                    break;

                case 2:
                    tvTabText.setText(getString(R.string.maintab_text_setting));
                    break;
            }
            mActionBar.addTab(mActionBar.newTab().setCustomView(mTabView).setTabListener(this));

        }

        mBadgeView = (BadgeView) mActionBar.getTabAt(1).getCustomView()
                .findViewById(R.id.tab_badge);

        mUDPListener.connectUDPSocket();
        mUDPListener.notifyOnline();

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int i = tab.getPosition();
        mViewPager.setCurrentItem(i);
        switch (i) {

            case 1:
                MessageFragment v2 = (MessageFragment) mPagerAdapter.getItem(1);
                v2.refreshAdapter();
                break;

            default:
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                Toast.makeText(this, getString(R.string.maintab_toast_logout), Toast.LENGTH_SHORT)
                        .show();
                ExitTime = System.currentTimeMillis();
            }
            else {
                ActivitiesManager.finishAllActivities();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IPMSGConst.IPMSG_BR_ENTRY:
                case IPMSGConst.IPMSG_BR_EXIT:
                    FriendsFragment v1 = (FriendsFragment) (mPagerAdapter).getItem(0);
                    v1.initMaptoList();
                    v1.refreshAdapter();
                    break;

                case IPMSGConst.IPMSG_RECVMSG:
                case IPMSGConst.IPMSG_READMSG:
                    if (mBadgeView == null) {
                        mBadgeView = (BadgeView) mActionBar.getTabAt(1).getCustomView()
                                .findViewById(R.id.tab_badge);
                    }
                    int num = mUDPListener.getUnReadPeopleSize();
                    MessageFragment v2 = (MessageFragment) (mPagerAdapter).getItem(1);
                    v2.refreshAdapter();
                    switch (num) {
                        case 0:
                            mBadgeView.setText("0");
                            mBadgeView.setVisibility(View.GONE);
                            break;

                        default:
                            mBadgeView.setText(num + "");
                            mBadgeView.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void processMessage(Message pMsg) {
        handler.sendEmptyMessage(pMsg.what);

    }

}
