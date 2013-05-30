package my.amppercent.chatrequest;

import my.amppercent.project.R;
import my.amppercent.project.TabFragment;
import my.amppercent.remoteservice.IBinding;
import my.amppercent.types.newActivity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AvailabilitySettings extends newActivity {

	public static final String STATUS = "STATUS";
	public static final String MODE = "MODE";
	public static final String AVAIL = "AVAIL";

	private String connectionid;
	private String password;
	private String old_status = null;

	private EditText StatusField;
	private CheckBox Available;
	private Spinner spinner;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_configuration);
		Bundle b = getIntent().getExtras();

		this.connectionid = b.getString(TabFragment.CONNECTIONID);
		this.password = b.getString(TabFragment.PASSWORD);

		StatusField = (EditText) findViewById(R.id.StatusField);
		Available = (CheckBox) findViewById(R.id.Available);
		spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.chatstate, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

	}

	@Override
	public void onAIDLConnected(ComponentName name, IBinder service, IBinding ib) {
		super.onAIDLConnected(name, service, ib);
		try {
			int i = 0;
			StatusField.setText(ib.getStatus(connectionid, password));
			Available.setChecked(ib.getAvail(connectionid, password));
			String res[] = getResources().getStringArray(R.array.chatstate);
			this.old_status = ib.getMode(connectionid, password);
			spinner.setSelection(0);
			for (i = 0; i < res.length; i++) {
				if (res[i].equals(this.old_status)) {
					spinner.setSelection(i);
				}
			}
		} catch (Throwable t) {
			Log.e("onAidlConnected", "Error");
		}
	}

	@Override
	public void onBackPressed() {
		IBinding ib = getIBinding();
		try {
			ib.setState(connectionid, password, Available.isChecked(),
					StatusField.getText().toString(), spinner.getSelectedItem()
							.toString());
		} catch (Throwable t) {
			Log.e("onBackPressed", "Error sending infos");
		}
		super.onBackPressed();
	}

	@Override
	public void initializer() {
		setAIDL(IBinding.class);
	}

}
