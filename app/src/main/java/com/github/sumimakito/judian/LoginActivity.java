package com.github.sumimakito.judian;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements TextWatcher {
    private TextInputEditText usernameEdit, passwordEdit;
    private MaterialButton loginButton;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logo = findViewById(R.id.login_logo);
        usernameEdit = findViewById(R.id.login_username);
        passwordEdit = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_login);
        usernameEdit.addTextChangedListener(this);
        passwordEdit.addTextChangedListener(this);
        loginButton.setOnClickListener(v -> {
            final AlertDialog dialog = new ProgressDialog.Builder(this)
                    .setMessage("登录中…")
                    .setCancelable(false)
                    .create();
            dialog.show();
            JudianClient.login(usernameEdit.getText().toString(), passwordEdit.getText().toString(), new JudianClient.ResultCallback() {
                @Override
                public void onResult(String result) {
                    dialog.dismiss();
                    App.username = usernameEdit.getText().toString();
                    startActivity(new Intent(LoginActivity.this, MapActivity.class));
                    LoginActivity.this.finish();
                }

                @Override
                public void onFailed() {
                    Toast.makeText(LoginActivity.this, "登录失败, 请检查账号信息", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        loginButton.setEnabled(usernameEdit.getText().toString().trim().length() > 0 && passwordEdit.getText().length() > 0);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
