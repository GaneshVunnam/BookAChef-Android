package com.bookachef.adapters;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookachef.R;
import com.bookachef.models.Event;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

// SpecialOccasionsAdapter.java
public class SpecialOccasionsAdapter extends RecyclerView.Adapter<SpecialOccasionsAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public SpecialOccasionsAdapter(Context context, List<Event> eventList, OnItemClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleTextView;
        private TextView dateTextView;
        private TextView timeTextView;
        private TextView venueTextView;
        private ImageView imageView;
        private Button bookNowButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            venueTextView = itemView.findViewById(R.id.venueTextView);
            imageView = itemView.findViewById(R.id.eventImageView); // Initialize ImageView
            bookNowButton = itemView.findViewById(R.id.bookNowButton); // Initialize book now button

            itemView.setOnClickListener(this);
            bookNowButton.setOnClickListener(this);
        }

        public void bind(Event event) {
            titleTextView.setText(event.getTitle());
            dateTextView.setText(event.getDate());
            timeTextView.setText(event.getTime());
            venueTextView.setText(event.getVenue());

            // Load image using Glide with rounded corners
            RequestOptions requestOptions = new RequestOptions()
                    .transforms(new CenterCrop(), new RoundedCorners(20)); // Adjust corner radius as needed
            Glide.with(context)
                    .load(event.getImageUrl())
                    .apply(requestOptions)
                    .into(imageView);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Event event = eventList.get(position);
                listener.onItemClick(event); // Pass selected event to listener
            }
        }
    }
}
