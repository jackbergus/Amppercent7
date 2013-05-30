package my.amppercent.chatrequest;

import my.amppercent.project.TabFragment;
import my.amppercent.project.TextEntryActivity;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Questa classe implementa la scelta di quale connessione al server (sotto
 * forma di quale utente) utilizzare
 * 
 * @author giacomo
 * 
 */
public class serverActivity extends ListActivity {

	private int position = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String array[] = getIntent().getStringArrayExtra(
				SelectConnActivity.SERVERS);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, array);
		setListAdapter(adapter);
		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent stepNext = new Intent(this, TextEntryActivity.class);
		this.position = position;
		stepNext.putExtra("title",
				"Insert the password, cancel to keep the previous.");
		this.startActivityForResult(stepNext, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 0) {
			if (position == -1) {
				Intent i = getIntent();
				setResult(Activity.RESULT_CANCELED, i);
			} else {
				Intent i = getIntent();
				i.putExtra(SelectConnActivity.RESULT, getListView()
						.getItemAtPosition(position).toString());
				i.putExtra(TabFragment.PASSWORD, data.getStringExtra("value"));
				setResult(Activity.RESULT_OK, i);
			}
		}
		finish();
	}

}
