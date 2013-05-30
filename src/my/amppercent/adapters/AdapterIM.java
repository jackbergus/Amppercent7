package my.amppercent.adapters;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import my.amppercent.project.R;
import my.amppercent.remoteservice.IBinding;
import my.amppercent.remoteservice.IntentManage;

/**
 * Adattatore per la visualizzazione della lista di Intent per la
 * visualizzazione delle richieste di download
 * 
 * @author jack
 * 
 */
public class AdapterIM extends AdapterElems<IntentManage> {

	private IBinding service;

	public AdapterIM(Context context, int resource, int textViewResourceId,
			IntentManage[] objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public AdapterIM(Context context, int resource, int textViewResourceId,
			List<IntentManage> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public static AdapterIM ArrayNullInit(Context context, int resource,
			int textViewResourceId, IntentManage[] objects) {
		if ((objects == null) || (objects.length == 0)) {
			return new AdapterIM(context, resource, textViewResourceId,
					new LinkedList<IntentManage>());
		} else {
			List<IntentManage> im = new LinkedList<IntentManage>();
			for (IntentManage x : objects)
				im.add(x);
			return new AdapterIM(context, resource, textViewResourceId, im);
		}
	}

	public void setService(IBinding s) {
		this.service = s;
	}

	public void remove(IntentManage x) {
		super.remove(x);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		final IntentManage im = getItem(position);
		final TextView from = (TextView) row.findViewById(R.id.To_or_From_text);
		final TextView file = (TextView) row.findViewById(R.id.File_name);
		final Button ok = (Button) row.findViewById(R.id.Download_button_file);
		final Button ko = (Button) row.findViewById(R.id.Decline_button_file);

		file.setText(im.second_argument);
		from.setText(im.first_argument);
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (service != null) {
					try {
						ok.setEnabled(false);
						boolean result = service.handleFileRequest(im.connid,
								im.passwo, true, im.getId(), im.second_argument);
						if (result) {
							Log.d("result", "ok");
							ko.setEnabled(false);
						}
						im.handled = true;
						remove(im);
					} catch (Throwable e) {
					}
				}
			}
		});
		ko.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (service != null) {
					try {
						ok.setEnabled(false);
						service.handleFileRequest(im.connid, im.passwo, true,
								im.getId(), im.second_argument);
						ko.setEnabled(false);
						im.handled = true;
						remove(im);
					} catch (Throwable e) {
					}
				}
			}
		});

		return row;
	}

}
