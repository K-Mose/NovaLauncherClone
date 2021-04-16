package com.java.novalauncher;

import android.graphics.drawable.Drawable;

public class AppObject {
    private String name,
                   packageName;
    private Drawable image;

    public AppObject(String packageName, String name, Drawable image){
        this.name = name;
        this.packageName = packageName;
        this.image = image;
    }
    public Drawable getImage(){return image;}
    public String getName(){return name;}
    public String getPackageName(){return packageName;}

}
