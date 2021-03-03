package com.drugporter.janatapharmacy.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.fragment.CategoryFragment;
import com.drugporter.janatapharmacy.fragment.HomeFragment;
import com.drugporter.janatapharmacy.locationpick.LocationGetActivity;
import com.drugporter.janatapharmacy.locationpick.MapUtility;
import com.drugporter.janatapharmacy.utiles.DatabaseHelper;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.drugporter.janatapharmacy.ui.AddressActivity.changeAddress;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincoded;
import static com.drugporter.janatapharmacy.utiles.SessionManager.user;

public class HomeActivity extends RootActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.txt_location)
    TextView txtLocation;
    public static HomeActivity homeActivity = null;
    public static TextView txtCountcard;


    public static HomeActivity getInstance() {
        return homeActivity;
    }

    SessionManager sessionManager;
    DatabaseHelper helper;
    public static String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(HomeActivity.this);
        sessionManager.setStringData(SessionManager.pincode, "2200");
        sessionManager.setStringData(pincoded, "2200");
        helper = new DatabaseHelper(HomeActivity.this);
        homeActivity = HomeActivity.this;
        txtCountcard = findViewById(R.id.txt_countcard);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        if (sessionManager.getStringData(pincoded).equalsIgnoreCase("")) {
            startActivity(new Intent(HomeActivity.this, LocationGetActivity.class));
        } else {
            setLocation(sessionManager.getStringData(pincoded));
            openFragment(new HomeFragment());
            updateItem();
        }

        Intent i = getIntent();
        if (i != null) {
            Bundle extras = i.getExtras();
            if (extras != null) {
                userId = getIntent().getStringExtra("userid");
                /*newuser = getIntent().getStringExtra("newuser");*/

            }
        }else{

        }
    }

    public void updateItem() {
        Cursor res = helper.getAllData();
        if (res.getCount() == 0) {
            txtCountcard.setText("0");
        } else {
            txtCountcard.setText("" + res.getCount());
        }
    }

    public void setLocation(String location) {

        txtLocation.setText("Deliver to " + location);

    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    bottomNavigation.getMenu().getItem(0).setIcon(R.drawable.ic_home);
                    bottomNavigation.getMenu().getItem(1).setIcon(R.drawable.ic_medicine);
                    bottomNavigation.getMenu().getItem(2).setIcon(R.drawable.ic_prescription);
                    bottomNavigation.getMenu().getItem(3).setIcon(R.drawable.ic_notification);
                    bottomNavigation.getMenu().getItem(4).setIcon(R.drawable.ic_setting);
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            item.setIcon(R.drawable.ic_home_black);
                            openFragment(new HomeFragment());
                            return true;
                        case R.id.navigation_medicine:
                            item.setIcon(R.drawable.ic_medicine_black);
                            /*openFragment(new CategoryFragment());*/
                            startActivity(new Intent(HomeActivity.this, OrderByTyping.class));
                            return true;
                        case R.id.navigation_prescription:
                            item.setIcon(R.drawable.ic_prescription_black);
                            startActivity(new Intent(HomeActivity.this, UploadPrescriptionActivity.class));

                            return true;
                        case R.id.navigation_notifications:
                            item.setIcon(R.drawable.ic_notification_black);
                            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));


                            return true;
                        case R.id.navigation_setting:
                            item.setIcon(R.drawable.ic_setting_black);
                            startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            };

    @OnClick({R.id.rlt_cart, R.id.lvl_actionsearch, R.id.txt_location})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlt_cart:
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                break;
            case R.id.lvl_actionsearch:
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
                break;
            case R.id.txt_location:
                startActivity(new Intent(HomeActivity.this, AddressActivity.class));
                break;
            default:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (changeAddress) {
            changeAddress = false;
            setLocation(sessionManager.getStringData(pincoded));
            openFragment(new HomeFragment());
            updateItem();
        }
    }

    @Override
    public void onBackPressed() {

        FragmentManager fragment = getSupportFragmentManager();


        if (fragment.getBackStackEntryCount() > 1) {
            Fragment fragmentaa = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragmentaa instanceof HomeFragment && fragmentaa.isVisible()) {
                finish();
            } else {
                super.onBackPressed();
            }
        } else {
            //Nothing in the back stack, so exit
            finish();
        }
    }


}