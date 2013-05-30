package my.amppercent.project;

import my.amppercent.types.State;
import my.amppercent.types.myNotification;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Classe necessaria ad effettuare la gestione dell'azione scatenata
 * dall'utente, a partire dalla pressione della notifica. Serve unicamente per
 * decidere se inviare un broadcast oppure attivare l'attività con un Intent, in
 * base alla visualizzazione delle Activity.
 * 
 * @author jack
 * 
 */
public class LaunchMe extends Activity {

	public static Intent copy(Intent from, Intent to) {
		return to;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		boolean test = false;

		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		String action = i.getAction();
		Intent j;
		Class<?> cls = null;
		Log.d("LaunchMe", "okhere");

		if ((action.equals(myNotification.BROADCAST_CHAT_REQ_STRING))) {
			test = (State.main_is_visible || State.main_is_active);
			cls = Amppercent4Activity.class;
			Log.d("LaunghMe", "CHAT");
		} else if ((action.equals(myNotification.FILE_REQUEST_STRING))) {
			test = State.manager_in;
			cls = DownloadManager.class;
			Log.d("LaunghMe", "FILE_REQUEST");
		} else
			finish();

		// Se il servizio di destinazione è visibile, allora gli invio un
		// broadcast, altrimenti gli lancio un intent
		if (!test) {
			Log.d("LaunchMe", "visibility = false");
			j = new Intent(this, cls);
		} else {
			Log.d("LaunchMe", "visibility = true");
			j = new Intent();
		}
		// Ricopio i dati ricevuti
		j.putExtras(i.getExtras());

		j.setAction(action);
		if (test)
			sendBroadcast(j);
		else
			startActivity(j);

		Log.d("LaunchMe", "ok");
		finish();

	}

}
