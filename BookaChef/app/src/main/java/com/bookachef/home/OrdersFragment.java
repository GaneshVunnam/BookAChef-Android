package com.bookachef.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookachef.R;
import com.bookachef.adapters.OrdersAdapter;
import com.bookachef.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.razorpay.PaymentResultListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment implements OrdersAdapter.OrderUpdateListener, PaymentResultListener {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        recyclerView = view.findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orders = new ArrayList<>();
        adapter = new OrdersAdapter(orders, requireContext(), this, requireActivity());
        recyclerView.setAdapter(adapter);
        fetchOrdersData();
        return view;
    }

    private void fetchOrdersData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            // Handle error
                            return;
                        }
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String orderId = documentSnapshot.getId();
                            Order order = documentSnapshot.toObject(Order.class);
                            order.setDocumentId(orderId);
                            orders.add(order);
                        }
                        adapter.setOrders(orders);
                    });
        }
    }

    @Override
    public void onOrderStatusUpdated() {
        fetchOrdersData();
    }

@Override
    public void onPaymentSuccess(String orderId) {
        try {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String orderDocumentId = sharedPreferences.getString("orderId", "");
            adapter.updateOrderStatus(orderDocumentId,"Accepted");
            Toast.makeText(requireContext(),
                    "Payment Success",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "An error occurred while updating order status",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(requireContext(),"Payment Failed",Toast.LENGTH_SHORT).show();
    }
}