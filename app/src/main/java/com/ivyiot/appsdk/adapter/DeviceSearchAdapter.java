package com.ivyiot.appsdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivyiot.appsdk.R;
import com.ivyiot.ipclibrary.model.DiscoveryDev;

import java.util.List;

/**
 * 搜索device列表适配器
 */
public class DeviceSearchAdapter extends BaseAdapter {
	/** context */
	private Context context = null;
	/** 摄像头 */
	private List<DiscoveryDev> devices = null;

	/** 摄像头名称 */
	private TextView tv_name;
	/** 摄像头UID */
	private TextView tv_uid;
	/** 摄像头IP和端口 */
	private TextView tv_ipwithport;

	/**
	 * 构造函数
	 *
	 * @param context
	 *            context
	 * @param camArr
	 *            camera列表内容
	 */
	public DeviceSearchAdapter(Context context, List<DiscoveryDev> camArr) {
		this.context = context;
		this.devices = camArr;
	}


	@Override
	public int getCount() {
		int len = 0;
		if (devices != null) {
			len = devices.size();
		}
		return len;
	}

	@Override
	public Object getItem(int position) {
		DiscoveryDev dev = null;
		if (devices != null) {
			dev = devices.get(position);
		}
		return dev;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		DiscoveryDev dev = devices.get(position);
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.camera_wlan_search_item, null);
		}
		tv_name =  view.findViewById(R.id.tv_name);
		tv_uid =  view.findViewById(R.id.tv_uid);
		tv_ipwithport =  view.findViewById(R.id.tv_ipwithport);
		if (dev != null) {
			view.setTag(dev);
			tv_name.setText(dev.name);
			String uid = dev.uid;
			if (TextUtils.isEmpty(uid)) {
				tv_uid.setVisibility(View.GONE);
			} else {
				tv_uid.setVisibility(View.VISIBLE);
				tv_uid.setText(uid);
			}
			String ip = dev.ip;
			if (TextUtils.isEmpty(ip)) {
				tv_ipwithport.setVisibility(View.GONE);
			} else {
				tv_ipwithport.setVisibility(View.VISIBLE);
				tv_ipwithport.setText(dev.ip + ":" + dev.port);
			}
		}
		return view;
	}
}
