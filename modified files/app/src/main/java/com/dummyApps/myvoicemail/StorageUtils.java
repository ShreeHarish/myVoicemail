package com.dummyApps.myvoicemail;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class StorageUtils{

    public static String createDirectory(Context context, String parentName, String directoryName){

        File folder = new File(context.getExternalFilesDir(parentName), directoryName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getAbsolutePath();
    }

    public static String GetFilePath(Context context, String folder, String fileName){

        String path = folder + File.separator + fileName;

        return path;
    }

    public static File GetFile(String filePath){

        File f = new File(filePath);

        return f;
    }
}