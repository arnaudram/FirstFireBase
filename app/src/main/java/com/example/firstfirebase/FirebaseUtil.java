package com.example.firstfirebase;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {

    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtil firebaseUtil;
    public static ArrayList<TravelDeal> mDeals;
    //dealing with authentification
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 123;
    //private static Activity caller;
    private static ListActivity caller;
    public static FirebaseStorage mStorage;
     public static StorageReference mStorageRef;

    public static boolean isAdmin;

    private FirebaseUtil() {
    }

    ;

    public static void onpenFbReference(String ref, final ListActivity callerActivity) {
        if (firebaseUtil == null) {
            FirebaseUtil firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();


            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    }
                    else{
                        String userId=firebaseAuth.getUid();
                        checkAdmin(userId);
                    }

                    Toast.makeText(callerActivity.getBaseContext(), "Welcome back", Toast.LENGTH_LONG)
                            .show();
                }
            };

              connectStorage();
        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthListener);


    }

    public static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    public static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    private  static void checkAdmin(String uid){
        FirebaseUtil.isAdmin=false;
        DatabaseReference ref=mFirebaseDatabase.getReference()
                .child("Administrator").child(uid);
        ChildEventListener Listener= new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 FirebaseUtil.isAdmin=true;
               // Log.d("Admin","you are an administrator");
               caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(Listener);

    }
    public static void connectStorage(){
        mStorage=FirebaseStorage.getInstance();
        mStorageRef=mStorage.getReference().child("deals_pictures");

    }
}