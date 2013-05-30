package my.amppercent.adapters;

import java.util.List;

import my.amppercent.project.R;
import my.amppercent.remoteservice.IFMessage;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class AdapterChat extends AdapterElems<IFMessage> {

	public AdapterChat(Context context, int resource, int textViewResourceId,
			IFMessage[] objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public AdapterChat(Context context, int resource, int textViewResourceId,
			List<IFMessage> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItem(position) == null)
			Log.e("error", "nullo");
		String[] s = getItem(position).getArray();

		View row = super.getView(position, convertView, parent);
		int[] fields = { R.id.Username_text, R.id.Body_message };
		for (int i = 0; i < fields.length; i++) {
			Object obj = row.findViewById(fields[i]);
			if (obj instanceof TextView) {
				((TextView) obj).setText(s[i]);
			} else if (obj instanceof EditText) {
				((EditText) obj).setText(s[i]);
			}
		}
		return row;
	}

}
