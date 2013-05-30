package my.amppercent.adapters;

import java.util.Collection;
import java.util.List;

import my.amppercent.project.TabFragment;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * Generico adattatore per semplificare ed uniformare la visualizzazione degli
 * elementi
 * 
 * @author jack
 * 
 * @param <T>
 */
public class ListViewAdapting<T> {
	private AdapterElems<T> list;
	private ListView listview;

	public AdapterElems<T> getAdapter() {
		return this.list;
	}

	public ListViewAdapting(Activity mainview, int MainViewListId,
			AdapterElems<T> coll) {
		this.listview = (ListView) mainview.findViewById(MainViewListId);
		this.list = coll;
		if (this.listview != null) {
			this.listview
					.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			this.listview.setStackFromBottom(true);
			this.listview.setAdapter(this.list);
		} else
			Log.e("ListViewAdapting", "null listview");
	}

	public ListViewAdapting(TabFragment fragment, int MainViewListId,
			AdapterElems<T> coll) {
		View toview = fragment.getInnerView();
		this.listview = (ListView) toview.findViewById(MainViewListId);
		this.list = coll;
		if (this.listview != null) {
			this.listview
					.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			this.listview.setStackFromBottom(true);
			this.listview.setAdapter(this.list);
		} else
			Log.e("ListViewAdapting", "null listview");
	}

	public void add_and_update(T... ls) {
		this.list.addAll(ls);
		this.list.notifyDataSetChanged();
	}

	public void add_and_update(T x) {
		this.list.add(x);
		this.list.notifyDataSetChanged();
	}

	public void add_and_update(Collection<T> ls) {
		this.list.addAll(ls);
		this.list.notifyDataSetChanged();
	}

	public List<T> getList() {
		return this.list.getList();
	}

	public void refresh() {
		if (!this.list.isEmpty())
			this.list.notifyDataSetChanged();
	}

	public void clear() {
		this.list.clear();
	}

}
