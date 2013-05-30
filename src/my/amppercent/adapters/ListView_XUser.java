package my.amppercent.adapters;

import java.util.LinkedList;
import java.util.List;

import my.amppercent.remoteservice.XUser;

import android.util.Log;
import android.widget.ListView;
import android.app.Activity;

/**
 * Implementa la visualizzazione della lista degli utenti. È stato necessario
 * distinguerlo da ListViewAdapting, in quando era necessario ottenere da
 * AdapterUStatus delle informazioni aggiuntive (quali i wantchatxuser)
 * 
 * @author giacomo
 * 
 */
public class ListView_XUser {
	private ListView listview;
	private AdapterUStatus da;
	private Activity ma;

	/**
	 * Restituisce gli elementi selezionati dall'utente, ai fini di insaurare
	 * con loro una chat
	 * 
	 * @return
	 */
	public List<XUser> getWanttochat() {
		return this.da.wantchatxuser;
	}

	/**
	 * 
	 * @param id_listview
	 *            Id della lista da visualizzare
	 * @param id_mainview
	 *            Id del layout per la visualizzazione dell'item
	 * @param toview
	 *            Activity di destinazione
	 * @param list
	 *            Elementi di inizializzazione della lista
	 */
	public ListView_XUser(int id_listview, int id_mainview, Activity toview,
			XUser... list) {
		this.ma = toview;
		this.listview = (ListView) toview.findViewById(id_listview);
		List<XUser> tmp = new LinkedList<XUser>();
		for (XUser x : list) {
			tmp.add(x);
		}
		this.da = new AdapterUStatus(toview, id_mainview, 0, tmp);
		if (this.listview == null) {
			Log.e("listview null", "null list view");
			return;
		}
		this.listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		this.listview.setStackFromBottom(true);
		this.listview.setAdapter(this.da);

	}

	public void add_and_update(XUser... messages) {
		for (XUser x : messages) {
			add_and_update(x);
		}
	}

	public void add_and_update(List<XUser> messages) {
		for (XUser x : messages) {
			add_and_update(x);
		}
	}

	public boolean checkExistance(XUser x) {
		for (XUser t : this.da.getList()) {
			if (t.getJid().equals(x.getJid()))
				return true;
		}
		return false;
	}

	public void add_and_update(XUser x) {
		this.da.add(x);
		this.da.notifyDataSetChanged();
	}

	public void clear() {
		this.da.clear();
		this.da.notifyDataSetChanged();
	}

	public Activity getActivity() {
		return this.ma;
	}

	public List<XUser> getChatWith() {
		return this.da.getChatWith();
	}

}
