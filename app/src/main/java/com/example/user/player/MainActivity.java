package com.example.user.player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lv;
    String items[];
    private static final String TAG = "myLogs";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, PlayerService.class);

        lv = (ListView) findViewById(R.id.lvPlaylist);

        final ArrayList<File> mySongs = findSongs(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for(int i = 0; i < mySongs.size(); i++){
            items[i] = mySongs.get(i).getName().replace(".mp3","");
        }

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getApplicationContext(),R.layout.song_layout,R.id.textView,items);
        lv.setAdapter(adp);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Log.d(TAG, "Кликнули по списку на позиции " + position);
                startActivity(new Intent(getApplicationContext(),Play_activity.class).putExtra("pos",position).putExtra("songslist",mySongs));

            }
        });
    }

    public ArrayList<File> findSongs(File root){
        ArrayList<File> al = new ArrayList<>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                al.addAll(findSongs(singleFile));
            }
            else {
                if(singleFile.getName().endsWith(".mp3")){
                    al.add(singleFile);
                }
            }
        }
        return al;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService (intent);
        Log.d(TAG, "StopService from MainActivity");
    }

}
