package com.example.gene.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> fname;
	private ArrayList<String> lname;

	public DisplayAdapter(Context c, ArrayList<String> fname, ArrayList<String> lname) {
		this.context = c;
		this.fname = fname;
		this.lname = lname;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fname.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}


	public View getView(int pos, View child, ViewGroup parent) {
		Holder holder;
		LayoutInflater layoutInflater;
		if (child == null) {
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			child = layoutInflater.inflate(R.layout.list_row, null);
			holder = new Holder();
			holder.txt_fName = (TextView) child.findViewById(R.id.txt_fName);
			holder.txt_lName = (TextView) child.findViewById(R.id.txt_lName);
			child.setTag(holder);
		} 
		
		else {
			holder = (Holder) child.getTag();
		}
		
		holder.txt_fName.setText(fname.get(pos));
		holder.txt_lName.setText(lname.get(pos));

		return child;
	}

	public class Holder {
		TextView txt_fName;
		TextView txt_lName;
	}

}
