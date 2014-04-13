package cn.ireliance.android.common.ui;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class SwitchCallbackFragmentActivity extends FragmentActivity {
	private Map<Class<? extends Fragment>, ItemSelectedCallback> callBackes;

	public SwitchCallbackFragmentActivity() {
		super();
		callBackes = new HashMap<Class<? extends Fragment>, ItemSelectedCallback>();
	}

	public ItemSelectedCallback getItemSelectedCallback(
			Class<? extends Fragment> fragmentClass) {
		return callBackes.get(fragmentClass);
	}

	public void putItemSelectedCallback(Class<? extends Fragment> fragmentClass,
			ItemSelectedCallback instance) {
		callBackes.put(fragmentClass, instance);
	}
	
	public void removeItemSelectedCallback(Class<? extends Fragment> fragmentClass) {
		callBackes.remove(fragmentClass);
	}
}
