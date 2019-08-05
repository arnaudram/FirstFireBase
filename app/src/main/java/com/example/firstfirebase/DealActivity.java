package com.example.firstfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.content.Intent.createChooser;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT=42;

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    ImageView imageView;
    ProgressBar progressBar;
    private TravelDeal deal;

    private static ListActivity caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseUtil.onpenFbReference("traveldeals",caller );
        // instance of the firebase
        // mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        // the database reference
        // mDatabaseReference=mFirebaseDatabase.getReference().child("traveldeals");
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        // child("traveldeals"); is the path we intend to reach


        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        imageView =(ImageView)findViewById(R.id.image);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);


        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTile());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
        showImage(deal.getImageUrl());
        Button btnImage=(Button)findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);

                //startActivityForResult(createChooser(intent,"Insert Picture",  42));
                startActivityForResult(intent.createChooser(intent,"Insert Picture"),PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater()
                .inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditexts(true);
        }
        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "deal saved", Toast.LENGTH_LONG)
                        .show();
                clean();
                backTolist();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted", Toast.LENGTH_SHORT)
                        .show();
                backTolist();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICTURE_RESULT && resultCode==RESULT_OK){
            final Uri imageUri= data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   // String url= taskSnapshot.getUploadSessionUri().toString();
                   // String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getMetadata().getReference().getPath();

                    deal.setImageName(pictureName);
                    ref.getPath();
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            deal.setImageUrl(uri.toString());
                            showImage(uri.toString());
                        }
                    });
                    //String pictureName=taskSnapshot.getStorage().getPath();

                    //showImage(url);
                }
            });


        }
    }

    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");

        // the focus on the title editext
        txtTitle.requestFocus();

    }

    private void saveDeal() {
        // String title = txtTitle.getText().toString();
        deal.setTile(txtTitle.getText().toString());
        // String price= txtPrice.getText().toString();
        deal.setPrice(txtPrice.getText().toString());
        // String description = txtDescription.getText().toString();
        deal.setDescription(txtDescription.getText().toString());
        // TravelDeal deal = new TravelDeal(title,description,price,"");
        // inserting data to the dadabase
        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }


    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please saving the deal before deleting", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName()!=null && deal.getImageName().isEmpty()==false){
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                Log.d("delete image","Image successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image",e.getMessage());

                }
            });
        }
    }

    private void backTolist() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
    private  void enableEditexts(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);

    }
    private void showImage(String url){
        Log.d("url","url is "+" "+url);
        if(url!=null && url.isEmpty()==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            progressBar.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .resize(width,width*2/3)


                     .into(imageView, new Callback() {
                         @Override
                         public void onSuccess() {
                             progressBar.setVisibility(View.GONE);
                         }

                         @Override
                         public void onError(Exception e) {
                               e.getMessage();
                         }
                     });
            Picasso.get().setLoggingEnabled(true);

        }
    }
}
