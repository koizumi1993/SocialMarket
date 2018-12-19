package com.fundamentos.abisu.socialmarket;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddProducto extends Fragment {
    private static final String CARPETA_PRINCIPAL = "imagenesDeMiAPP/";//carpeta principal de la apliacion
    private static final String CARPETA_IMAGEN = "productos";//carpeta donde se guardaran las fotos
    private static final String DIRECTORIO = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta completa
    private static final int RESPUESTA_GALERIA = 10;
    private static final int RESPUESTA_CAMARA = 20;
    private String path;//variable donde se alamacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;

    EditText producto, precio, descripcion;
    Button btnAgregar;
    ImageView fotoProd;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    //private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    ProgressDialog progressDialog;

    public AddProducto() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflView = inflater.inflate(R.layout.fragment_add_producto, container, false);

        producto = (EditText) inflView.findViewById(R.id.nombreProducto);
        precio = (EditText) inflView.findViewById(R.id.precioProducto);
        descripcion = (EditText) inflView.findViewById(R.id.descripcionProducto);
        fotoProd = (ImageView) inflView.findViewById(R.id.foto_producto);
        btnAgregar = (Button) inflView.findViewById(R.id.botonAgregar);

        progressDialog = new ProgressDialog(getContext());

        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("productos");//Sala de chat (nombre

        firebaseStorage = FirebaseStorage.getInstance();

        fotoProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpciones();
            }
        });
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregar();
            }
        });

        return inflView;
    }

    private void agregar() {
        mUser = mAuth.getCurrentUser();
        final String nameProducto = producto.getText().toString().trim();
        final String priceProducto = precio.getText().toString().trim();
        final String descProducto = descripcion.getText().toString().trim();
        Uri url = Uri.parse(path);
        storageReference = firebaseStorage.getReference("productos");
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
                    Producto producto = new Producto(nameProducto,priceProducto,descProducto,url.toString(),mUser.getDisplayName());
                    databaseReference.push().setValue(producto);
                    Toast.makeText(getContext(),"Añadido al inventario: "+nameProducto,Toast.LENGTH_SHORT).show();
                }else{}
            }
        });
    }

    private void mostrarOpciones() {
        final CharSequence[] opciones = {"Camara","Galeria","Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccione una opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Camara")){
                    //llamada al metodo de camara
                    abrirCamara();
                }else{
                    if (opciones[i].equals("Galeria")){
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Selecciona"),RESPUESTA_GALERIA);
                    }else{
                        dialogInterface.dismiss();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESPUESTA_GALERIA:
                Uri miPath = data.getData();
                path=miPath.toString();
                fotoProd.setImageURI(miPath);
                Toast.makeText(getActivity(),path,Toast.LENGTH_SHORT).show();
                break;
            case RESPUESTA_CAMARA:
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String s, Uri uri) {
                                Log.i("Path",""+path);
                            }
                        });
                bitmap = BitmapFactory.decodeFile(path);
                fotoProd.setImageBitmap(bitmap);
                Toast.makeText(getActivity(),path,Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
