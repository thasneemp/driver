package com.launcher.mummu.driver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.launcher.mummu.driver.R;
import com.launcher.mummu.driver.storage.CabStorageUtil;

/**
 * Created by muhammed on 3/1/2017.
 */

public class LoginActivityCompact extends Container implements View.OnClickListener {
    FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d("TEST", "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d("TEST", "onAuthStateChanged:signed_out");
            }
            // ...
        }
    };
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_compact);
        setUI();
    }

    private void setUI() {
        mUsernameEditText = (EditText) findViewById(R.id.usernameeditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordeditText);
        mLoginButton = (Button) findViewById(R.id.loginButton);

        mLoginButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        mAuth.removeAuthStateListener(mAuthListener);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                mAuth.signInWithEmailAndPassword(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivityCompact.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivityCompact.this, "Authentication Success",
                                    Toast.LENGTH_SHORT).show();
                            CabStorageUtil.setUsername(LoginActivityCompact.this, CabStorageUtil.USER_NAME, task.getResult().getUser().getEmail());
                            startActivity(new Intent(LoginActivityCompact.this, MainActivity.class));
                            finish();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }
}
