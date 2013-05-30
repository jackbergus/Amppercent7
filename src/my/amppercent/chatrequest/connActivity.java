package my.amppercent.chatrequest;

import my.amppercent.project.R;
import my.amppercent.project.TabFragment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Effettua l'inserimento dei dati per stabilire la connessione con il server
 * (Collegamento + login)
 * 
 * @author jack
 * 
 */
public class connActivity extends Activity {

	private Button b;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_server);
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		Resources res = getResources();
		((EditText) findViewById(R.id.Username)).setText(prefs.getString(
				TabFragment.USERNAME, "yourname"));
		((EditText) findViewById(R.id.Password)).setText(prefs.getString(
				TabFragment.PASSWORD, ""));
		((EditText) findViewById(R.id.Status)).setText(prefs.getString(
				SelectConnActivity.STATUS,
				res.getString(R.string.example_status)));
		((EditText) findViewById(R.id.Host))
				.setText(prefs.getString(SelectConnActivity.HOST,
						res.getString(R.string.example_server)));
		((EditText) findViewById(R.id.Service)).setText(prefs.getString(
				SelectConnActivity.SERVICE,
				res.getString(R.string.example_service)));
		((EditText) findViewById(R.id.Port_no))
				.setText(Integer
						.valueOf(
								prefs.getInt(
										SelectConnActivity.PORT,
										Integer.parseInt(res
												.getString(R.string.default_port))))
						.toString());
		((CheckBox) findViewById(R.id.setAvailable)).setChecked(prefs
				.getBoolean(SelectConnActivity.AVAIL, false));
		((CheckBox) findViewById(R.id.setSASL)).setChecked(prefs.getBoolean(
				SelectConnActivity.SASL, true));

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		b = (Button) findViewById(R.id.newConnect_start);
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String username = ((EditText) findViewById(R.id.Username))
						.getText().toString();
				String password = ((EditText) findViewById(R.id.Password))
						.getText().toString();
				String status = ((EditText) findViewById(R.id.Status))
						.getText().toString();
				boolean avail = ((CheckBox) findViewById(R.id.setAvailable))
						.isChecked();
				String host = ((EditText) findViewById(R.id.Host)).getText()
						.toString();
				Integer port;
				String service = ((EditText) findViewById(R.id.Service))
						.getText().toString();
				Boolean sasl = ((CheckBox) findViewById(R.id.setSASL))
						.isChecked();
				try {
					port = Integer
							.parseInt(((EditText) findViewById(R.id.Port_no))
									.getText().toString());
				} catch (Throwable t) {
					port = 5222;
				}
				Intent i = getIntent();
				i.putExtra(TabFragment.USERNAME, username);
				i.putExtra(TabFragment.PASSWORD, password);
				i.putExtra(SelectConnActivity.STATUS, status);
				i.putExtra(SelectConnActivity.AVAIL, avail);
				i.putExtra(SelectConnActivity.HOST, host);
				i.putExtra(SelectConnActivity.PORT, port);
				i.putExtra(SelectConnActivity.SERVICE, service);
				i.putExtra(SelectConnActivity.SASL, sasl);
				setResult(RESULT_OK, i);
				Log.d("EXITING_NO_CONN", "Doing finish");
				finish();
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		String username = ((EditText) findViewById(R.id.Username)).getText()
				.toString();
		String password = ((EditText) findViewById(R.id.Password)).getText()
				.toString();
		String status = ((EditText) findViewById(R.id.Status)).getText()
				.toString();
		boolean avail = ((CheckBox) findViewById(R.id.setAvailable))
				.isChecked();
		String host = ((EditText) findViewById(R.id.Host)).getText().toString();
		Integer port;
		String service = ((EditText) findViewById(R.id.Service)).getText()
				.toString();
		Boolean sasl = ((CheckBox) findViewById(R.id.setSASL)).isChecked();
		try {
			port = Integer.parseInt(((EditText) findViewById(R.id.Port_no))
					.getText().toString());
		} catch (Throwable t) {
			port = 5222;
		}
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putString(TabFragment.USERNAME, username);
		editor.putString(TabFragment.PASSWORD, password);
		editor.putString(SelectConnActivity.STATUS, status);
		editor.putBoolean(SelectConnActivity.AVAIL, avail);
		editor.putString(SelectConnActivity.HOST, host);
		editor.putInt(SelectConnActivity.PORT, port);
		editor.putString(SelectConnActivity.SERVICE, service);
		editor.putBoolean(SelectConnActivity.SASL, sasl);
		editor.commit();
	}

}
