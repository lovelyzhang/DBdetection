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
		// ���ڶ�ȡ�� buffer
		short[] buffer = new short[bs];
		isRun = true;
		while(isRun)
		{
			int r = ar.read(buffer, 0, bs);
			long v = 0;
			// �� buffer ����ȡ��������ƽ��������
			for (int i = 0; i <buffer.length; i++) {
				// ����û����������Ż���Ϊ�˸���������չʾ����
				v += buffer[i] * buffer[i];
			}
			// ƽ���ͳ��������ܳ��ȣ��õ�������С�����Ի�ȡ������ֵ��Ȼ���ʵ�ʲ������б�׼����
			// ��������������ֵ���в����������� sendMessage �����׳����� Handler ����д���
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
