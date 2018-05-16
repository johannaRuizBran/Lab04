package com.example.joha.lab4;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1234;
    private static final int IMAGE_CAPTURE = 12;
    public Boolean resultEdit= false;
    EditText editTextNombre;
    EditText editTextCorreo;
    EditText editTextPassword;
    Bitmap fotoTomadda;
    String uriFoto = "";
    DatabaseReference databaseReference;

    Button buttonGuardar;
    Button buttonFoto;

    boolean foto = false;
    private FirebaseAuth mAuth;
    String nombre, correo, password;
    Uri uriResult;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    public void newUser(String email, String password, Persona res) {
        final Persona per=res;
        Log.d("email",email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(getApplicationContext(), "Exito", Toast.LENGTH_SHORT).show();

                            String id= databaseReference.push().getKey();
                            databaseReference.child(id).setValue(per);
                            //upLoad (uriResult);
                            Toast.makeText(getApplicationContext(), "Usuario creado exitosamente", Toast.LENGTH_LONG).show();

                        } else {
                            System.out.println("Fallo: " + task.getException());
                            Toast.makeText(getApplicationContext(), "Fallo: " + task.getException(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("nombre","dasdasdasdsad");
        editTextNombre = (EditText) findViewById(R.id.name);
        editTextCorreo = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);

        buttonFoto = (Button) findViewById(R.id.photo);
        buttonGuardar = (Button) findViewById(R.id.save);

        mAuth = FirebaseAuth.getInstance();

        databaseReference= FirebaseDatabase.getInstance().getReference("users"); //tabla users
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nombre= editTextNombre.getText().toString();
                password= editTextPassword.getText().toString();
                correo= editTextCorreo.getText().toString();
                if (!nombre.equals("") && !correo.equals("") && !password.equals("")) {
                    if(modificar(correo)){
                        return;
                    }
                    Persona res= new Persona(correo, password, nombre);
                    newUser(correo, password,res);
                } else {
                    Toast.makeText(getApplicationContext(), "Faltan datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Tomar foto");
                Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE) ;
                startActivityForResult ( intent , IMAGE_CAPTURE ) ;
            }
        });
    }


    public Boolean modificar(String email){
        resultEdit= false;
        final String correo= email;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Persona user= postSnapshot.getValue(Persona.class);
                    if(user.getEmail().equals(correo)){
                        databaseReference.child(user.getEmail()).child("name").setValue(nombre);
                        Toast.makeText(getApplicationContext(),"Se ha actualizado exitosamente", Toast.LENGTH_LONG).show();
                        resultEdit= true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return resultEdit;
    }


    public void upLoad (Uri fileUri) {
        FirebaseStorage storageRef = FirebaseStorage . getInstance () ;
        StorageReference storageReference =
                storageRef . getReferenceFromUrl ("gs://lab4-9bc54.appspot.com") ;
        final StorageReference photoReference =
                storageReference . child (" photos ")
                        . child ( fileUri . getLastPathSegment () ) ;
        photoReference . putFile ( fileUri )
                . addOnSuccessListener ( new
                     OnSuccessListener < UploadTask . TaskSnapshot >() {
                         @Override
                         public void onSuccess ( UploadTask . TaskSnapshot
                                                         taskSnapshot ) {

                             Uri downloadUrl = taskSnapshot . getDownloadUrl () ;
                         }
                     })
                . addOnFailureListener ( new OnFailureListener () {
                    @Override
                    public void onFailure ( @NonNull Exception exception ) {
                        Toast . makeText ( getApplicationContext(),
                                exception . toString () ,
                                Toast . LENGTH_SHORT ) . show () ;
                    }
                }) ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_CAPTURE){
            System.out.println("tomar imagen");
            Uri UriResults = data.getData ();
            if ( resultCode == RESULT_OK ) {
                //uriFoto =  UriResults.toString();
                //uriResult= UriResults;
                Toast . makeText (this , "guardado", Toast . LENGTH_LONG ) . show () ;
            }
        }
    }

    public void existingUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new
                        OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {


                                } else {

                                    Toast.makeText(getApplicationContext(),
                                            " Authentication failed .",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

}