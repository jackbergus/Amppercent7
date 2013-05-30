package my.amppercent.project;

import my.amppercent.project.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity generica e parametrizzata per la richiesta di un unico testo con
 * conferma e cancellazione (back)
 * 
 * @author jack
 * 
 */
public class TextEntryActivity extends Activity {
	private EditText et;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_text_entry);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		// title
		try {
			String s = getIntent().getExtras().getString("title");
			if (s.length() > 0) {
				this.setTitle(s);
			}
		} catch (Exception e) {
		}
		// value

		et = ((EditText) findViewById(R.id.txtValue));

		// button
		((Button) findViewById(R.id.btnDone))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						executeDone(et.getText().toString());
					}

				});
		((Button) findViewById(R.id.btnCancel))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						executeDone(null);
					}

				});
	}

	public void onBackPressed() {
		executeDone(null);
		super.onBackPressed();
	}

	private void executeDone(String value) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("value", value);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

}
