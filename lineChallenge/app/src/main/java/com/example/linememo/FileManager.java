package com.example.linememo;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FileManager
{
    public Item dateAndContentsRead(File folder)
    {
        try {
            FileInputStream contentsFileStream = new FileInputStream(new File(folder,"contentsAndDate.txt"));//데이터 저장 파일
            BufferedReader contentsReader = new BufferedReader(new InputStreamReader(contentsFileStream));
            String title = folder.getName();
            String date = contentsReader.readLine();
            StringBuilder str = new StringBuilder();
            String line ="";
            while ((line = contentsReader.readLine())!=null)
            {
                str.append(line+"\n");
            }
            contentsReader.close();
            contentsFileStream.close();
            return new Item(title,str.toString(),date);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFileNameFromUri(Uri uri)
    {
        try
        {
            return(new File(uri.getPath()).getName());
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }

    //폴더내 파일 및 폴더 자체를 삭제하는 메소드
    public void deleteAllFile(File filesDir)
    {
        File[] folders = filesDir.listFiles();
        for(File memo : folders)
        {
            if(memo.isDirectory()) { deleteAllFile(memo);}
            else{memo.delete();}
        }
        filesDir.delete();
    }

    public ArrayList<Uri> imageRead(File folder)
    {
        ArrayList<Uri> image = new ArrayList<>();
        try {
            FileInputStream imageFileStream = new FileInputStream((new File(folder,"image.txt")));//이미지의 URI가 들어있는 파일
            BufferedReader imageReader = new BufferedReader(new InputStreamReader(imageFileStream));
            String uri ="";
            while ((uri = imageReader.readLine())!=null)
            {
                image.add(Uri.parse(uri));
            }
            imageFileStream.close();
            imageReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void CleanUpImageFIle(ArrayList<Uri> deletedUri)
    {
        for(int i=0;i<deletedUri.size();i++)
        {
            File deleteFile = new File(String.valueOf(deletedUri.get(i)));
            deleteFile.delete();
        }
    }

    public Uri cameraImageSave(Bitmap bitmap,File cachePath)
    {
        int fileNumber = 0;
        String fileName = "camera"+fileNumber+".jpg";
        while((new File(cachePath,fileName).exists()))
        {
            fileNumber++;
            fileName = "camera"+fileNumber+".jpg";
        }
        File tempFile = new File(cachePath,fileName);
        try
        {
            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            return Uri.parse(tempFile.getPath());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cacheRemove(File cacheDir)
    {
        File[] files = cacheDir.listFiles();
        for(File cameraImage : files)
        {
            cameraImage.delete();
        }
    }

    public Uri importExternalImage(File cacheDir, URL url)
    {
        UrlLoadTask task = new UrlLoadTask(cacheDir);
        Bitmap result=null;
        Uri resultUri = null;
        try {
            result = task.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(result !=null)
        {
            try {
                int fileNumber = 0;
                String fileName = "externalUrl" + fileNumber + ".jpg";
                while ((new File(cacheDir, fileName).exists())) {
                    fileNumber++;
                    fileName = "externalUrl" + fileNumber + ".jpg";
                }
                File tempFile = new File(cacheDir, fileName);
                tempFile.createNewFile();
                FileOutputStream out = new FileOutputStream(tempFile);
                result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
                resultUri = Uri.parse(tempFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultUri;
    }
}
