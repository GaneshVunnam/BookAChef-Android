package com.bookachef.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookachef.R;
import com.bookachef.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChefOrderAdapter extends RecyclerView.Adapter<ChefOrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context context;
    private OrderUpdateListener orderUpdateListener;
    private Activity activity;

    public ChefOrderAdapter(List<Order> orders, Context context, OrderUpdateListener orderUpdateListener,Activity activity) {
        this.orders = orders;
        this.context = context;
        this.orderUpdateListener = orderUpdateListener;
        this.activity = activity;
    }

    public void updateChefOrderStatus() {
    }

    public void updateChefOrderStatus(String orderId, String newStatus) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Construct the path to the specific order document in your Firestore database
        String ordersCollection = "chefbookings"; // Replace "bookings" with the name of your orders collection
        String orderDocumentPath = ordersCollection + "/" + orderId;

        // Update the status field of the order document
        db.document(orderDocumentPath).update("status", newStatus).addOnSuccessListener(aVoid -> {
            // Update successful
            // Perform any additional actions if needed
            // For example, notify the user that the order status has been updated
            Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show();

            // Notify the fragment that the order status has been updated
            orderUpdateListener.onOrderStatusUpdated();

        }).addOnFailureListener(e -> {
            // Handle any errors that occurred during the update
            // For example, notify the user that the update failed
            Toast.makeText(context, "Failed to update order status", Toast.LENGTH_SHORT).show();
        });

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
//            if ("waitingpayment".equals(order.getStatus())) {
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
            int statusColor = context.getResources().getColor("Accepted".equals(order.getStatus()) ? R.color.green : R.color.black);

            statusTextView.setText(order.getStatus());
            statusTextView.setTextColor(statusColor);
//"Status: " +
            // Check if the order status is "waitingpayment"
            if ("waitingpayment".equals(order.getStatus())) {
                // Show the Pay Now button
                payNowButton.setVisibility(View.VISIBLE);
                // Set click listener for Pay Now button
                payNowButton.setOnClickListener(v -> {
                    initiateRazorpayPayment(order.getTotalPrice(), order.getDocumentId());
                    SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("orderId", order.getDocumentId());
                    editor.apply();
                });
            } else {
                // Hide the Pay Now button if the status is not "waitingpayment"
                payNowButton.setVisibility(View.GONE);
            }
        }

        private void initiateRazorpayPayment(double totalPrice, String orderId) {
            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_L1stWFZEA9XXVL"); // Replace with your Razorpay key ID

            try {
                JSONObject options = new JSONObject();
                options.put("name", "BookAChef");
                options.put("description", "Payment");
                options.put("currency", "INR");
                options.put("amount", (int) (totalPrice * 100)); // Convert totalPrice to paise
                JSONObject prefill = new JSONObject();
                prefill.put("email", "ganeshvunnam8@gmail.com"); // Replace with user's email
                prefill.put("contact", "9392522044"); // Replace with user's contact number
                options.put("prefill", prefill);

                checkout.open(activity, options);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}