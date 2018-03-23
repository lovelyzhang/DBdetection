package com.example.dbdetection;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class ARThread extends Thread {
	private AudioRecord ar;
	private int bs;
	private static int SAMPLE_RATE_IN_HZ = 8000;
	private Handler hl;
	private Boolean isRun;
	private Object mLock = new Object();
	
	public ARThread(Handler handler) {
		super();
		bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bs);
		hl = handler;
	}

	public void run() {
		ar.startRecording();
		// 用于读取的 buffer
		short[] buffer = new short[bs];
		isRun = true;
		while(isRun)
		{
			int r = ar.read(buffer, 0, bs);
			long v = 0;
			// 将 buffer 内容取出，进行平方和运算
			for (int i = 0; i <buffer.length; i++) {
				// 这里没有做运算的优化，为了更加清晰的展示代码
				v += buffer[i] * buffer[i];
			}
			// 平方和除以数据总长度，得到音量大小。可以获取白噪声值，然后对实际采样进行标准化。
			// 如果想利用这个数值进行操作，建议用 sendMessage 将其抛出，在 Handler 里进行处理。
			double mean = v / (double) r;
			double db = 10 * Math.log10( mean);
			int dbint = (int) Math.round(db);
			Bundle b = new Bundle();
			b.putString("dbValue", String.valueOf(dbint));
			Message msg = hl.obtainMessage();
			msg.setData(b);
			msg.what = 000;
			hl.sendMessage(msg);
            
			synchronized (mLock) {  
                try {  
                    mLock.wait(100);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
            } 
		}
		ar.stop();
	}
	public void pause()
	{
		isRun = false;
	}
}
