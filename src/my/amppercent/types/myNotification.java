package my.amppercent.types;

import my.amppercent.project.LaunchMe;
import my.amppercent.project.TabFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Gestione delle notifiche
 * 
 * @author jack
 * 
 */
public class myNotification {

	private NotificationManager nm;
	private Context c;
	private String tag;
	private int id;
	private String connectionid = null;
	private String password = null;

	public static final int FILE_REQUEST = 0x19876;
	public static final int BROADCAST_CHAT_REQUEST = 0x65478;
	public static final String BROADCAST_CHAT_REQ_STRING = "my.amppercent.CHAT";
	public static final String FILE_REQUEST_STRING = "my.amppercent.FILE";
	public static final String IS_NOTIFICATION = "IS_NOTIFICATION";

	public static final String FIRST_BUNDLE = "first";
	public static final String SECOND_BUNDLE = TabFragment.CHATNAME;

	public myNotification(Context c, Class<?> to_intent, String tag, int id) {
		this.c = c;
		nm = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		this.tag = tag;
		this.id = id;
	}

	public void setConnPassword(String connid, String pw) {
		this.connectionid = connid;
		this.password = pw;
	}

	public void notify_case(String maintext, String subtext) {
		Intent intent;
		int icon = 0;

		switch (id) {
		case FILE_REQUEST: {
			intent = new Intent(c, LaunchMe.class);
			intent.putExtra(myNotification.FIRST_BUNDLE, maintext); // requestor
			intent.putExtra(myNotification.SECOND_BUNDLE, subtext); // file
			intent.putExtra("kind", FILE_REQUEST);
			intent.setAction(FILE_REQUEST_STRING);
			// Preciso intento di non palesare altrimenti le informazioni
			// sensibili
			if ((this.password != null) && (this.connectionid != null)) {
				intent.putExtra(TabFragment.CONNECTIONID, connectionid);
				intent.putExtra(TabFragment.PASSWORD, this.password);
			}
			icon = android.R.drawable.stat_sys_download;
			break;
		}
		case BROADCAST_CHAT_REQUEST: {
			intent = new Intent(this.c, LaunchMe.class);
			intent.setAction(BROADCAST_CHAT_REQ_STRING);
			intent.putExtra(myNotification.SECOND_BUNDLE, maintext);
			// Preciso intento di non palesare altrimenti le informazioni
			// sensibili
			if ((this.password != null) && (this.connectionid != null)) {
				intent.putExtra(TabFragment.CONNECTIONID, connectionid);
				intent.putExtra(TabFragment.PASSWORD, this.password);
			}
			icon = android.R.drawable.ic_dialog_email;
			break;
		}
		default:
			intent = null;
		}
		PendingIntent pi = PendingIntent.getActivity(c, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification n = new Notification(icon, maintext,
				System.currentTimeMillis());
		n.flags = n.flags | Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_SHOW_LIGHTS;
		n.defaults = n.defaults | Notification.DEFAULT_VIBRATE
				| Notification.DEFAULT_SOUND;
		n.setLatestEventInfo(c, maintext, subtext, pi);
		nm.notify(this.tag, this.id, n);
	}

}
