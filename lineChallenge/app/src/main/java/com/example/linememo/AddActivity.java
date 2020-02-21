package com.example.linememo;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements View.OnClickListener
{

    public static final int MODE_GALLERY = 2000;
    public static final int MODE_CAMERA = 4000;
    private ViewPager vp =null;
    private ImageMemoPageAdapter adapter = null;
    private ArrayList<Uri> imageUri =null;
    private ArrayList<Uri> savedImageUri =null;
    private ArrayList<Uri> deletedImageUri =null;
    private boolean MODE_TO_ADD = true;
    private FileManager fileManager = new FileManager();
    private int position = 0;

    //false인 경우 이미지 추가 버튼이 존재, true인 경우 사진을 불러올 수 있는 버튼들 존재
    boolean buttonActivation = false;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        deletedImageUri = new ArrayList<>();
        savedImageUri = new ArrayList<>();
        adapter = new ImageMemoPageAdapter(this);
        Intent titleIntent = getIntent();
        checkMode(titleIntent);
        Button backButton = findViewById(R.id.backButton);
        Button imageButton = findViewById(R.id.imageAddButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button imageDeleteButton = findViewById(R.id.imageDeleteButton);
        vp = findViewById(R.id.viewPager);
        vp.setAdapter(adapter);
        backButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        imageDeleteButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            //뒤로가기 버튼
            case R.id.backButton:
                if(!buttonActivation)
                {
                    finish();
                }
                else
                {
                    Button imageAddButton = findViewById(R.id.imageAddButton);
                    buttonChanger(imageAddButton,buttonActivation);
                    buttonActivation = false;
                }
                break;
                //이미지 제거 버튼으로 이미지 제거
            case R.id.imageDeleteButton:
                //팝업창!
                if(imageUri!=null && imageUri.size()!=0)
                {
                    AlertDialog.Builder checkWindow = new AlertDialog.Builder(this);
                    checkWindow.setTitle("이미지 제거 확인");
                    checkWindow.setMessage("현재 보이는 이미지를 제거하시겠습니까?");
                    checkWindow.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imageDelete(vp.getCurrentItem());
                            Toast.makeText(getApplicationContext(),"이미지를 제거하였습니다.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    checkWindow.setNegativeButton("아니요",null);
                    checkWindow.create().show();
                }
                else
                {
                    Toast.makeText(this,"제거할 이미지가 없습니다.",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imageAddButton: //이미지 추가 버튼
                buttonChanger(v,buttonActivation);
                buttonActivation = true;
                break;
            case R.id.URLButton: //URL 검색 버튼
                EditText writingUrl = findViewById(R.id.writingURL);
                String externalURL = writingUrl.getText().toString();
                try {
                    URL url = new URL(externalURL);
                    Uri externalUriImage = fileManager.importExternalImage( getCacheDir(),url);
                    if(externalUriImage!=null)
                    {
                        createImageUri();
                        if(MODE_TO_ADD)
                        {
                            imageUri.add(externalUriImage);
                        }
                        else
                        {
                            savedImageUri.add(externalUriImage);
                        }
                        insertImageToAdapter();
                    }
                    else
                    {
                        Toast.makeText(this,"URL 경로가 이미지 파일인지 확인해주세요.",Toast.LENGTH_SHORT).show();
                    }
                } catch (MalformedURLException e) {
                    Toast.makeText(this,"올바른 URL 인지 확인해주세요",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.saveButton:
                EditText text = (EditText)findViewById(R.id.wrtingText);
                EditText titleText = (EditText)findViewById(R.id.title);
                String title = titleText.getText().toString();//제목
                String contents = text.getText().toString();//내용

                if(title.equals(""))  //타이틀 입력은 필수
                { Toast.makeText(this,"메모의 제목을 입력해주세요!",Toast.LENGTH_SHORT).show(); }
                else //타이틀이 입력 된 경우 저장
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREA); //메모가 저장된 날짜를 알려주는 포맷
                    Date currentTime = new Date();//날짜
                    Item saveItem;
                    fileSave(title, currentTime ,contents); //파일 저장
                    if(imageUri==null) { saveItem = new Item(title,contents,format.format(currentTime)); }
                    else if(imageUri.size()!=0) { saveItem = new Item(imageUri.get(0),title,contents,format.format(currentTime)); }//이미지가 없으면
                    else if(savedImageUri.size()!=0){ saveItem = new Item(savedImageUri.get(0),title,contents,format.format(currentTime)); }
                    else {saveItem = new Item(title,contents,format.format(currentTime));}//이미지가 있으면
                    Intent intent = new Intent();
                    intent.putExtra("saveItem",saveItem);
                    if(!MODE_TO_ADD) intent.putExtra("position",position);

                    setResult(RESULT_OK,intent);
                    finish();
                }
                break;
            case R.id.galleryButton:
                goToAlbum();
                break;
            case R.id.cameraButton:
                goToCamera();
                break;
        }

    }

    // 이미지 추가 버튼 활성화인 경우, 갤러리, 카메라, URL 검색 버튼 활성화 후 이미지 추가 버튼 제거
    // 이미지 추가 버튼 비활성화인 경우, 갤러리, 카메라, URL 검색 버튼 비활성화 후 이미지 추가 버튼 활성화
    public void buttonChanger(View v,boolean buttonActivation)
    {
        Button galleryButton = (Button)findViewById(R.id.galleryButton);
        Button URLButton = (Button)findViewById(R.id.URLButton);
        Button cameraButton = (Button)findViewById(R.id.cameraButton);
        Button imageDeleteButton = (Button)findViewById(R.id.imageDeleteButton);
        if(!buttonActivation)
        {
            //갤러리 불러오기 버튼 활성화
            galleryButton.setVisibility(View.VISIBLE);
            galleryButton.setOnClickListener(this);

            //카메라 불러오기 버튼 활성화
            cameraButton.setVisibility(View.VISIBLE);
            cameraButton.setOnClickListener(this);

            //URL 검색 버튼 활성화
            LinearLayout URLLayout = (LinearLayout)findViewById(R.id.URLLayout);
            URLLayout.setVisibility(View.VISIBLE);
            URLButton.setOnClickListener(this);

            //이미지 추가 버튼 제거
            v.setVisibility(View.GONE);
            imageDeleteButton.setVisibility(View.GONE);
        }
        else
        {
            //갤러리 불러오기 버튼 비활성화
            galleryButton.setVisibility(View.GONE);

            //카메라 불러오기 버튼 비활성화
            cameraButton.setVisibility(View.GONE);

            //URL 검색 버튼 비활성화
            LinearLayout URLLayout = (LinearLayout)findViewById(R.id.URLLayout);
            URLLayout.setVisibility(View.GONE);

            //이미지 추가 버튼 추가
            v.setVisibility(View.VISIBLE);
            imageDeleteButton.setVisibility(View.VISIBLE);
        }
    }

    //내부저장소에 저장
    public void fileSave (String fileName,Date date, String contents)
    {
        try {
            File folder = new File(getFilesDir().getPath()+"/"+fileName);
            if(!folder.exists()) folder.mkdirs();
            FileOutputStream fos = new FileOutputStream(new File(folder,"contentsAndDate.txt"));
            PrintWriter writer = new PrintWriter(fos);
            writer.println(date);
            writer.print(contents);
            writer.close();
            fileManager.CleanUpImageFIle(deletedImageUri);
            fileSaveToImage(fileName);
            Toast.makeText(this,"파일이 저장되었습니다.",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,"파일 저장에 실패하였습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    public void fileSaveToImage(String uriFileName)
    {
        String path = getFilesDir().getPath()+"/"+uriFileName;
        if(imageUri == null) return;
        HashSet<Uri> arrCheck = new HashSet<>(imageUri);
        imageUri = new ArrayList<>(arrCheck);//중복제거
        Bitmap image;
        FileOutputStream fos = null;
        PrintWriter writer=null;
        try
        {
            fos = new FileOutputStream(new File(path,"image.txt"),true);
            writer = new PrintWriter(fos, true);
            if(MODE_TO_ADD)
            {
                for(int i=0;i<imageUri.size();i++)
                {
                    String imageName = fileManager.getFileNameFromUri(imageUri.get(i));
                    imagePush(uriFileName,imageName,imageUri.get(i));
                    Uri saveUri = imagePush(uriFileName,imageName,imageUri.get(i));
                    if(saveUri !=null) imageUri.set(i,saveUri); //카메라의 경우 이미 확장자가 fileName에 존재
                    if(writer!=null)
                    {
                        writer.println(imageUri.get(i).toString());
                    }
                }
            }
            else
            {
                if(deletedImageUri.size()!=0)
                {
                    new File(path,"image.txt").delete();//다시 쓰기
                    fos = new FileOutputStream(new File(path,"image.txt"),true);
                    writer = new PrintWriter(fos, true);
                    for(int i=0;i<imageUri.size();i++)
                    {
                        writer.println(imageUri.get(i).toString());
                    }
                }
                if(savedImageUri!=null)
                {
                    for(int i=0;i<savedImageUri.size();i++)
                    {
                        String imageName = fileManager.getFileNameFromUri(savedImageUri.get(i));
                        Uri saveUri = imagePush(uriFileName,imageName,savedImageUri.get(i));
                        if(saveUri !=null) savedImageUri.set(i,saveUri); //카메라의 경우 이미 확장자가 fileName에 존재
                        if(writer!=null)
                        {
                            writer.append(savedImageUri.get(i).toString()+"\n");
                            writer.flush();
                        }
                    }
                }
                else if(imageUri.size()==0)
                {
                    new File(path,"image.txt").delete();
                }

            }
            fos.close();
            writer.close();
            fileManager.cacheRemove(getCacheDir());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //갤러리 사진 불러오기
    public void goToAlbum()
    {
        Intent AlbumIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        AlbumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        AlbumIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(AlbumIntent, "어플리케이션 선택"), MODE_GALLERY);
    }

    //카메라 사진 찍기
    public void goToCamera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, MODE_CAMERA);
    }

    public void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        if(requestCode == MODE_GALLERY && resultCode==RESULT_OK)
        {
            if(MODE_TO_ADD)
            {
                if(imageUri==null) imageUri = createImageList(data); //처음
                else { imageUri.addAll(createImageList(data)); }//두번째부터

            }
            else
            {
                savedImageUri.addAll(createImageList(data));
                HashSet<Uri> arrCheck = new HashSet<>(savedImageUri);
                savedImageUri = new ArrayList<>(arrCheck);//중복제거
            }
            insertImageToAdapter();
        }
        else if(requestCode ==MODE_CAMERA && resultCode==RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Uri uri = fileManager.cameraImageSave(bitmap,getCacheDir());
            if(uri !=null)
            {
                if(MODE_TO_ADD)
                {
                    createImageUri();
                    imageUri.add(uri);
                }
                else
                {
                    savedImageUri.add(uri);
                }
                insertImageToAdapter();
            }
            else
            {
                Toast.makeText(this,"사진 저장에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkMode (Intent getIntent)
    {
        String titleToChange = getIntent.getStringExtra("title");
        position = getIntent.getIntExtra("position",-1);
        if(titleToChange!=null)
        {
            EditText titleText = (EditText)findViewById(R.id.title);
            EditText contentsText = (EditText)findViewById(R.id.wrtingText);
            titleText.setText(titleToChange);
            titleText.setEnabled(false);
            File loadFile = new File(getFilesDir(),titleToChange);
            //데이터 저장 파일
            Item loadItem = fileManager.dateAndContentsRead(loadFile);
            contentsText.setText(loadItem.getText());
            ArrayList<Uri> images = fileManager.imageRead(loadFile);
            createImageUri();
            if(images.size()!=0) //이미지가 저장되어 있으면
            {
                imageUri.addAll(images);
                HashSet<Uri> arrCheck = new HashSet<>(imageUri);
                imageUri = new ArrayList<>(arrCheck);//중복제거
                insertImageToAdapter();
            }
            MODE_TO_ADD = false;
        }
    }

    public void insertImageToAdapter()
    {
        if(adapter!=null)
        {
            ArrayList<Uri> imageUris = new ArrayList<>();
            imageUris.addAll(imageUri);
            imageUris.addAll(savedImageUri);
            adapter.setImageUris(imageUris.toArray(new Uri[imageUris.size()]));
            adapter.notifyDataSetChanged();
        }
    }

    //갤러리에서 가져온 데이터 가공
    public ArrayList<Uri> createImageList(Intent data)
    {
        Uri uri = data.getData();
        ClipData clipData = data.getClipData();
        ArrayList<Uri> temp =null;
        if(temp==null) temp = new ArrayList<>();
        if(clipData !=null)
        {
            for(int i=0;i<clipData.getItemCount();i++)
            {
                Uri multiUri = clipData.getItemAt(i).getUri();
                temp.add(multiUri);
            }
        }
        else if(uri !=null)
        {
            temp.add(uri);
        }
        return temp;
    }

    //이미지 제거 버튼시 발생하는 메소드
    public void imageDelete(int position)
    {
        Uri uri = imageUri.get(position);
        if(savedImageUri==null)//하나도 불러오지 않음
        {
            deletedImageUri.add(uri);
        }
        else if(savedImageUri.contains(uri))//불러 왔음
        {
            savedImageUri.remove(savedImageUri.indexOf(uri));//saved에 있으면 아직 파일로 저장 전이기 때문에 메모리에서만 제거
        }
        else//불러 왔으나 불러온 파일을 지우는 것이 아님
        {
            deletedImageUri.add(uri); //제거할 파일이 존재하게 됨
        }
        imageUri.remove(position);
        if(imageUri.size()==0)//아무것도 남지 않으면 마지막 이미지가 안지워지는 문제...
        {
            adapter = new ImageMemoPageAdapter(this);
            vp.setAdapter(adapter);
        }
        insertImageToAdapter();

    }

    //이미지 파일 로컬 영역에 저장
    public Uri imagePush(String folderName, String imageName,Uri streamUri) {
        String path = getFilesDir().getPath()+"/"+folderName;
        Bitmap image;
        try{
            InputStream imageStream = getApplicationContext().getContentResolver().openInputStream(streamUri);
            image = BitmapFactory.decodeStream(imageStream);
            imageStream.close();
            FileOutputStream outImage = new FileOutputStream(path+"/"+imageName+".jpg");
            image.compress(Bitmap.CompressFormat.JPEG,100, outImage);
            outImage.close();
            return Uri.parse(path+"/"+imageName+".jpg");
        }catch (IOException e)
        {
            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(streamUri.toString())));
                FileOutputStream outImage = new FileOutputStream(path+"/"+imageName);
                image.compress(Bitmap.CompressFormat.JPEG,100, outImage);
                outImage.close();
                return Uri.parse(path+"/"+imageName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    //imageUri 리스트가 없는 경우 나는 null 방지 및 있는 경우 생성 방지를 고민하지 않기 위한 처리
    public void createImageUri()
    {
        if(imageUri==null) imageUri = new ArrayList<>();
    }

}
