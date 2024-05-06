package com.bookachef.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookachef.R;
import com.bookachef.models.Dish;

import java.util.List;

// DishAdapter.java
public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> {
    private List<Dish> dishes;

    public DishAdapter(List<Dish> dishes) {
        this.dishes = dishes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_box_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dishTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishTextView = itemView.findViewById(R.id.category);
        }

        public void bind(Dish dish) {
            dishTextView.setText(dish.getName());
            // Set click listener for the dish item
            itemView.setOnClickListener(v -> {
                // Handle click event for dish item
                // For example, display categories for the selected dish
            });
        }
    }
}


