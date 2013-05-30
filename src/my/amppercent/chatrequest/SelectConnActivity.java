package my.amppercent.chatrequest;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.Toast;

import java.util.*;

import my.amppercent.adapters.ListView_XUser;
import my.amppercent.project.*;
import my.amppercent.remoteservice.*;
import my.amppercent.types.*;

/**
 * Activity necessaria per effettuare l'instaurazione di una nuova connessione
 * con il server: questa schermata, successivamente, mostrerà la lista degli
 * utenti desiderata
 * 
 * @author giacomo
 * 
 */
public class SelectConnActivity extends newActivity {

	private static int LAYOUT = R.layout.listview;
	private static int TENTA = 2;
	private Activity self = SelectConnActivity.this;

	public static final String RESULT = "result";
	public static final String SERVERS = "servers";
	public static final String STATUS = "status";
	public static final String AVAIL = "avail";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String SASL = "sasl";
	public static final String SERVICE = "service";

	private ListView_XUser xulv = null;
	private String connection = null;

	private String username = null;
	private String password = null;
	public boolean hastowai = true;

	private ProgressDialog pdialog;

	private void pdialog_show() {
		if (pdialog != null) {
			pdialog.cancel();
			pdialog.dismiss();
			pdialog = null;
		}
		if ((pdialog == null) && (this.hastowai)) {
			pdialog = ProgressDialog.show(this, "Amppercent v.5",
					"Loading buddies", true, true);
		}

	}

	private void pdialog_close() {
		if (pdialog != null) {
			pdialog.cancel();
			try {
				pdialog.dismiss();
			} catch (Throwable t) {
			}
			pdialog = null;
			this.hastowai = true;
		}
	}

