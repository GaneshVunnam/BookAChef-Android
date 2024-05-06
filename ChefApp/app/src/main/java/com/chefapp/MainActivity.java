package com.chefapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.Toast;

import com.chefapp.home.ChefOrdersFragment;
import com.chefapp.home.OrdersFragment;
import com.chefapp.home.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    public static final int NAV_ORDERS = R.id.nav_orders;
    public static final int NAV_PROFILE = R.id.nav_profile;
    public static final int NAV_CHEFBOOK = R.id.chef_orders;

    private SparseArray<Fragment> fragmentMap = new SparseArray<>();
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the fragment map

        fragmentMap.put(NAV_ORDERS, new OrdersFragment());
        fragmentMap.put(NAV_CHEFBOOK, new ChefOrdersFragment());
        fragmentMap.put(NAV_PROFILE, new ProfileFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new OrdersFragment()).commit();
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = fragmentMap.get(item.getItemId());

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressTime < 2000) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            lastBackPressTime = currentTime;
        }
    }
}
