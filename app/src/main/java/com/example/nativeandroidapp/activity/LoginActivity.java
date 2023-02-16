package com.example.nativeandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nativeandroidapp.R;
import com.example.nativeandroidapp.ultil.PreferencesUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    EditText editemailin, editpassin;
    CardView btnin;
    TextView txtres , txtrecover;
    SignInButton GoogleLogin;
    private FirebaseAuth auth;
    ProgressDialog pd;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Login");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        auth = FirebaseAuth.getInstance();
        editemailin = findViewById(R.id.editemailin);
        editpassin = findViewById(R.id.editpassin);
        btnin = findViewById(R.id.btnin);
        txtres = findViewById(R.id.nothaveac);
        txtrecover = findViewById(R.id.recover);
        GoogleLogin = findViewById(R.id.gglogin);
        GoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        txtres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        btnin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editemailin.getText().toString().trim();
                String pass = editpassin.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editemailin.setError("Invalid Email");
                    editemailin.setFocusable(true);
                }
                else{
                    loginUser(email,pass);
                }
            }
        });
        txtrecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverDialog();
            }
        });
        pd = new ProgressDialog(this);
    }

    private void showRecoverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout( this);
        EditText emailt = new EditText(this);
        emailt.setHint("Email");
        emailt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailt.setMinEms(16);
        linearLayout.addView(emailt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending email....");
        pd.show();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this,"Email sent",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginUser(String email, String pass) {
        pd.setMessage("Logging in...");
        pd.show();
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Toan",task.toString());
                if(task.isSuccessful()) {
                    PreferencesUtils.deleteAll(LoginActivity.this);
                    PreferencesUtils.setString(LoginActivity.this , "email",email);
                    PreferencesUtils.setString(LoginActivity.this , "pass",pass);
                    pd.dismiss();
                    FirebaseUser user = auth.getCurrentUser();
                    startActivity(new Intent(LoginActivity.this , DashboardActivity.class));
                    finish();
                }
                else {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();

                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                String email = user.getEmail();
                                String uid = user.getUid();
                                HashMap<Object, String> hashMap =new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");
                                hashMap.put("phone","");
                                hashMap.put("image","");
                                hashMap.put("cover","");
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);
                            }

                            startActivity(new Intent(LoginActivity.this , DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Login Failed...",Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("Toan",e.getMessage());
            }
        });
    }
}