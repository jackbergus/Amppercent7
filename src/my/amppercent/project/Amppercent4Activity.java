package my.amppercent.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import my.amppercent.chatrequest.SelectConnActivity;
import my.amppercent.remoteservice.IFMessage;
import my.amppercent.remoteservice.XUser;
import my.amppercent.types.myNotification;
import my.amppercent.types.newActivity;
import my.amppercent.remoteservice.IBinding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Questa classe identifica solamente il gestore principale delle chat
 * 
 * @author giacomo
 */
public class Amppercent4Activity extends newActivity {

	/*
	 * Chiave per il salvataggio dello stato del Tab
	 */
	private final static String SELECTED_TAG_INDEX_PARAM = "SELECTED_TAG_INDEX_PARAM";

	// Sta ad indicare la lista completa di tutti i contenuti della chat
	private final static String FRAGMENT_ARRAY_STATE = "FRAGMENT_ARRAY_STATE";

	// private final static String FRAGMENT_TAG_ID = "FRAGMENT_TAG_ID";

	private final static String ROTATION = "ROTATION";

	private final static int ChoseSendFile = 12345;
	private final static int GiveSendFile = 12346;

	private List<TabFragment> fragmentlist = new LinkedList<TabFragment>();
	private List<TabFragment> unactive_fragmentlist = new LinkedList<TabFragment>();

	private Map<String, String> connidtopw = new HashMap<String, String>();

	private Map<String, LinkedList<String>> connidtoun = new HashMap<String, LinkedList<String>>();

	private Fragment firstfragment = null;

	private Fragment currentFragment;

	private ActionBar actionBar;

	private Activity self = this;

	private FragmentManager fmanage = null;

	private Intent defaultService;

	// Postpongo la visualizzazione delle Tab se non sono visibile
	private List<Tab> tablist = new LinkedList<Tab>();

	private Bundle getBundle = null;

	private Map<String, Bundle> waiting_tabs = new HashMap<String, Bundle>();

	/**
	 * Implementazione di TabListener checi permette di gestire la selezione dei
	 * diversi Tab
	 */
	private class MyTabListener implements TabListener {

		/*
		 * Fragment associato ad un Tab
		 */
		private TabFragment fragment;

		/**
		 * Crea un MyTabListener associato ad un particolare Tab
		 * 
		 * @param fragment
		 *            Riferimento al Fragment associato al Tag
		 */
		public MyTabListener(TabFragment fragment, String setTabName) {
			fragment.setTabViewName(setTabName);
			this.fragment = fragment;
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Vogliamo aggiungere alla visualizzazione il fragment corrente
			if (!fragmentlist.contains(fragment)) {
				fragmentlist.add(fragment);
				unactive_fragmentlist.remove(fragment);
				Fragment found = fmanage.findFragmentByTag(fragment
						.getChatJID());
				if (found == null) {
					// Associo ad ogni fragment un ID, corrispondente allo JID
					// della chat
					ft.add(R.id.anchor_container, fragment,
							fragment.getChatJID());
					ft.show(fragment);
				} else
					ft.show(found);
				Log.d("onTabSelected", "new Fragment");
			} else {
				Log.w("onTabSelected", "contains");
				if (fragment == firstfragment)
					Log.d("listener", "the first");
				ft.show(fragment);
			}
			fragment.callWhenShowing();
			currentFragment = fragment;
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// Vogliamo mantenere il fragment attivo, assieme ai suoi thread,
			// anche se non visibile
			if (currentFragment != null) {
				ft.hide(fragment);
				fragment.callWhenHidingOrShowing();
			}
		}

	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Starting the Remote Service
		this.defaultService = new Intent(this, IBindRemoteService.class);
		if (!hasServiceBeenActive())
			startService(defaultService);

		// Setting the local "dynamic" variables
		this.fmanage = getFragmentManager();
		this.actionBar = getActionBar();
		this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getBundle = savedInstanceState;

		if ((savedInstanceState != null)
				&& (savedInstanceState
						.containsKey(Amppercent4Activity.FRAGMENT_ARRAY_STATE))) {
			Log.w("viewNavigation", "there is State");
			Bundle array[] = null;
			try {
				array = (Bundle[]) savedInstanceState
						.getParcelableArray(Amppercent4Activity.FRAGMENT_ARRAY_STATE);
			} catch (Throwable t) {
			}
			if (array != null) {
				for (Bundle b : array) {
					definitiveUniqueMonoVisualization(b);
				}
			} else
				Log.e("viewNavigation", "array is null");
		} else
			Log.e("viewNavigation", "NULL!!");

