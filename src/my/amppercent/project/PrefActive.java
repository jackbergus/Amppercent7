package my.amppercent.project;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Activity per la gestione delle preferenze
 * 
 * @author jack
 * 
 */
public class PrefActive extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}

}
