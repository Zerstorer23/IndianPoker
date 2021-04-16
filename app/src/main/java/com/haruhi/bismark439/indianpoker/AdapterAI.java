package com.haruhi.bismark439.indianpoker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import static com.haruhi.bismark439.indianpoker.MainActivity.AiDB;

public class AdapterAI extends BaseAdapter {
	
	Context mContext;
	LayoutInflater mInflate;
	public AdapterAI(Context context) {
		mContext = context;
		mInflate = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return AiDB.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return AiDB.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;//Alarms.get(position).reqCode;
	}

	public boolean updateRadio(){
		notifyDataSetChanged();
		return true;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AiListView layoutSingleAlarmItem = (AiListView) convertView;
		
		if (layoutSingleAlarmItem == null) {
			layoutSingleAlarmItem = new AiListView(mContext);
		//	layoutSingleAlarmItem.setOnRemoveButtonClickListener(onRemoveButtonClickListner);
		}
		layoutSingleAlarmItem.setData(AiDB.get(position),position);

		return layoutSingleAlarmItem;
	}
	

}
