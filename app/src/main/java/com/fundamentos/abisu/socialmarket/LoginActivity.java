package com.fundamentos.abisu.socialmarket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity{
    private FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private EditText email;
    private EditText passWd;
    private Button btnLogin, btnCrearCuenta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        email = (AutoCompleteTextView) findViewById(R.id.email);
        passWd = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.loginEmail);
        btnCrearCuenta = (Button) findViewById(R.id.crearCuenta);

        progressDialog = new ProgressDialog(this);

        btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SigUpActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String correo = email.getText().toString().trim();
                String contraseña = passWd.getText().toString().trim();

                //verificamos que las cajas de texto no esten vacias
                if (TextUtils.isEmpty(correo)||TextUtils.isEmpty(contraseña)){
                    Toast.makeText(LoginActivity.this,"Llene todos los campos por favor",Toast.LENGTH_LONG).show();
                }

                progressDialog.setMessage("Consultando Firebase.........");
                progressDialog.show();

                //Loguear al usuario
                firebaseAuth.signInWithEmailAndPassword(correo,contraseña)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Checking if success
                                if (task.isSuccessful()){
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    Toast.makeText(LoginActivity.this,"Bienvenido: "+user.getDisplayName(),Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), Navigation.class);
                                    intent.putExtra(Navigation.users,user.getDisplayName());
                                    intent.putExtra(Navigation.correos,user.getEmail());
                                    intent.putExtra(Navigation.urlsPerfil,user.getPhotoUrl().toString());
                                    startActivity(intent);
                                    startActivity(intent);
                                }else{
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException){//si se presenta una colision
                                        Toast.makeText(LoginActivity.this,"Usuario existente",Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(LoginActivity.this,"No se pudo registrar al usuario",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }

}

