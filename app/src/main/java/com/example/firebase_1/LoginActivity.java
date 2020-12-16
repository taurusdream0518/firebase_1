package com.example.firebase_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Context context;
    private EditText editTextEmail;
    private Switch switchPass;
    private TextView textViewResult;
    private EditText editTextPassword;
    private Button buttonCancel;
    private Button buttonLogin,buttonLogout,buttonRegister;
    private FirebaseAuth authControl;
    private String emailData;
    private String passwordData;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        setTitle("Login");

        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPassword = (EditText) findViewById(R.id.editText_password);
        switchPass = (Switch) findViewById(R.id.switch_pass);
        textViewResult = (TextView) findViewById(R.id.textView_loginResult);
        textViewResult.setText("");

        switchPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    switchPass.setText("ON");
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    switchPass.setText("OFF");
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        buttonCancel = (Button) findViewById(R.id.button_loginCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEmail.setText("");
                editTextPassword.setText("");
            }
        });


        buttonLogin = (Button) findViewById(R.id.button_loginLogin);
        buttonLogout = (Button) findViewById(R.id.button_loginLogout);
        buttonRegister = (Button) findViewById(R.id.button_loginRegister);

        buttonLogin.setOnClickListener(new MyButton());
        buttonLogout.setOnClickListener(new MyButton());
        buttonRegister.setOnClickListener(new MyButton());

        authControl = FirebaseAuth.getInstance();
        Log.d("login","authControl = "+authControl);



    }

    private class MyButton implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_loginLogin:
                    if(editTextEmail.length()==0 || editTextPassword.length()==0){
                        Toast.makeText(context,"Please input email & password",Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        emailData = editTextEmail.getText().toString();
                        passwordData = editTextPassword.getText().toString();
                        Log.d("login","emilData = "+emailData);
                        Log.d("login","passData = "+passwordData);

                        textViewResult.setText("");
                        currentUser = authControl.getCurrentUser();

                        if(currentUser != null){
                            String name = currentUser.getDisplayName();
                            String email = currentUser.getEmail();
                            String uID = currentUser.getUid();

                            textViewResult.append("user already login :\n");
                            textViewResult.append("user name = "+name+"\n");
                            textViewResult.append("email = "+email+"\n");
                            textViewResult.append("uID = "+uID);
                            Log.d("login","already login ");
                        } else {
                            authControl.signInWithEmailAndPassword(emailData,passwordData).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Log.d("login","Sing in ok");
                                        FirebaseUser user = authControl.getCurrentUser();
                                        updataUI(user);
                                    } else {
                                        Log.d("login","sign in fail");
                                        textViewResult.setText("Sign in fail");
                                    }

                                }
                            });
                        }

                    }

                    break;
                case R.id.button_loginLogout:
                    authControl.signOut();

                    break;
                case R.id.button_loginRegister:
                    if(editTextEmail.length()==0 || editTextPassword.length()==0){
                        Toast.makeText(context,"Please input email & password",Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        emailData = editTextEmail.getText().toString();
                        passwordData = editTextPassword.getText().toString();
                        Log.d("login","emilData = "+emailData);
                        Log.d("login","passData = "+passwordData);

                        authControl.createUserWithEmailAndPassword(emailData,passwordData).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Log.d("login"," ok");
                                    FirebaseUser user = authControl.getCurrentUser();
                                    updataUI(user);
                                } else {
                                    Log.d("login","register fail");
                                    textViewResult.setText("register fail");
                                }
                            }
                        });
                    }


                    break;

            }

        }

    }


    private void updataUI(FirebaseUser user) {

        if(user != null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            String uID = user.getUid();

            textViewResult.append("user already login :\n");
            textViewResult.append("user name = "+name+"\n");
            textViewResult.append("email = "+email+"\n");
            textViewResult.append("uID = "+uID);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authControl.signOut();
    }
}