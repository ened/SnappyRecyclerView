package com.sebroth.snappyrecyclerview.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sebroth.snappyrecyclerview.SnappyGridLayoutManager;
import com.sebroth.snappyrecyclerview.SnappyRecyclerView;


public class SnappyRecyclerViewDemo extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snappy_recycler_view_demo);

        // Step 1: Use the SnappyRecyclerView as a base class
        SnappyRecyclerView recyclerView = (SnappyRecyclerView) findViewById(R.id.recyclerView);

        // Step 2: Configure the layout manager.
        final SnappyGridLayoutManager layoutManager = new SnappyGridLayoutManager(this, 1, SnappyGridLayoutManager.HORIZONTAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        // Step 3: Assign the layout manager to the recycler view.
        recyclerView.setLayoutManager(layoutManager);

        // Step 4: Set the adapter as usual
        recyclerView.setAdapter(new RecyclerView.Adapter<DemoViewHolder>() {
            final int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};

            @Override
            public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new DemoViewHolder(getLayoutInflater().inflate(R.layout.demo_item, parent, false));
            }

            @Override
            public void onBindViewHolder(DemoViewHolder viewHolder, int position) {
                viewHolder.itemView.setBackgroundColor(colors[position % colors.length]);
                viewHolder.indexLabel.setText(Integer.toString(position + 1));
            }

            @Override
            public int getItemCount() {
                return 100;
            }
        });
    }

    static class DemoViewHolder extends RecyclerView.ViewHolder {
        final TextView indexLabel;

        public DemoViewHolder(View itemView) {
            super(itemView);

            indexLabel = (TextView) itemView.findViewById(R.id.indexLabel);
        }
    }
}
