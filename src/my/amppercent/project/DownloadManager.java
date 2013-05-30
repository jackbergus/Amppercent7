package my.amppercent.project;

import java.util.LinkedList;
import java.util.List;

import my.amppercent.adapters.AdapterIM;
import my.amppercent.adapters.ListViewAdapting;
import my.amppercent.project.R;
import my.amppercent.remoteservice.IBinding;
import my.amppercent.remoteservice.IntentManage;
import my.amppercent.types.State;
import my.amppercent.types.myNotification;
import my.amppercent.types.newActivity;

import android.os.Bundle;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadManager extends newActivity {

	private Bundle temp = new Bundle();

	private ListViewAdapting<IntentManage> lva = null;

	private AdapterIM aim = null;

	private IntentManage[] lim;

	@Override
	public void onAIDLConnected(ComponentName name, IBinder service,
			IBinding myservice) {
		if (aim == null) {
			aim = AdapterIM.ArrayNullInit(this, R.layout.download_view, 0, lim);
			if (lva == null)
				lva = new ListViewAdapting<IntentManage>(this, R.id.listview,
						aim);
		} else
			((AdapterIM) lva.getAdapter()).setService(myservice);
	}

	private void receiveIntent(Intent i, Bundle savedInstanceState) {
		IntentManage j = new IntentManage(i);

		if (savedInstanceState != null) {
			temp = savedInstanceState;
		} else {
			temp = new Bundle();
		}
		Log.d("receiveIntent: first/second", j.first_argument + " "
				+ j.second_argument);

		if ((j != null) && (j.first_argument != null)
				&& (j.second_argument != null))
			temp.putParcelable(j.getId(), j);

		lim = new IntentManage[temp.keySet().size()];
		int k = 0;

		if ((temp.keySet() != null) || (temp.size() > 0))
			for (String x : temp.keySet()) {
				Log.d("DownloadManager:key", x);
				try {
					IntentManage im = (IntentManage) temp.getParcelable(x);
					lim[k++] = im;
				} catch (Throwable t) {

				}
			}

		if (aim == null) {
			aim = AdapterIM.ArrayNullInit(this, R.layout.download_view, 0, lim);
			if (lva == null)
				lva = new ListViewAdapting<IntentManage>(this, R.id.listview,
						aim);
		} else {
			lva.add_and_update(j);
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		/* Visualizzazione dell'interfaccia */
		super.onCreate(savedInstanceState);
		this.setTitle("Download Manager");
		setContentView(R.layout.listview);

		Intent i = getIntent();
		receiveIntent(i, savedInstanceState);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		State.manager_in = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Updates with only the not handled objects
		List<String> toremove = new LinkedList<String>();
		for (String id : temp.keySet()) {
			try {
				IntentManage im = (IntentManage) temp.getParcelable(id);
				if (im.handled) {
					toremove.add(id);
				}
			} catch (Throwable t) {
				toremove.add(id);
			}

		}
		for (String id : toremove) {
			temp.remove(id);
		}
		super.onSaveInstanceState(temp);
	}

	public void deleteString(String id) {
		this.temp.remove(id);
	}

	@Override
	public void initializer() {
		State.manager_in = true;
		setCouldSetMainVisible(false);
		BroadcastReceiver brodrecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				receiveIntent(intent, temp);
			}
		};
		setBroadcastReceiver(brodrecv, myNotification.FILE_REQUEST_STRING);
		setAIDL(IBinding.class);
	}

	@Override
	public void onIntent(Intent i) {
	}
}
