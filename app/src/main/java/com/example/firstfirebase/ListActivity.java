package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    ArrayList<TravelDeal> deals;            // An arraylist of deals where we will store each deal retrieve from the database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;     //using to listen to every changes in our database reference
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu,menu);
        MenuItem insertMenu= menu.findItem(R.id.insert_menu);
        if(FirebaseUtil.isAdmin==true){
            insertMenu.setVisible(true);
        }
        else{
            insertMenu.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.insert_menu:
                Intent intent = new Intent(ListActivity.this, DealActivity.class);

                startActivity(intent);
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                Log.d("logout", "user logged out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.onpenFbReference("traveldeals",this);

        //mFirebaseDatabase= FirebaseDatabase.getInstance();
        //  mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        // mDatabaseReference= mFirebaseDatabase.getReference().child("traveldeals");
        // mDatabaseReference = FirebaseUtil.mDatabaseReference;
        // creating our new object listener to listen to changes


        // setting the listener to the database

        // mDatabaseReference.addChildEventListener(mChildEventListener);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DealAdapter dealAdapter= new DealAdapter(this);
        recyclerView.setAdapter(dealAdapter);
        FirebaseUtil.attachListener();
    }


    public void showMenu(){
        invalidateOptionsMenu();
    }
}
