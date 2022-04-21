package com.ivyiot.appsdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivyiot.appsdk.R;
import com.ivyiot.ipclibrary.model.PlaybackRecordInfo;
import com.ivyiot.ipclibrary.model.PlaybackRecordListInfoArgsType0;
import com.ivyiot.ipclibrary.model.PlaybackRecordListInfoArgsType1;

import java.util.List;

/**
 * 回放列表适配器
 */
public class PlaybackListAdapter extends BaseAdapter {
	/** context */
	private Context context = null;
	/** 列表数据源 */
	private List<PlaybackRecordInfo> playbackList = null;

	/** 列表item */
	private TextView tv_name;

	/**
	 * 构造函数
	 *
	 * @param context
	 *            context
	 * @param playbackList
	 *            回放列表
	 */
	public PlaybackListAdapter(Context context, List<PlaybackRecordInfo> playbackList) {
		this.context = context;
		this.playbackList = playbackList;
	}


	@Override
	public int getCount() {
		int len = 0;
		if (playbackList != null) {
			len = playbackList.size();
		}
		return len;
	}

	@Override
	public Object getItem(int position) {
		PlaybackRecordInfo dev = null;
		if (playbackList != null) {
			dev = playbackList.get(position);
		}
		return dev;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		PlaybackRecordInfo playbackRecord = playbackList.get(position);
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.play_back_item, null);
		}
		tv_name =  view.findViewById(R.id.tv_playback_name);
		if (playbackRecord != null) {
			view.setTag(playbackRecord);
			if(playbackRecord instanceof PlaybackRecordListInfoArgsType0){
				PlaybackRecordListInfoArgsType0 playbackInfo = (PlaybackRecordListInfoArgsType0) playbackRecord;
				tv_name.setText(playbackInfo.sTime + " - "+ playbackInfo.eTime);
			}
			if(playbackRecord instanceof PlaybackRecordListInfoArgsType1){
				PlaybackRecordListInfoArgsType1 playbackInfo = (PlaybackRecordListInfoArgsType1) playbackRecord;
				tv_name.setText(playbackInfo.recordPath);
			}

		}
		return view;
	}
}
