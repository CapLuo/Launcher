package com.example.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

public class GridItemCallBack implements ItemSelectedCallback {
	private SwitchCallbackFragmentActivity activty;
	private Fragment fragment;

	public GridItemCallBack(SwitchCallbackFragmentActivity activty,
			Fragment fragment) {
		this.activty = activty;
		this.fragment = fragment;
	}

	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
//		Intent detailIntent = new Intent(activty, .class);
//		activty.startActivity(detailIntent);
		if(fragment instanceof MyGridFragment){
			((MyGridFragment)fragment).open(position);
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onDetach() {
		activty.removeItemSelectedCallback(fragment.getClass());
	}

}
