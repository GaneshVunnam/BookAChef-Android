package com.bookachef;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.Toast;

import com.bookachef.home.BookChefFragment;
import com.bookachef.home.ChefOrdersFragment;
import com.bookachef.home.HomeFragment;
import com.bookachef.home.OrdersFragment;
import com.bookachef.home.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.razorpay.PaymentResultListener;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    public static final int NAV_HOME = R.id.nav_home;
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
        fragmentMap.put(NAV_HOME, new HomeFragment());
        fragmentMap.put(NAV_ORDERS, new OrdersFragment());
        fragmentMap.put(NAV_CHEFBOOK, new ChefOrdersFragment());
        fragmentMap.put(NAV_PROFILE, new ProfileFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
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
    public void onPaymentSuccess(String razorpayPaymentID) {

        try {
            // Check if the selected fragment is OrdersFragment or ChefOrdersFragment
            Fragment selectedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (selectedFragment instanceof OrdersFragment) {
                OrdersFragment ordersFragment = (OrdersFragment) selectedFragment;
                ordersFragment.onPaymentSuccess(razorpayPaymentID);
            } else if (selectedFragment instanceof ChefOrdersFragment) {
                ChefOrdersFragment chefOrdersFragment = (ChefOrdersFragment) selectedFragment;
                chefOrdersFragment.onPaymentSuccess(razorpayPaymentID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "An error occurred while updating order status",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        // Handle payment error if needed
        Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_SHORT).show();
    }

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
