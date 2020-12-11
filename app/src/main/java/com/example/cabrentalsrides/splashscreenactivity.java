package com.example.cabrentalsrides;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.cabrentalsrides.Common.Common;
import com.example.cabrentalsrides.Model.RiderModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action;
import rx.functions.Action0;


public class splashscreenactivity extends AppCompatActivity {

    //private final static int LOGIN_REQUEST_CODE = 284;
    private final static int LOGIN_REQUEST_CODE = 283;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    TextInputEditText firstn, lastn, emailId, moBNum;

    Button btn_register;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference riderInfoRef;

    @Override
    protected void onStart() {
        super.onStart();
        delaysplashscreen();
    }

    private void delaysplashscreen() {
        progressBar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() ->

                        firebaseAuth.addAuthStateListener(listener)
        //Toast.makeText(splashscreenactivity.this, "Splash Screen Done!", Toast.LENGTH_SHORT).show()
                        );



    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        init();
    }

    private void init() {
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        riderInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null)
            {
                checkUserFromFirebase();
            }
            else
            {
             showLoginLayout();
            }
        };
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.signphonerider)
                .setGoogleButtonId(R.id.signgooglerider)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserFromFirebase() {
        riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            RiderModel riderModel = dataSnapshot.getValue(RiderModel.class);
                            goToHomeActivity(riderModel);
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(splashscreenactivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this, R.style.DialogTheme);
        View itemView= LayoutInflater.from(this).inflate(R.layout.layout_register, null);

        firstn= itemView.findViewById(R.id.FirstName);
        lastn= itemView.findViewById(R.id.LastName);
        moBNum= itemView.findViewById(R.id.PhoneNumberReg);
        emailId= itemView.findViewById(R.id.emailreg);

        btn_register= itemView.findViewById(R.id.registerbtn);

        //Set Data
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()!=null
                && TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            moBNum.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        if (FirebaseAuth.getInstance().getCurrentUser().getEmail()!=null
                && TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            emailId.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //Set View
        builder.setView(itemView);
        AlertDialog dialog= builder.create();
        dialog.show();

        btn_register.setOnClickListener(view -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(firstn.getText()).toString()))
            {
                Toast.makeText(this, "Please Enter Your First Name", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(Objects.requireNonNull(lastn.getText()).toString()))
            {
                Toast.makeText(this, "Please Enter Your Last Name", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(Objects.requireNonNull(moBNum.getText()).toString()))
            {
                Toast.makeText(this, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(Objects.requireNonNull(emailId.getText()).toString()))
            {
                Toast.makeText(this, "Please Enter Your Email Id", Toast.LENGTH_SHORT).show();
            }
            else
            {
                RiderModel model= new RiderModel();
                model.setFirstName(firstn.getText().toString());
                model.setLastName(lastn.getText().toString());
                model.setPhoneNumber(moBNum.getText().toString());
                model.setEmailID(emailId.getText().toString());


                riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnFailureListener(e ->
                                {
                                    dialog.dismiss();
                                    Toast.makeText(splashscreenactivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            goToHomeActivity(model);
                        });
            }

        });


    }


    private void goToHomeActivity(RiderModel riderModel) {
        Common.currentRider = riderModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }


}

