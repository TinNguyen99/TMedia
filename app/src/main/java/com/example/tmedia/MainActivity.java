package com.example.tmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tmedia.adapter.AdapterOfRecyclerView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity implements OnClickedItem, Playing {

    private static final int REQUEST_CODE = 1999;
    private static final int REQUEST_MESS = 2020;

    private NotificationManager notificationManager;

    private Map<String, Runnable> MapThread = new HashMap<String, Runnable>();
    private RecyclerView recyclerView;
    private ArrayList<ItemModel> itemModelArrayList;
    private AdapterOfRecyclerView adapter;
    private MediaPlayer mediaPlayer;
    private Handler mhandler;
    private ProgressBar progressBar;
    private TextView txtTitle, txtArtist;
    private LinearLayout linearLayout;
    private SearchView searchView;


    ImageView imgback, imgplay, imgnext;

    boolean isPlaying = false;
    int position = 0;

    ProcessSS pr;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("PLAY"));
        }

        itemModelArrayList = new ArrayList<>();

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtArtist = (TextView) findViewById(R.id.txtArtist);
        mediaPlayer = new MediaPlayer();

        searchView = (SearchView) findViewById(R.id.searchview);


        imgback = (ImageView) findViewById(R.id.imgBack);
        imgplay = (ImageView) findViewById(R.id.imgPlay);
        imgnext = (ImageView) findViewById(R.id.imgNext);

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        imgplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    onPaused();
                    imgplay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                } else {
                    onPlay();
                    imgplay.setImageResource(R.drawable.ic_pause_black_24dp);
                }
            }
        });


        imgnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext();
            }
        });

        initHandler();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        adapter = new AdapterOfRecyclerView(itemModelArrayList, this, this);

        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            LoadSong();
        } else {
            RequestPermisson();
        }

        DragSong();


    }

    private void RequestPermisson() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu quyền truy cập!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                LoadSong();
            } else {
                Toast.makeText(this, "Ứng dụng chưa được cấp quyền để hoạt động!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void LoadSong(){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        while (cursor.moveToNext()){
            int _id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int _title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int _duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int _artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);


            long id = cursor.getLong(_id);
            long duration = cursor.getLong(_duration);
            String title = cursor.getString(_title);
            String artist = cursor.getString(_artist);

            int tem = (int) duration/1000;
            if (tem >= 110){
                itemModelArrayList.add(new ItemModel(id, title, tem, artist));
            }

        }


    }

    @SuppressLint("ResourceType")
    public void PlaySong(int pos){

        linearLayout = (LinearLayout) findViewById(R.id.playbottom);
        linearLayout.setVisibility(View.VISIBLE);

        isPlaying = !isPlaying;

        if(itemModelArrayList.get(pos).getTitle().length() > 27)
        {
            txtTitle.setTextSize(11f);
            txtTitle.setText(itemModelArrayList.get(pos).getTitle());
        } else {
            txtTitle.setTextSize(15f);
            txtTitle.setText(itemModelArrayList.get(pos).getTitle());
        }
        txtArtist.setText(itemModelArrayList.get(pos).getArtist());

        long id = itemModelArrayList.get(pos).getId();

        //Toast.makeText(this, String.valueOf(id), Toast.LENGTH_SHORT).show();
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        onPlay();
    }


    private void initHandler(){
        mhandler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == REQUEST_MESS){
                    progressBar.setProgress(msg.arg1);
                }
            }
        };
    }

    public void StarProcess(final int pos){

        Thread tr = new Thread(new Runnable() {
            int timeDuration = itemModelArrayList.get(pos).getTimeDuration();
            @Override
            public void run() {
                progressBar.setMax(timeDuration);
                for(int i=0; i<timeDuration;++i){

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message ms = new Message();

                    ms.what = REQUEST_MESS;
                    ms.arg1 = i;
                    mhandler.sendMessage(ms);
                }

            }
        });

        if(isPlaying){
            onPaused();

        } else {
            onPlay();
        }

    }

    public class ProcessSS extends Thread{
        public ProcessSS(int pos) {
            this.pos = pos;
        }

        int pos;
        int timeDuration = itemModelArrayList.get(pos).getTimeDuration();

        @Override
        public void run() {
            progressBar.setMax(timeDuration);
                for(int i=0; i<timeDuration;++i){

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message ms = new Message();
                    ms.what = REQUEST_MESS;
                    ms.arg1 = i;
                    mhandler.sendMessage(ms);
                }
        }
    }

    public class ProcessS implements Runnable{
        public ProcessS(int pos) {
            this.pos = pos;
        }

        int pos;
        int timeDuration = itemModelArrayList.get(pos).getTimeDuration();
        @Override
        public void run() {
            progressBar.setMax(timeDuration);
            for(int i=0; i<timeDuration;++i){

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message ms = new Message();

                ms.what = REQUEST_MESS;
                ms.arg1 = i;
                mhandler.sendMessage(ms);
            }
        }
    }

    public void DragSong(){
        ItemTouchHelper hel = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder drag, @NonNull RecyclerView.ViewHolder target) {

                int pos_drag = drag.getAdapterPosition();
                int pos_tar = target.getAdapterPosition();

                Collections.swap(itemModelArrayList, pos_drag, pos_tar);
                adapter.notifyItemMoved(pos_drag, pos_tar);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        hel.attachToRecyclerView(recyclerView);
    }
    @Override
    public void onClickedItem(int pos) {

        if (isPlaying)
        {
            onPaused();
            mediaPlayer.stop();
        }
        PlaySong(pos);
    }



    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action){
                case CreateNotification.BACKACTION:
                    onBack();
                    break;
                case CreateNotification.NEXTACTION:
                    onNext();
                    //Toast.makeText(context, "on function", Toast.LENGTH_SHORT).show();
                    break;
                case CreateNotification.PLAYACTION:
                    if(isPlaying){
                        onPaused();
                    } else onPlay();
                    break;
            }
        }
    };

    @Override
    public void onBack() {
        position--;
        CreateNotification.NotificationCreate(MainActivity.this,
                itemModelArrayList.get(position),
                R.drawable.ic_pause_black_24dp,
                position,
                itemModelArrayList.size() - 1);
        mediaPlayer.stop();
        PlaySong(position);

    }

    @Override
    public void onNext() {
        position++;
        CreateNotification.NotificationCreate(MainActivity.this,
                itemModelArrayList.get(position),
                R.drawable.ic_pause_black_24dp,
                position,
                itemModelArrayList.size() - 1);
        mediaPlayer.stop();
        PlaySong(position);
    }

    @Override
    public void onPlay() {
        int resource = 1;
        if (isPlaying){
            resource = (R.drawable.ic_pause_black_24dp);
            imgplay.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            resource = (R.drawable.ic_play_arrow_black_24dp);
            imgplay.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        }
        CreateNotification.NotificationCreate(MainActivity.this,
                itemModelArrayList.get(position),
                //R.drawable.ic_pause_black_24dp,
                resource,
                position,
                itemModelArrayList.size() - 1);
        isPlaying = true;

        mediaPlayer.start();
    }

    @Override
    public void onPaused() {
        int resource = 0;


        if (isPlaying){
            resource = (R.drawable.ic_pause_black_24dp);
            imgplay.setImageResource(R.drawable.ic_pause_black_24dp);

        } else {
            resource = (R.drawable.ic_play_arrow_black_24dp);
            imgplay.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        }
        CreateNotification.NotificationCreate(MainActivity.this,
                itemModelArrayList.get(position),
                //R.drawable.ic_play_arrow_black_24dp,
                resource,
                position,
                itemModelArrayList.size() - 1);
        isPlaying = false;

        mediaPlayer.pause();

    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.channel_id, "TinNguyen", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);

            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }
}
