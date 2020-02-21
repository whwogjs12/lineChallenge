package com.example.linememo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private ListView mainListView = null;
    ListAdapter adapter;
    ArrayList<Item> memos = new ArrayList<>(); // 리스트에 추가할 메모들
    public static final int MODE_ADD = 500;
    public static final int MODE_AMEND = 1000;
    private FileManager fileManager = new FileManager();

    //false인 경우 제거 모드 비활성화 상태, true인 경우 제거 모드 활성화로 전체제거 버튼과 체크박스 활성화
    boolean deleteModeActivation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = (Button)findViewById(R.id.addButton);
        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        addButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        adapter = new ListAdapter();
        importFile();
        mainListView = (ListView)findViewById(R.id.listView);
        mainListView.setAdapter(adapter);
        mainListView.setOnItemClickListener(this);
    }

//시작시 파일 불러오기
    public void importFile()
    {
        String path = getFilesDir().getPath();
        File directory = new File(path);
        File[] memoLists = directory.listFiles();
        StringBuilder str = new StringBuilder();
        String title;
        String date;
        FileManager fileManager = new FileManager();
        for(File folder : memoLists)
        {
           Item temp = fileManager.dateAndContentsRead(folder);
           if(temp !=null)
           {
               ArrayList<Uri> image =null;
               if((image = fileManager.imageRead(folder)).size()!=0) //이미지가 저장되어 있으면
               {
                   temp.setImage(image.get(0));//첫번째를 보여주기
               }

               memos.add(temp);
           }
        }
        Collections.sort(memos); //정렬
        adapter.setItem(memos);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.addButton :
                Intent intent = new Intent(getApplicationContext(),AddActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,MODE_ADD);
                break;
            case R.id.deleteButton:
            case R.id.mainBackButton:
                buttonChanger(v);
                break;
            case R.id.deleteAllButton:
                AlertDialog.Builder checkDeleteAll = new AlertDialog.Builder(this);
                checkDeleteAll.setTitle("전체 제거");
                checkDeleteAll.setMessage("모든 메모를 제거하시겠습니까?");
                checkDeleteAll.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        memos.clear(); //데이터도 다 지우기
                        deleteAllFile(getFilesDir());
                        adapter.setItem(memos);
                        mainListView.invalidateViews();
                    }
                });
                checkDeleteAll.setNegativeButton("아니요",null);
                checkDeleteAll.create().show();
                break;
        }
    }

    public void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            //추가 버튼을 이용해 결과를 받아오는 경우
            if(requestCode == MODE_ADD)
            {
                Item itemToAdd = (Item) data.getParcelableExtra("saveItem");
                memos.add(itemToAdd);
            }
            //리스트를 선택해 수정하는 경우
            else if (requestCode ==MODE_AMEND)
            {
                Item itemToChange = (Item) data.getParcelableExtra("saveItem");
                int position = data.getIntExtra("position",-1);
                if(position!=-1)
                {
                    memos.set(position,itemToChange);
                    Collections.sort(memos);
                }
                else
                {
                    Toast.makeText(this,"수정에 문제가 발생하였습니다",Toast.LENGTH_SHORT).show();
                }
            }
            adapter.setItem(memos);
        }
        else
        {
            Toast.makeText(this,"저장을 취소하셨습니다.",Toast.LENGTH_SHORT).show();
        }



    }

    //리스트 클릭시 수정 및 제거가 가능하게 하는 메소드
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(deleteModeActivation) //제거 모드시 클릭할 경우 제거
        {
            String title = memos.get(position).getTitle();
            File deleteMemo = new File(getFilesDir(),title);
            deleteAllFile(deleteMemo);
            Toast.makeText(this,title+"가 제거되었습니다.",Toast.LENGTH_SHORT).show();
            memos.remove(position);
            adapter.setItem(memos);
        }
        else // 아닌 경우는 수정으로 들어감
        {
            String changeMemoTitle = memos.get(position).getTitle();//타이틀 가져오기
            Intent intent = new Intent(getApplicationContext(),AddActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("title",changeMemoTitle);
            intent.putExtra("position",position);
            startActivityForResult(intent,MODE_AMEND);
        }
    }

    //전체 제거시 발생하는 메소드
    public void deleteAllFile(File filesDir) //getFilesDir로 받는 경우 return이 File
    {
        fileManager.deleteAllFile(filesDir);
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"모든 메모를 삭제하셨습니다.",Toast.LENGTH_SHORT).show();
    }

    public void buttonChanger(View v)
    {
        Button deleteAllButton = (Button)findViewById(R.id.deleteAllButton);
        Button addButton = (Button)findViewById(R.id.addButton);
        Button mainBackButton = (Button)findViewById(R.id.mainBackButton);
        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        if(!deleteModeActivation) //제거 모드 활성화
        {
            //전체 제거 버튼 보이기
            addButton.setVisibility(View.GONE);
            deleteAllButton.setVisibility(View.VISIBLE);
            mainBackButton.setVisibility(View.VISIBLE);
            mainBackButton.setOnClickListener(this);
            deleteAllButton.setOnClickListener(this);
            deleteModeActivation = true;
            Toast.makeText(this,"리스트를 선택하면 제거됩니다.",Toast.LENGTH_SHORT).show();
        }
        else //제거 모드 비활성화
        {
            deleteButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            deleteAllButton.setVisibility(View.GONE);
            deleteModeActivation = false;
        }
        v.setVisibility(View.GONE);
    }

}
