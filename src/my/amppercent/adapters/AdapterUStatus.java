package my.amppercent.adapters;

import java.util.LinkedList;
import java.util.List;

import my.amppercent.project.R;
import my.amppercent.remoteservice.XUser;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Array per la visualizzazione degli utenti
 * 
 * @author jack
 * 
 */
public class AdapterUStatus extends AdapterElems<XUser> {

	public List<String> wannachatwith;
	public List<XUser> wantchatxuser;

	/**
	 * 
	 * @param context
	 *            Contesto nel quale effettuare la visualizzazione
	 * @param resource
	 * @param textViewResourceId
	 *            Opzionale
	 * @param active
	 *            Attitivtà nella quale verrà effettuata la visualizzazione
	 * @param field
	 *            Sono i campi nei quali verranno settati i valori: 1) Valore
	 *            dello stato 2) Nickname 3) Jid utente
	 * @param objects
	 */

	public AdapterUStatus(Context context, int resource,
			int textViewResourceId, List<XUser> objects) {
		super(context, resource, textViewResourceId, objects);
		wannachatwith = new LinkedList<String>();
		wantchatxuser = new LinkedList<XUser>();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Integer pos = position;
		if (getItem(position) == null) {
			Log.e("error", "nullo @" + pos.toString());
			return null;
		}
		final XUser user = getItem(position);

		View row = super.getView(position, convertView, parent);

		/* 1) Valore dello stato */
		ImageView stato = (ImageView) row.findViewById(R.id.Status);
		if (user.getState() == null)
			stato.setImageResource(android.R.drawable.presence_offline);
		else
			switch (user.getState()) {
			case DoNotDisturb:
				stato.setImageResource(android.R.drawable.presence_busy);
				break;
			case Chat:
				stato.setImageResource(android.R.drawable.presence_online);
				break;
			case Away:
			case ExtendedAway:
				stato.setImageResource(android.R.drawable.presence_away);
				break;
			default:
				stato.setImageResource(android.R.drawable.presence_offline);
			}

		/* 2) Nickname */
		final TextView text = (TextView) row.findViewById(R.id.Nickname);
		text.setText(user.getNickname());

		/* 3) jid */
		final TextView text2 = (TextView) row.findViewById(R.id.user_jid);
		text2.setText(user.getJid());

		/* 4) Button Handler */
		final ImageButton ib = (ImageButton) row.findViewById(R.id.doChat);
		if (wannachatwith.contains(user.getJid()))
			ib.setImageResource(android.R.drawable.ic_menu_add);
		ib.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String jid = text2.getText().toString();
				if (!wannachatwith.contains(jid)) {
					user.selected = true;
					ib.setImageResource(android.R.drawable.ic_menu_add);
					wannachatwith.add(jid);
					wantchatxuser.add(user);
				} else {
					XUser todel = null;
					user.selected = false;
					wannachatwith.remove(jid);
					for (XUser x : wantchatxuser) {
						if (x.getJid().equals(jid)) {
							todel = x;
							break;
						}
					}
					if (todel != null) {
						wannachatwith.remove(todel);
						for (XUser x : wantchatxuser) {
							if (x.getJid().equals(jid)) {
								wantchatxuser.remove(x);
								break;
							}
						}
					}
					ib.setImageResource(android.R.drawable.sym_action_chat);
				}
			}
		});

		return row;
	}

	/**
	 * Restituisce la lista di XUser selezionati dall'utente
	 * 
	 * @return
	 */
	public List<XUser> getChatWith() {
		return this.wantchatxuser;
	}

}
