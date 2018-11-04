package com.example.android.waitlist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.waitlist.data.WaitlistContract;
import com.example.android.waitlist.data.WaitlistDbHelper;


public class MainActivity extends AppCompatActivity {

    private GuestListAdapter mAdapter;
    private SQLiteDatabase mDb;

    EditText mNewGuestNameEditText;
    EditText mNewPartySizeEditText;

    String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView waitlistRecyclerView;

        // Set local attributes to corresponding views
        waitlistRecyclerView = (RecyclerView) this.findViewById(R.id.all_guests_list_view);

        mNewGuestNameEditText = (EditText) findViewById(R.id.person_name_edit_text);
        mNewPartySizeEditText = (EditText) findViewById(R.id.party_count_edit_text);

        // Set layout for the RecyclerView, because it's a list we are using the linear layout
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Create a DB helper (this will create the DB if run for the first time)
        WaitlistDbHelper dbHelper = new WaitlistDbHelper(this);

        // Keep a reference to the mDb until paused or killed. Get a writable database
        // because you will be adding restaurant customers
        mDb = dbHelper.getWritableDatabase();

        // Get all guest info from the database and save in a cursor
        Cursor cursor = getAllGuests();

        // Create an adapter for that cursor to display the data
        mAdapter = new GuestListAdapter(this, cursor);

        // Link the adapter to the RecyclerView
        waitlistRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long)viewHolder.itemView.getTag();
                removeGuest(id);
                mAdapter.swapCursor(getAllGuests());
            }
        }).attachToRecyclerView(waitlistRecyclerView);

    }

    /**
     * This method is called when user clicks on the Add to waitlist button
     *
     * @param view The calling view (button)
     */
    public void addToWaitlist(View view) {

        if (mNewGuestNameEditText.getText().toString().equals("") || mNewPartySizeEditText.getText().toString().equals("")){
            return;
        }
        String name = mNewGuestNameEditText.getText().toString();
        int partySize = 1;
        try {
            partySize = Integer.parseInt(mNewPartySizeEditText.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long id = addGuest(name, partySize);
        Log.d(TAG,"ID of the added guest : " + id);
        mAdapter.swapCursor(getAllGuests());
        mNewGuestNameEditText.getText().clear();
        mNewPartySizeEditText.getText().clear();
        mNewPartySizeEditText.clearFocus();
        mNewGuestNameEditText.clearFocus();

    }



    /**
     * Query the mDb and get all guests from the waitlist table
     *
     * @return Cursor containing the list of guests
     */
    private Cursor getAllGuests() {
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_TIMESTAMP
        );
    }

    public long addGuest(String guestName, int partySize) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WaitlistContract.WaitlistEntry.COLUMN_GUEST_NAME, guestName);
        contentValues.put(WaitlistContract.WaitlistEntry.COLUMN_PARTY_SIZE, partySize);
        return mDb.insert(WaitlistContract.WaitlistEntry.TABLE_NAME, null, contentValues);
    }

    public void removeGuest(long id){
        int k = mDb.delete(WaitlistContract.WaitlistEntry.TABLE_NAME, WaitlistContract.WaitlistEntry._ID+"="+id,null);
        if(k>0){
            Toast.makeText(this,"Guest Removed",Toast.LENGTH_SHORT).show();
        }
    }



}