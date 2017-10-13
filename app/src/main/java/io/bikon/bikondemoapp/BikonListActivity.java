package io.bikon.bikondemoapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import io.bikon.bikondemoapp.ble.BLEService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An activity representing a list of Bikons. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BikonDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BikonListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private List<ScanResult> bikons = new ArrayList<ScanResult>();
    private SimpleItemRecyclerViewAdapter adapter;
    private HashMap<String, BluetoothDevice> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bikon_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View recyclerView = findViewById(R.id.bikon_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.bikon_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        final BLEService beaconService = new BLEService(this);
        beaconService.enableBT();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.e("BIKON", "BLE SCAN STARTED");
                beaconService.startScan(new BikonScanCallback(map));
            }
        });


        Timer timer = new Timer();
        TimerTask updateListTimer = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setmValues(map);
                    }
                });
            }
        };
        timer.schedule(updateListTimer, 0, 1000);

    }

    private Activity getActivity() {
        return this;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(map);
        recyclerView.setAdapter(adapter);
    }

    private class BikonScanCallback extends ScanCallback {
        HashMap<String, BluetoothDevice> map;

        BikonScanCallback(HashMap<String, BluetoothDevice> map) {
            this.map = map;
        }

        @Override
        public void onScanResult(int type, ScanResult result) {
            Log.i("BIKON", "onScanResult" + result.toString());
            this.addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i("BIKON", "onBatchScanResults" + results.toString());
            for (ScanResult result : results) {
                this.addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("BIKON", "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            final BluetoothDevice btDevice = result.getDevice();
            final String address = btDevice.getAddress();
            map.put(address, btDevice);
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<BluetoothDevice> mValues;

        public SimpleItemRecyclerViewAdapter(HashMap<String, BluetoothDevice> items) {
            mValues = new ArrayList<>(items.values());
        }

        public void setmValues(HashMap<String, BluetoothDevice> items) {
            List<BluetoothDevice> devices = new ArrayList<>(items.values());
            mValues.clear();
            for (BluetoothDevice device : devices) {
                mValues.add(device);
            }
            this.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bikon_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).getAddress());
            holder.mContentView.setText(mValues.get(position).getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(BikonDetailFragment.ARG_ITEM_ID, holder.mItem.getAddress());
                        BikonDetailFragment fragment = new BikonDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.bikon_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BikonDetailActivity.class);
                        intent.putExtra(BikonDetailFragment.ARG_ITEM_ID, holder.mItem.getAddress());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public BluetoothDevice mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
