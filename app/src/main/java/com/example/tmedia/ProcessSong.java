package com.example.tmedia;

import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

public class ProcessSong implements Runnable{

    ProgressBar progressBar;
    int timePlay;
    Handler mhandler;



    @Override
    public void run() {


    }
}


//    public void StarProcess(final int pos){
//
//        Thread tr = new Thread(new Runnable() {
//            int timeDuration = itemModelArrayList.get(pos).getTimeDuration();
//            @Override
//            public void run() {
//                progressBar.setMax(timeDuration);
//                for(int i=0; i<timeDuration;++i){
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Message ms = new Message();
//
//                    ms.what = REQUEST_MESS;
//                    ms.arg1 = i;
//                    mhandler.sendMessage(ms);
//                }
//
//            }
//        });
//
//        if(isPlaying){
//            onPaused();
//            tr.interrupt();
//        } else {
//            onPlay();
//            tr.start();
//        }
//
//    }