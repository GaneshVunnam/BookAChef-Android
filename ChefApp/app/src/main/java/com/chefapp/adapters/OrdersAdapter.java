package com.chefapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chefapp.R;
import com.chefapp.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context context;
    private OrderUpdateListener orderUpdateListener;

    public OrdersAdapter(List<Order> orders, Context context, OrderUpdateListener orderUpdateListener) {
        this.orders = orders;
        this.context = context;
        this.orderUpdateListener = orderUpdateListener;
    }

    public interface OrderUpdateListener {
        void onOrderStatusUpdated();
    }

    // Method to set the list of orders
    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    // Inner class for the ViewHolder
    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView venueTextView;
        private TextView dateTextView;
        private TextView mealTextView;
        private TextView numberOfGuestsTextView;
        private TextView selectedCuisineTextView;
        private TextView selectedDishTextView;
        private TextView selectedCategoriesTextView;
        private TextView totalPriceTextView;
        private TextView dietaryRestrictionsTextView;
        private TextView statusTextView;
        private Button payNowButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            venueTextView = itemView.findViewById(R.id.venueTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            mealTextView = itemView.findViewById(R.id.mealTextView);
            numberOfGuestsTextView = itemView.findViewById(R.id.numberOfGuestsTextView);
            selectedCuisineTextView = itemView.findViewById(R.id.selectedCuisineTextView);
            selectedDishTextView = itemView.findViewById(R.id.selectedDishTextView);
            selectedCategoriesTextView = itemView.findViewById(R.id.selectedCategoriesTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            dietaryRestrictionsTextView = itemView.findViewById(R.id.dietaryRestrictionsTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            payNowButton = itemView.findViewById(R.id.payNowButton);
        }

//        public void bind(Order order) {
//            venueTextView.setText("Venue: " + order.getVenue());
//            dateTextView.setText("Date: " + order.getDate());
//            mealTextView.setText("Meal: " + order.getMeal());
//            numberOfGuestsTextView.setText("Number of Guests: " + order.getNumOfGuests());
//            selectedCuisineTextView.setText("Selected Cuisine: " + order.getSelectedCuisine());
//            selectedDishTextView.setText("Selected Category: " + order.getSelectedDish());
//            selectedCategoriesTextView.setText("Selected Dishes: " + order.getSelectedCategories());
//            totalPriceTextView.setText("Total Price: ₹" + order.getTotalPrice());
//            dietaryRestrictionsTextView.setText("Dietary Restrictions: " + order.getDietaryRestrictions());
//            statusTextView.setText("Status: " + order.getStatus());
//
//            // Check if the order status is "waitingpayment"
//            if ("Pending".equals(order.getStatus())) {
//                // Show the Pay Now button
//                payNowButton.setVisibility(View.VISIBLE);
//                // Set click listener for Pay Now button
//                payNowButton.setOnClickListener(v -> {
//                    // Show dialog to confirm payment
//                    showPaymentConfirmationDialog(order);
//                });
//            } else {
//                // Hide the Pay Now button if the status is not "waitingpayment"
//                payNowButton.setVisibility(View.GONE);
//            }
//        }

        // ...

        public void bind(Order order) {
            venueTextView.setText("Venue: " + order.getVenue());
            dateTextView.setText("Date: " + order.getDate());
            mealTextView.setText("Meal: " + order.getMeal());
            numberOfGuestsTextView.setText("Number of Guests: " + order.getNumOfGuests());
            selectedCuisineTextView.setText("Selected Cuisine: " + order.getSelectedCuisine());
            selectedDishTextView.setText("Selected Category: " + order.getSelectedDish());
            selectedCategoriesTextView.setText("Selected Dishes: " + order.getSelectedCategories());
            totalPriceTextView.setText("Total Price: ₹" + order.getTotalPrice());
            dietaryRestrictionsTextView.setText("Dietary Restrictions: " + order.getDietaryRestrictions());

            // Set text color based on order status
            if ("Accepted".equals(order.getStatus())) {
                statusTextView.setTextColor(context.getResources().getColor(R.color.green)); // Change R.color.green to your actual color resource
            } else {
                statusTextView.setTextColor(context.getResources().getColor(R.color.black)); // Change R.color.black to your actual color resource
            }
            statusTextView.setText(order.getStatus());
//"Status: " +
            // Check if the order status is "waitingpayment"
            if ("Pending".equals(order.getStatus())) {
                // Show the Pay Now button
                payNowButton.setVisibility(View.VISIBLE);
                // Set click listener for Pay Now button
                payNowButton.setOnClickListener(v -> {
                    // Show dialog to confirm payment
                    showPaymentConfirmationDialog(order);
                });
            } else {
                // Hide the Pay Now button if the status is not "waitingpayment"
                payNowButton.setVisibility(View.GONE);
            }
        }

// ...


        private void showPaymentConfirmationDialog(Order order) {
            // Show dialog to confirm payment
            new AlertDialog.Builder(context)
                    .setTitle("Accept?")
                    .setMessage("Are you sure you want to accept the order?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Payment successful, update status to "accepted"
                        updateOrderStatus(order, "waitingpayment");
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void updateOrderStatus(Order order, String newStatus) {
            // Get the Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get the document ID of the order
            String orderId = order.getDocumentId();

            // Construct the path to the specific order document in your Firestore database
            String ordersCollection = "bookings"; // Replace "bookings" with the name of your orders collection
            String orderDocumentPath = ordersCollection + "/" + orderId;

            // Update the status field of the order document
            db.document(orderDocumentPath)
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        // Update successful
                        // Perform any additional actions if needed
                        // For example, notify the user that the order status has been updated
                        Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show();
                        // Hide the Pay Now button
                        payNowButton.setVisibility(View.GONE);

                        // Notify the fragment that the order status has been updated
                        orderUpdateListener.onOrderStatusUpdated();

                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred during the update
                        // For example, notify the user that the update failed
                        Toast.makeText(context, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
