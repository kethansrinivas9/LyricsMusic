package com.kitspl.kh2139.lyricsmusic;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    MediaPlayer mediaPlayer;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            requestPermissions();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted now fetch the data from the storage
                    lv = (ListView) findViewById(R.id.playList);
                    final ArrayList<String> mySongs = findAllSongs();
                    final ArrayList<File> mySongsOnDevice = findSongsOnDevice(Environment.getExternalStorageDirectory());
                    int songsCount = mySongs.size()+mySongsOnDevice.size();
                    final ArrayList<String> songNames = new ArrayList<>(); /* = new String[songsCount];*/
                    for (int i = 0; i < mySongs.size(); i++) {
                        songNames.add(mySongs.get(i).replace(".mp3",""));
                    }
                    
                    for (int i = 0; i < mySongsOnDevice.size(); i++) {
                        songNames.add(mySongsOnDevice.get(i).getName().replace(".mp3", ""));
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.song_layout, R.id.songName, songNames);
                    lv.setAdapter(arrayAdapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            startActivity(new Intent(getApplicationContext(), MusicPlayer.class).putExtra("pos", position).putExtra("songlist", mySongs).putExtra("songNames",songNames));
                        }
                    });

                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public ArrayList<File> findSongsOnDevice(File storageDirectory){
        ArrayList<File> fileList = new ArrayList<>();
        File[] files = storageDirectory.listFiles();
        System.out.print("file lenght:");
        System.out.print(files.length);
        for(File singleFile: files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                fileList.addAll(findSongsOnDevice(singleFile));
            }
            else{
                if(singleFile.getName().endsWith(".mp3"))
                    fileList.add(singleFile);
            }
        }
        return fileList;
    }

    public ArrayList<String> findAllSongs(){
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        ArrayList<String> songList = new ArrayList<>();
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    songList.add(data);
                }
            }
        }
        cur.close();
        return songList;
    }
}