		// Ripristino dello stato del tab
		if (actionBar.getTabCount() > 0) {
			int selectedTabIndex = 0;
			if (savedInstanceState != null) {
				selectedTabIndex = savedInstanceState.getInt(
						SELECTED_TAG_INDEX_PARAM, 0);
			}
			try {
				actionBar.setSelectedNavigationItem(selectedTabIndex);
			} catch (Throwable e) {
			}
		}

		/* Recovers previously opened chats */
		if (tablist.size() > 0) {
			for (Tab t : tablist) {
				actionBar.addTab(t);
			}
			tablist.clear();
		}

	}

	public boolean shallIKillOnExit() {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		return p.getBoolean("killonexit", false);
	}

	private static final String HAS_SERVICE_BEEN_ACTIVE = "has_service_been_active";

	public boolean hasServiceBeenActive() {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		return p.getBoolean(HAS_SERVICE_BEEN_ACTIVE, false);
	}

	public void setServiceStillActiveOnExit(boolean data) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor ed = prefs.edit();
		ed.putBoolean(HAS_SERVICE_BEEN_ACTIVE, data);
		ed.commit();
	}

	public void onBackPressed() {

		/* Saving chat to file */
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);

		new AlertDialog.Builder(this).setTitle("Really Exit?")
				.setMessage("Are you sure you want to exit?")
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

						boolean test = shallIKillOnExit();

						for (TabFragment f : fragmentlist) {
							Bundle b = f.cloneState();

							if (p.getBoolean("savechats", false)) {
								String path = Environment
										.getExternalStorageDirectory()
										.getAbsolutePath();
								if (!path.endsWith(File.pathSeparator))
									path += "/";
								path += "AmppercentChat/";
								path += b.getString(TabFragment.NICKNAME);
								new File(path).mkdirs();
								IFMessage messages[] = (IFMessage[]) b
										.getParcelableArray(TabFragment.STATE);
								String state = "";
								for (IFMessage x : messages) {
									state = state + x.getArray()[0] + ": "
											+ x.getArray()[1] + "\n";
								}
								appendChat(path, state);
							}

						}

						stopAIDL();
						deactivateBReceiver();

						closeTabAndFragments(true);

						if (test) {
							// Mi accerto di chiudere tutte le connessioni ed il
							// Service, direttamente.
							try {
								getIBinding().killme();
							} catch (Throwable t) {
							}
						}
						setServiceStillActiveOnExit(!test);

						Log.d("backpressed", "backpressed");
						Amppercent4Activity.super.onBackPressed();

					}
				}).create().show();

	}

	public static String outKeySet(Set<String> s) {
		if (s == null)
			return "";
		String o = "";
		for (String t : s)
			o = o + " " + t;
		return o;
	}

	public static String outValueSet(Bundle i) {
		String o = "";
		for (String k : i.keySet()) {
			String tmp = i.getString(k);
			o = o + " " + Nullify(tmp);
		}
		return o;
	}

	public void onResume() {
		super.onResume();
		Log.d("onResume::getBundle(Keys)", (getBundle == null ? "<null>"
				: outKeySet(getBundle.keySet())));
		Log.d("onResume::getBundle(Values)", (getBundle == null ? "<null>"
				: outValueSet(getBundle)));

		// Ora che è attivato il predicato "isVisible", provo ad inserire i Tab
		// pervenuti
		for (String cid : waiting_tabs.keySet()) {
			Log.d("el", cid);
			definitiveUniqueMonoVisualization(waiting_tabs.remove(cid));
		}
	}

	protected void onRotate() {
		super.onRotate();
		closeTabAndFragments(true);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.mainmenu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menu) {
		switch (menu.getItemId()) {
		case R.id.newconnection:
			doNewChat();
			break;
		case R.id.getbuddy:
			showConnect();
			break;
		case R.id.sendfile:
			AskSendFile();
			break;
		case R.id.closechat:
			removeCurrentTabAndFragment(true);
			break;
		case R.id.settings: {
			Intent i = new Intent(this, PrefActive.class);
			startActivity(i);
			break;
		}
		case R.id.showsoftk: {
			if (currentFragment != null)
				((TabFragment) currentFragment).ShowCurrentKeyboard();
			break;
		}
		default:
			Log.w("Settings", "TODO");
		}
		return true;
	}

	private boolean common_remove() {

		if ((currentFragment == null) || (fragmentlist.isEmpty())
				|| (actionBar.getTabCount() == 0))
			return false;

		// Nel Fragment corrente chiudo la chat corrente
		((TabFragment) currentFragment).closeChat();

		// Effettuo la transizione, chiudendo tutto.
		FragmentTransaction ft;
		ft = this.fmanage.beginTransaction();

		ft.hide(currentFragment);
		ft.remove(currentFragment);
		ft.commit();

		return true;
	}

	/**
	 * Effettua la rimozione di un tab selezionato dalla vista corrente'
	 */
	public void removeCurrentTabAndFragment(boolean remove_from_list) {

		Log.w("remCurrTAF", "closing chat");
		if (!common_remove())
			return;
		Tab tab = getActionBar().getSelectedTab();

		if (tab != null)
			getActionBar().removeTab(tab);

		if (currentFragment.equals(firstfragment)) {
			firstfragment = null;
			currentFragment = null;
		}

		if (!fragmentlist.isEmpty()) {
			firstfragment = fragmentlist.get(0);
		}

	}

	/**
	 * Effettua la rimozione di un tab selezionato dalla vista corrente'
	 */
	public void closeTabAndFragments(boolean remove_from_list) {

		Log.w("closeTAFs", "closing chat");
		if (!common_remove())
			return;
		currentFragment = null;

		for (Fragment del : fragmentlist) {
			Log.w("closeTabAndFragments", "closing");
			// Per ogni fragment, chiudo la chat associata
			Log.w("closeTAFs", "closing chat 2");
			((TabFragment) del).closeChat();
		}

		firstfragment = null;
		currentFragment = null;

	}

	/**
	 * Inserisce un nuovo Tab, associandovi un fragment
	 * 
	 * @param
	 * @param view_tab_text
	 * @param bundle
	 * @return
	 */
	public void TabAdapter(String view_tab_text, Bundle bund) {
		if (actionBar == null)
			actionBar = getActionBar();

		boolean b = (bund.containsKey(myNotification.IS_NOTIFICATION) && bund
				.getBoolean(myNotification.IS_NOTIFICATION));
		if (b)
			Log.d("TabAdapter", "inside the notification for " + view_tab_text);

		Tab tabby = actionBar.newTab().setText(view_tab_text);

		if (bund != null) {
			Log.d("TabAdapter::NICKNAME", view_tab_text);
			bund.putString(TabFragment.NICKNAME, view_tab_text);
		}
		TabFragment frag = (TabFragment) fmanage.findFragmentByTag(bund
				.getString(TabFragment.CHATNAME));
		if (frag == null)
			frag = TabFragment.chatFragment(bund);
		this.unactive_fragmentlist.add(frag);
		tabby.setTabListener(new MyTabListener(frag, view_tab_text));

		/* Shows the tab iif the view is available */
		if (isVisible())
			actionBar.addTab(tabby);
		else {
			Log.d("TabAdapter", "Posponing the tab creation");
			waiting_tabs.put(bund.getString(TabFragment.CONNECTIONID), bund);
		}
		if (firstfragment == null) {
			firstfragment = frag;
		}
	}

	private void AskSendFile() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent, ChoseSendFile);
		} catch (Throwable e) {
			// There is no handler
			Intent foo = new Intent(this, TextEntryActivity.class);
			foo.putExtra("title", "Give the path of the file");
			this.startActivityForResult(foo, GiveSendFile);
		}
	}

	protected void doNewChat() {
		SelectConnActivity.LaunchMeForNewConnection(self,
				SelectConnActivity.MONO_CHAT);
	}

	protected void showConnect() {
		SelectConnActivity.LaunchMeForShowConnection(self,
				SelectConnActivity.MONO_CHAT);
	}

	/**
	 * Effettua la scrittura di una stringa su di un file
	 * 
	 * @param path
	 * @param text
	 */
	public void appendChat(String path, String text) {
		File logFile = new File(path);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("onPause::size", Integer.toString(fragmentlist.size()));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Garantisco che, tra una creazione e l'altra, le mappe corrispondenti
		// siano vuote.
		clean_IPPW();

		for (TabFragment t : unactive_fragmentlist)
			fragmentlist.add(t);
		if (unactive_fragmentlist.size() > 0)
			unactive_fragmentlist.clear();

		// Salvataggio delle chat all'interno dei files.
		if (fragmentlist.size() > 0) {
			Bundle states[] = new Bundle[fragmentlist.size()];
			String fragIDS[] = new String[fragmentlist.size()];
			int i = 0;
			for (TabFragment f : fragmentlist) {
				fragIDS[i] = f.getChatJID();

				Bundle b = f.cloneState();
				b.putBoolean(ROTATION, isRotating());
				if (!b.containsKey(TabFragment.NICKNAME))
					Log.e("onSaveInstanceState::NICKNAME", "not contain");
				states[i++] = b;

			}
			outState.putParcelableArray(FRAGMENT_ARRAY_STATE,
					(Parcelable[]) states);
		} else
			Log.e("onSave", "Not saving");

		int selectedTabIndex = getActionBar().getSelectedNavigationIndex();
		outState.putInt(SELECTED_TAG_INDEX_PARAM, selectedTabIndex);

	}

	/**
	 * Aggiornamento delle liste di presenza
	 * 
	 * @param connectionid
	 * @param username
	 * @param password
	 * @return Restituisce true se non era presente, altrimenti false
	 */
	public boolean update_IPPW(String connectionid, String username,
			String password) {
		if ((connectionid == null) || (username == null) || (password == null))
			return false;
		if (!connidtopw.containsKey(connectionid)) {
			connidtopw.put(connectionid, password);
		}
		LinkedList<String> ls;
		if (!connidtoun.containsKey(connectionid)) {
			ls = connidtoun.remove(connectionid);
		} else
			ls = new LinkedList<String>();
		if (ls == null)
			ls = new LinkedList<String>();
		if (ls.contains(username)) {
			connidtoun.put(connectionid, ls);
			return false;
		} else {
			ls.add(username);
			connidtoun.put(connectionid, ls);
			return true;
		}
	}

	public void clean_IPPW() {
		if (connidtopw.size() > 0)
			connidtopw.clear();
		if (connidtoun.size() > 0)
			connidtoun.clear();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if ((data != null)
				&& ((requestCode == SelectConnActivity.MONO_CHAT) || (requestCode == SelectConnActivity.MONO_CHAT))) {

			String connectionid = data.getStringExtra(TabFragment.CONNECTIONID);
			String password = data.getStringExtra(TabFragment.PASSWORD);

			Parcelable[] elem = data.getParcelableArrayExtra(TabFragment.USERS);
			if (elem != null) {
				for (Parcelable x : elem) {
					if (x instanceof XUser) {
						Bundle b = new Bundle();
						XUser u = (XUser) x;
						String nick = u.getNickname();
						if (nick == null)
							nick = u.getJid();
						b.putString(TabFragment.CHATNAME, u.getJid());
						b.putString(TabFragment.NICKNAME, nick);
						b.putString(TabFragment.CONNECTIONID, connectionid);
						b.putString(TabFragment.PASSWORD, password);
						definitiveUniqueMonoVisualization(b);
					}
				}
			}

		} else if (data != null) {
			switch (requestCode) {
			case ChoseSendFile:
				Log.d("onActivityResult::path", data.getData().getPath());
				((TabFragment) currentFragment).sendFile(data.getData()
						.getPath(), data.getData().getUserInfo());
				break;
			case GiveSendFile:
				((TabFragment) currentFragment).sendFile(
						data.getStringExtra("value"), "");
			default:
				break;
			}
		} else
			Toast.makeText(getApplicationContext(), "No user selected",
					Toast.LENGTH_SHORT).show();
	}

	@Override
	public void initializer() {
		setCouldSetMainVisible(true);
		BroadcastReceiver brodrecv = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				Log.d("Amppercent4Activity::initializer",
						"Received Intent from Broadcast");
				uniform_toGUI(true, intent);
			}
		};
		setBroadcastReceiver(brodrecv, myNotification.BROADCAST_CHAT_REQ_STRING);
		setAIDL(IBinding.class);
	}

	@Override
	public void onIntent(Intent i) {
		Log.d("Amppercent4Activity::onIntent",
				"Received Intent from Launched intent");
		if ((i.hasExtra(myNotification.SECOND_BUNDLE))
				&& (i.getAction()
						.equals(myNotification.BROADCAST_CHAT_REQ_STRING))) {
			uniform_toGUI(true, i);
		}
	}

	/**
	 * Effettua la visualizzazione come tab+fragment di un solo chatteur
	 * 
	 * @param connid
	 * @param connpw
	 * @param chatwith
	 * @param msgs
	 */
	public void definitiveUniqueMonoVisualization(Bundle msgs) {

		final String jidwith = msgs.getString(TabFragment.CHATNAME);
		final String nickname = (msgs.containsKey(TabFragment.NICKNAME) ? msgs
				.getString(TabFragment.NICKNAME) : jidwith);
		final Bundle messages = msgs;
		final String id = msgs.getString(TabFragment.CONNECTIONID);
		final String pw = msgs.getString(TabFragment.PASSWORD);
		final boolean r = (((msgs.containsKey(ROTATION) && msgs
				.getBoolean(ROTATION))));
		final boolean b = (msgs.containsKey(myNotification.IS_NOTIFICATION) && msgs
				.getBoolean(myNotification.IS_NOTIFICATION));

		Log.d("Ammpercent4Activity::prepareTab(keys)", outKeySet(msgs.keySet()));
		Log.d("Ammpercent4Activity::prepareTab(values)", outValueSet(msgs));
		if (r)
			Log.d("Ammpercent4Activity::prepareTab",
					"The screen has been rotated or a new message is incoming...");

		update_IPPW(id, jidwith, pw);

		AsyncTask<Void, Void, Void> ata = new AsyncTask<Void, Void, Void>() {

			protected Void doInBackground(Void... params) {
				// Attende per il binding con il RemoteService
				Log.d("ATask", "Waiting for grape to make figs...");
				while (getIBinding() == null) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}

			protected void onPostExecute(Void nullo) {
				IBinding myservice = getIBinding();
				try {

					String thenightfly = myservice.getNickname(id, pw, jidwith);
					if (thenightfly == null)
						thenightfly = jidwith;

					Log.d("onPostExecute::thenightfly", Nullify(thenightfly));
					Log.d("onPostExecute::jidwith", Nullify(jidwith));
					Log.d("onPostExecute::nickname", Nullify(nickname));

					// Se ruoto, devo ricreare le tab che vanno distrutte (nella
					// rotazione)
					/*
					 * Altro caso per il quale la chat esiste già (creata a lato
					 * servizio ma non a lato GUI) è l'arrivo di Broadcast o di
					 */
					if (r || b) {
						myservice.available_chat(id, pw, jidwith, true);
						TabAdapter(thenightfly, messages);// nickname
					} else
					// altrimenti devo creare la tab solamente se la chat non
					// esiste già
					if (!myservice.available_chat(id, pw, jidwith, true)) {
						if (!myservice.ChatWith(id, pw, jidwith))
							Log.w("definitiveUniqueMonoVisualization",
									"is not available, but no creable");
						// Per velocità, faccio visualizzare prima la tab
						TabAdapter(thenightfly, messages);
						myservice.available_chat(id, pw, jidwith, true);
						// nickname
					} else
						Log.d("definitiveUniqueMonoVisualization", "available");
				} catch (Throwable t) {
					Log.e("definitiveUniqueMonoVisualization",
							"Cannot execute a method");
					t.printStackTrace();
				}
			}
		};

		ata.execute();

	}

	/**
	 * 
	 * @param isIntent
	 *            Se il valore è vero, allora lo si considera come arrivato da
	 *            un'attivazione da Intent, altrimenti come la restituzione di
	 *            un Broadcast
	 * @param i
	 *            Argomento ricevuto dalla destinazione "remota"
	 * @return
	 */
	private void uniform_toGUI(boolean isIntent, Intent i) {

		String chatid = i.getStringExtra(myNotification.SECOND_BUNDLE);

		// Se lo ricevo da un broadcast, allora devo controllare che la chat
		// Non esista già nella gui: controllo da lista, poiché potrebbe anche
		// darsi che il thread non abbia ancora settato available_chat a true
		i.putExtra(myNotification.IS_NOTIFICATION, true);
		i.putExtra(TabFragment.CHATNAME, chatid);
		Bundle b = i.getExtras();

		waiting_tabs.put(chatid, b);

	}

	/**
	 * Se la stringa è null, allora restituisce una stringa "<null>"
	 * 
	 * @param s
	 * @return
	 */
	public static String Nullify(String s) {
		if (s == null)
			return "<null>";
		else
			return s;
	}

}