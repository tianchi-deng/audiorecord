package com.android.audiorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordListActivity extends AppCompatActivity {
    private ListView mListView;
    List<File> files = new ArrayList<>();
    RecordListAdapter adapter;
    private static final String TAG = "RecordListActivity";
    private Pcmplayer pcmplayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        mListView = findViewById(R.id.list_record);
        pcmplayer = Pcmplayer.getInstance();
        files = FileUtils.getPcmFiles();
        adapter = new RecordListAdapter(this,files);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = files.get(position);
//                pcmplayer.playInModeStatic(file); //适合提示铃声类型的声音播放
                pcmplayer.playInModeStream(file);
            }
        });
    }
}
