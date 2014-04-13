package cn.ireliance.android.common.ui;

import android.view.View;
import android.widget.AdapterView;

public interface ItemSelectedCallback {
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id);
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id);
	
	public void onDetach();
	
	public final class DummyCallbacks implements ItemSelectedCallback {
		private  static final DummyCallbacks instance = new DummyCallbacks() ;
		
		public static DummyCallbacks getInstance() {
			return instance;
		}

		public void onItemSelected(AdapterView<?> adapterView, View view,
				int position, long id) {
			// TODO Auto-generated method stub
		}

		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onDetach() {
			
		}
	}
}
