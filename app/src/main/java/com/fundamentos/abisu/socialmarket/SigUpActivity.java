package com.fundamentos.abisu.socialmarket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class SigUpActivity extends AppCompatActivity {
    private static final String CARPETA_PRINCIPAL = "help_my_fido/";
    private static final String CARPETA_IMAGENES = "perfiles";
    private static final String DIRECTORIO = CARPETA_PRINCIPAL + CARPETA_IMAGENES;
    private String path;//variable donde se alamacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;

    private FirebaseAuth mAuth;
    private EditText inputNombre, inputMail, inputPass;
    private Button btnRegistrar;
    private ImageView perfil;
    private TextView linkFoto;

    private ProgressDialog progressDialog;

    //private Uri imageUri = null;

    private static final int RESPUESTA_CAMARA = 20;
    private static final int RESPUESTA_GALERIA = 10;

    //private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sig_up);
        inputNombre = (EditText) findViewById(R.id.field_nombre);
        inputMail = (EditText) findViewById(R.id.field_email);
        inputPass = (EditText) findViewById(R.id.field_password);
        btnRegistrar = (Button) findViewById(R.id.crearCuenta);
        perfil = (ImageView) findViewById(R.id.fotoPerfil);
        linkFoto = (TextView) findViewById(R.id.linkFoto);

        mAuth = FirebaseAuth.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        //databaseReference = FirebaseDatabase.getInstance().getReference().child("usuarios");

        linkFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpciones();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        //Obtenemos el email y la contraseña desde las cajas de texto
        final String email = inputMail.getText().toString().trim();
        String password = inputPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
            Toast.makeText(this,"No se admiten campos vacios",Toast.LENGTH_SHORT).show();
        }

        progressDialog = new ProgressDialog(this,R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrando......");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SigUpActivity.this, "Se ha registrado el usuario con el email: " + inputMail.getText(), Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Toast.makeText(SigUpActivity.this,"Autenticacion fallida",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void mostrarOpciones() {
        final CharSequence[] opciones = {"Camara","Galeria","Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(SigUpActivity.this);
        builder.setTitle("Seleccione una opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opciones[which].equals("Camara")){
                    abrirCamara();
                }else{
                    if (opciones[which].equals("Galeria")){
                        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Selecciona"),RESPUESTA_GALERIA);
                    }else{
                        dialog.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private void abrirCamara() {
        File miFile = new File(Environment.getExternalStorageDirectory(),DIRECTORIO);

        if (miFile.exists()==false){
            miFile.mkdirs();
        }

        if (miFile.exists()==true){
            Long consecutivo = System.currentTimeMillis()/1000;

            String nombre = consecutivo.toString()+".jpg";

            path = Environment.getExternalStorageDirectory()+File.separator+DIRECTORIO
                    +File.separator+nombre;

            fileImagen=new File(path);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(fileImagen));

            startActivityForResult(intent,RESPUESTA_CAMARA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESPUESTA_GALERIA:
                Uri miPath = data.getData();
                path = miPath.toString();
                perfil.setImageURI(miPath);
                Toast.makeText(this.getBaseContext(),path,Toast.LENGTH_SHORT).show();
                break;
            case RESPUESTA_CAMARA:
                MediaScannerConnection.scanFile(this.getBaseContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String s, Uri uri) {
                                Log.i("Path",""+path);
                            }
                        });
                bitmap = BitmapFactory.decodeFile(path);
                perfil.setImageBitmap(bitmap);
                Toast.makeText(this.getBaseContext(),path,Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateUI(final FirebaseUser currentUser) {
        final String nombre = inputNombre.getText().toString().trim();
        Uri url = Uri.parse(path);
        storageReference = firebaseStorage.getReference("perfiles");
        final StorageReference referenceFoto = storageReference.child(url.getLastPathSegment());
        referenceFoto.putFile(url).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return referenceFoto.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri url = task.getResult();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre)
                            .setPhotoUri(url)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(SigUpActivity.this,"Perfil actualizado",Toast.LENGTH_SHORT).show();
                                        iniciarSesion();
                                    }
                                }
                            });
                }else{}
            }
        });
    }

    private void iniciarSesion() {
        final String correo = inputMail.getText().toString().trim();
        String contraseña = inputPass.getText().toString().trim();

        if (TextUtils.isEmpty(correo)||TextUtils.isEmpty(contraseña)){
            Toast.makeText(SigUpActivity.this,"No se admiten campos vacios",Toast.LENGTH_SHORT).show();
        }else{
            progressDialog = new ProgressDialog(SigUpActivity.this,R.style.AppTheme);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Autenticando......");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(correo,contraseña)
                    .addOnCompleteListener(SigUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                Toast.makeText(SigUpActivity.this,"Bienvenido: "+user.getDisplayName(),Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), Navigation.class);
                                intent.putExtra(Navigation.users,user.getDisplayName());
                                intent.putExtra(Navigation.correos,user.getEmail());
                                intent.putExtra(Navigation.urlsPerfil,user.getPhotoUrl().toString());
                                startActivity(intent);
                            }else{
                                if (task.getException() instanceof FirebaseAuthUserCollisionException){//si se presenta una colision
                                    Toast.makeText(SigUpActivity.this,"Usuario inexistente",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(SigUpActivity.this,"Acceso denegado",Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}
