package com.chefapp.home;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chefapp.R;
import com.chefapp.adapters.OrdersAdapter;
import com.chefapp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment implements OrdersAdapter.OrderUpdateListener {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orders; // Declare a list of orders

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Initialize the list of orders
        orders = new ArrayList<>();
        // Assuming you have the list of orders and the context available
        // Initialize the adapter with the list of orders and the context
        adapter = new OrdersAdapter(orders, getContext(), this);
        recyclerView.setAdapter(adapter);

        fetchOrdersData();

        return view;
    }

    private void fetchOrdersData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("bookings")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Order> orders = new ArrayList<>(); // Create a list of orders
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Retrieve the document ID
                            String orderId = documentSnapshot.getId();
                            // Create an instance of com.bookachef.models.Order from Firestore data
                            Order order = documentSnapshot.toObject(Order.class);
                            // Set the document ID for the order
                            order.setDocumentId(orderId);
                            orders.add(order); // Add the order to the list
                        }
                        adapter.setOrders(orders); // Set the list of orders in the adapter
                    });
        }
    }

    @Override
    public void onOrderStatusUpdated() {
        fetchOrdersData();
    }
}