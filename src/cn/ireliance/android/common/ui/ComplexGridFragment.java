package cn.ireliance.android.common.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.setting.ItemListFragment.Callbacks;

public abstract class ComplexGridFragment extends Fragment {
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	protected static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position. Only used on tablets.
	 */
	protected int mActivatedPosition = ListView.INVALID_POSITION;
	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	protected static ItemSelectedCallback sDummyCallbacks = ItemSelectedCallback.DummyCallbacks
			.getInstance();

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	protected ItemSelectedCallback mCallbacks = sDummyCallbacks;

	final private Handler mHandler = new Handler();

	final private Runnable mRequestFocus = new Runnable() {
		public void run() {
			mGrid.focusableViewAvailable(mGrid);
		}
	};

	final private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			onListItemClick((GridView) parent, v, position, id);
		}
	};
	
	final private AdapterView.OnItemLongClickListener mOnLongClickListener = new AdapterView.OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			if(mCallbacks!=null)
				return mCallbacks.onItemLongClick((GridView) parent, v, position, id);
			return false;
		}
	};

	protected ListAdapter mAdapter;
	private GridView mGrid;

	public ComplexGridFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof ItemSelectedCallback) {
			mCallbacks = (ItemSelectedCallback) activity;
		} else if (activity instanceof SwitchCallbackFragmentActivity) {
			mCallbacks = (ItemSelectedCallback) ((SwitchCallbackFragmentActivity) activity)
					.getItemSelectedCallback(this.getClass());
		} else {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		// mCallbacks = (Callbacks) activity;
	}

	/**
	 * Attach to list view once the view hierarchy has been created.
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ensureList();
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Detach from list view.
	 */
	@Override
	public void onDestroyView() {
		mHandler.removeCallbacks(mRequestFocus);
		mGrid = null;
		// mListShown = false;
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	/**
	 * This method will be called when an item in the list is selected.
	 * Subclasses should override. Subclasses can call
	 * getListView().getItemAtPosition(position) if they need to access the data
	 * associated with the selected item.
	 * 
	 * @param l
	 *            The GridView where the click happened
	 * @param v
	 *            The view that was clicked within the GridView
	 * @param position
	 *            The position of the view in the list
	 * @param id
	 *            The row id of the item that was clicked
	 */
	public void onListItemClick(GridView l, View v, int position, long id) {
		if(mCallbacks!=null)
			mCallbacks.onItemSelected(l, v, position, id);
	}

	/**
	 * Provide the cursor for the list view.
	 */
	public void setListAdapter(ListAdapter adapter) {
		mAdapter = adapter;
		if (mGrid != null) {
			mGrid.setAdapter(adapter);
		}
	}

	/**
	 * Set the currently selected list item to the specified position with the
	 * adapter's data
	 * 
	 * @param position
	 */
	public void setSelection(int position) {
		ensureList();
		mGrid.setSelection(position);
	}

	/**
	 * Get the position of the currently selected list item.
	 */
	public int getSelectedItemPosition() {
		ensureList();
		return mGrid.getSelectedItemPosition();
	}

	/**
	 * Get the cursor row ID of the currently selected list item.
	 */
	public long getSelectedItemId() {
		ensureList();
		return mGrid.getSelectedItemId();
	}

	/**
	 * Get the activity's list view widget.
	 */
	public GridView getListView() {
		ensureList();
		return mGrid;
	}

	/**
	 * Get the ListAdapter associated with this activity's GridView.
	 */
	public ListAdapter getListAdapter() {
		return mAdapter;
	}

	public abstract GridView getRawGridView();

	private void ensureList() {
		if (mGrid != null) {
			return;
		}
		mGrid = getRawGridView();
		mGrid.setOnItemClickListener(mOnClickListener);
		mGrid.setOnItemLongClickListener(mOnLongClickListener);
		if (mAdapter != null) {
			ListAdapter adapter = mAdapter;
			mAdapter = null;
			setListAdapter(adapter);
		}
		mHandler.post(mRequestFocus);
	}

}
