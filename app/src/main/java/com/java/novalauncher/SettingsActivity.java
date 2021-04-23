package com.java.novalauncher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {

    ImageView mHomeScreenImage;
    EditText mNumRow, mNumCol;

    int REQUEST_CODE_IMAGE = 1;
    String PREFS_NAME = "NovaPrefs";
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button mHomeScreenButton = findViewById(R.id.homeScreenButton);
        Button mGridSizeButton = findViewById(R.id.gridSizeButton);

        mHomeScreenImage = findViewById(R.id.homeScreenImage);
        mNumRow = findViewById(R.id.numRow);
        mNumCol = findViewById(R.id.numColumn);

        mGridSizeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                saveDate();
            }
        });
        mHomeScreenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*"); // 모든 이미지 타입
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        getData();
    }

    private void getData(){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUriString = sharedPreferences.getString("imageUri", null);
        int numRow = sharedPreferences.getInt("numRow", 7);
        int numColumn = sharedPreferences.getInt("numColumn", 4);

        if(imageUriString != null){
            imageUri = Uri.parse(imageUriString);
            mHomeScreenImage.setImageURI(imageUri);
        }

        mNumRow.setText(String.valueOf(numRow));
        mNumCol.setText(String.valueOf(numColumn));

    }
    private void saveDate(){
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        if(imageUri != null){
            sharedPreferences.putString("imageUri", imageUri.toString());
        }

        sharedPreferences.putInt("numRow", Integer.valueOf(mNumRow.getText().toString()));
        sharedPreferences.putInt("numColumn", Integer.valueOf(mNumCol.getText().toString()));
        sharedPreferences.apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK){
            imageUri = data.getData();
            mHomeScreenImage.setImageURI(imageUri);
            saveDate();
        }
    }
}