	private void buddy_view_run() {
		if (buddy_view != null)
			return;

		buddy_view = new myATask<Void, Void, Void>() {

			private boolean succeed = false;

			@Override
			public Void[] updating(Void... params) {
				if (!succeed) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else
					succeed = false;
				return null;
			}

			@Override
			public Void finalResult(Void... messages) {
				pdialog_close();
				return null;
			}

			@Override
			public void progressUpdate(Void... messages) {
				viewContactList(connection);
			}

			@Override
			public void beforeLoop(Void... params) {
				int trycount = 0;

				while (getIBinding() == null) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (trycount >= TENTA) {
						pdialog_close();
						this.kill();
						Toast.makeText(self, "No connection available",
								Toast.LENGTH_SHORT).show();
						return;
					} else
						trycount++;
				}

			}
		};
		buddy_view.execute();
	}

	private void buddy_view_stop() {
		if (buddy_view != null)
			buddy_view.kill();
		buddy_view = null;
	}

	private myATask<Void, Void, Void> buddy_view = null;

	// Richiesta di avvio dell'attività corrente per monochat
	public static final int MONO_CHAT = 0x123456;

	// Richiesta di una nuova connessione lato server
	public static final int NEW_CONNECTION = 0x654321;

	// Richiesta di ottenere una nuova connessione lato server
	public static final int GET_CONNECTION = 0x666999;

	// Richiesta di chiudere una connessione lato server
	public static final int DEL_CONNECTION = 0x333444;

	/**
	 * Metodo statico per effettuare l'invocazione della seguente attività
	 * 
	 * @param view
	 *            Chiamante dell'attività corrente
	 */
	public static void LaunchMeForNewConnection(Activity view, int WHATTODO) {
		Intent intent = new Intent(view, SelectConnActivity.class);
		intent.putExtra("action", NEW_CONNECTION);
		view.startActivityForResult(intent, WHATTODO);
	}

	public static void LaunchMeForShowConnection(Activity view, int WHATTODO) {
		Intent intent = new Intent(view, SelectConnActivity.class);
		intent.putExtra("action", GET_CONNECTION);
		view.startActivityForResult(intent, WHATTODO);
	}

	/**
	 * Lancia l'attività per effettuare una nuova connessione con il server.
	 */
	public void newConnection() {
		Intent i = new Intent(this, connActivity.class);
		startActivityForResult(i, NEW_CONNECTION);
	}

	private void manageSavedInstance() {

		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		this.connection = sp.getString(TabFragment.CONNECTIONID, null);
		this.password = sp.getString(TabFragment.PASSWORD, null);

		Log.d("manage:connection", (connection == null ? "<null>" : connection));
		Log.d("manage:password", (password == null ? "<null>" : password));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("onCreate", "creating");
		setContentView(LAYOUT); // Layout contenente la listview
		this.setTitle("Selezionare con chi chattare");

		manageSavedInstance();

		if (!hastowai) {
			if (connection == null)
				hastowai = true;
		} else {
			Intent i = getIntent();
			if (i != null) {
				switch (i.getIntExtra("action", -1)) {
				case NEW_CONNECTION:
					newConnection();
				}
			}
		}

	}

	/**
	 * Funzione di coonnessione con un nuovo server
	 * 
	 * @param data
	 *            Informazioni ottenute come risultato da un precedente Intent
	 */
	private String connect(Intent data) {
		if ((data == null) || (data.getStringExtra("host") == null)
				|| (data.getIntExtra("port", -1) == -1)
				|| (data.getStringExtra("service") == null)
				|| (data.getStringExtra(TabFragment.USERNAME) == null)
				|| (data.getStringExtra(TabFragment.PASSWORD) == null)
				|| (data.getStringExtra("status") == null))
			return null;

		this.username = data.getStringExtra(TabFragment.USERNAME);
		this.password = data.getStringExtra(TabFragment.PASSWORD);

		try {
			buddy_view_run();
			IBinding myservice = getIBinding();
			connection = null;
			if (myservice == null)
				Toast.makeText(this, "Sei un coglione", Toast.LENGTH_SHORT)
						.show();
			else
				connection = myservice.connect_n_login(
						data.getStringExtra("host"),
						data.getIntExtra("port", 5222),
						data.getStringExtra("service"),
						data.getStringExtra(TabFragment.USERNAME),
						data.getStringExtra(TabFragment.PASSWORD),
						data.getBooleanExtra("sasl", false),
						data.getStringExtra("status"),
						data.getBooleanExtra("avail", true));
		} catch (RemoteException e) {
			e.printStackTrace();
			this.hastowai = false;
			pdialog_close();
			buddy_view_stop();
			Toast.makeText(this, "Error connecting/logging to server",
					Toast.LENGTH_SHORT).show();

			connection = null;

		}
		pdialog_close();
		if (connection == null) {
			this.hastowai = false;
			pdialog_close();
			buddy_view_stop();
			Toast.makeText(getApplicationContext(),
					"Server unreachable: cannot establish connection",
					Toast.LENGTH_SHORT).show();

		}
		return connection;
	}

	/**
	 * Funzione per la visualizzazione della lista dei contatti
	 * 
	 * @param result
	 */
	private void viewContactList(String result) {

		if (result != null) {

			pdialog_close();

			List<XUser> ciao;
			try {
				IBinding myservice = getIBinding();
				if (myservice == null) {
					Toast.makeText(this, "Error getting AIDL connection",
							Toast.LENGTH_SHORT).show();
					return;
				}
				ciao = myservice.getbuddyList(result, this.password);
			} catch (RemoteException e) {
				e.printStackTrace();
				Toast.makeText(this, "Error getting buddylist",
						Toast.LENGTH_SHORT).show();
				return;
			}
			List<XUser> metabool = new LinkedList<XUser>();
			for (XUser x : ciao) {
				if (x != null)
					metabool.add(x);
			}
			XUser arr[] = new XUser[metabool.size()];
			if (metabool.size() == 0)
				Log.w("SelectConnA::viewContactList", "null ciao");
			else
				metabool.toArray(arr);

			// Listview: elemento dentro al layout lineare chiamato listview
			if (xulv == null) {
				xulv = new ListView_XUser(R.id.listview, R.layout.user, this,
						new XUser[] {});

			} else {
				xulv.clear();
				Log.d("SelectConnA::viewContactList",
						Integer.valueOf(arr.length).toString());
				xulv.add_and_update(arr);
			}

		} else {
			pdialog_close();
			buddy_view_stop();
			Toast.makeText(this, "No server connection available!!",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("onResume", "resuming");
		this.hastowai = true;
		buddy_view_run();
		pdialog_show();
	}

	/**
	 * Funzione di ritorno dalla chiamata di Intent con risultato
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("onActivityResult", "return call");
		switch (requestCode) {
		case NEW_CONNECTION:
			if (data != null)
				connection = connect(data);
			break;
		case GET_CONNECTION:
			/* Contiene il nome della connessione */
			if (data != null) {
				if (resultCode != Activity.RESULT_CANCELED) {
					String newpw = data.getStringExtra(TabFragment.PASSWORD);
					if (newpw != null)
						this.password = newpw;
					connection = data.getStringExtra(RESULT);
					buddy_view_run();
				}
			}
			break;
		case DEL_CONNECTION:
			if (data != null) {
				if (resultCode != Activity.RESULT_CANCELED) {
					String connection = data.getStringExtra(RESULT);
					String newpw = data.getStringExtra(TabFragment.PASSWORD);
					if ((newpw != null) && (connection != null)) {
						try {
							getIBinding().kill_connection(connection, newpw);
						} catch (RemoteException e) {
							Log.e("DEL_CONNECTION", "unable to kill connection");
						}
					}
				}
			}
			pdialog_close();
			break;
		default:
			Log.e("StartActivity", "no req code matching");
		}
	}

	/**
	 * Evento chiamato alla prima creazione dell'option menu
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.optmenu, menu);
		return true;
	}

	/**
	 * Permette di effettuare la selezione del comando, andando a verificare
	 * quale elemento è stato cliccato
	 */
	public boolean onOptionsItemSelected(MenuItem menu) {

		super.onOptionsItemSelected(menu);

		switch (menu.getItemId()) {

		// Richiedo di effettuare una nuova connessione con il server
		case R.id.newconnection:
			newConnection();
			break;

		// Richiedo di selezionare una connessione preesistente
		case R.id.removeconnection:
		case R.id.getconnections: {
			Intent i;
			/* Ottenere la lista delle connessioni presenti */
			List<String> slist = null;
			try {
				IBinding myservice = getIBinding();
				if (myservice == null) {
					if (myservice == null)
						Toast.makeText(this, "Unknown error?!?!",
								Toast.LENGTH_SHORT).show();
					return false;
				}
				slist = myservice.getConnectionList();
			} catch (RemoteException e) {
				e.printStackTrace();
				Toast.makeText(this, "Error getting Connection list",
						Toast.LENGTH_SHORT).show();
			}
			String arr[] = new String[slist.size()];
			slist.toArray(arr);

			if (slist.size() == 0) {
				Toast.makeText(this, "No connection available",
						Toast.LENGTH_SHORT).show();
			} else {
				i = new Intent(this, serverActivity.class);
				i.putExtra(SERVERS, arr);
				startActivityForResult(
						i,
						(menu.getItemId() == R.id.getconnections ? GET_CONNECTION
								: DEL_CONNECTION));
			}
		}
			break;

		case R.id.change_status_user: {
			Intent i = new Intent(this, AvailabilitySettings.class);
			if ((this.connection == null) || (this.password == null))
				Toast.makeText(this, "No connection available",
						Toast.LENGTH_SHORT).show();
			else {
				i.putExtra(TabFragment.CONNECTIONID, this.connection);
				i.putExtra(TabFragment.PASSWORD, this.password);
				startActivity(i);
			}
		}
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onBackPressed() {

		buddy_view_stop();
		pdialog_close();

		Intent i = getIntent();
		List<XUser> xul;

		if (xulv != null) {
			xul = xulv.getChatWith();
			Log.w("onBack", Integer.valueOf(xul.size()).toString());
		} else {
			Log.w("onBack", "xul is null");
			xul = null;
		}

		XUser[] xua;
		if (xul != null) {
			xua = new XUser[xul.size()];
			xua = xul.toArray(xua);
		} else
			xua = null;

		if (xua != null)
			Log.d("onBack", Integer.valueOf(xua.length).toString());
		i.putExtra(TabFragment.USERS, (Parcelable[]) xua);
		i.putExtra(TabFragment.USERNAME, this.username);
		i.putExtra(TabFragment.PASSWORD, this.password);
		i.putExtra(TabFragment.CONNECTIONID, this.connection);
		setResult(RESULT_OK, i);
		super.onBackPressed();
	}

	@Override
	public void onPause() {
		pdialog_close();
		SharedPreferences.Editor edit = getPreferences(MODE_PRIVATE).edit();
		edit.putString(TabFragment.CONNECTIONID, this.connection);
		edit.putString(TabFragment.PASSWORD, this.password);
		edit.commit();
		super.onPause();
	}

	@Override
	public void initializer() {
		setAIDL(IBinding.class);
	}

}
