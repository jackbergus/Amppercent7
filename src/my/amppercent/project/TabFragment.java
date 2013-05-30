package my.amppercent.project;

import java.util.LinkedList;
import java.util.List;

import my.amppercent.types.State;
import my.amppercent.types.myATask;
import my.amppercent.adapters.AdapterChat;
import my.amppercent.adapters.ListViewAdapting;
import my.amppercent.remoteservice.IBinding;
import my.amppercent.remoteservice.XUser;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import my.amppercent.remoteservice.*;

/**
 * Definizione di un Fragment, ciascuno per ogni tab
 * 
 * @author jack
 * 
 */
public class TabFragment extends Fragment {

	/*
	 * Chiavi per le informazioni di inizializzazione nel Fragment
	 */
	public final static String CHATNAME = "chatname";
	public final static String CONNECTIONID = "connectionid";
	public final static String PASSWORD = "password";
	public final static String USERNAME = "username";
	public final static String USERS = "users";
	public final static String NICKNAME = "nickname";
	public final static String STATE = "state";

	// Stringhe per l'autenticazione dell'utente.
	private String nickname;
	private String chatname;
	private String username;
	private String connectionid;
	private String password;
	private String tabViewName;
	private XUser[] utenti;
	private Button messaggio;
	private View view;
	private Bundle bundle;
	private ReadMessageClass readmessage;
	private ListViewAdapting<IFMessage> mlv;
	private IBinder currentEditTextToken = null;
	private EditText et = null;

	private AdapterChat ac = null;
	private List<IFMessage> im = new LinkedList<IFMessage>();
	private IBinding myservice = null;
	private Connector mconn = null;
	private Intent j = null;

	public class Connector implements ServiceConnection {

		public void onServiceDisconnected(ComponentName name) {
			myservice = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			myservice = IBinding.Stub.asInterface(service);

			// All'atto dell'inizializzazione del servizio, si attivano anche i
			// messaggi
			try {
				myservice
						.available_chat(connectionid, password, chatname, true);
				startTasks();
			} catch (Throwable e) {
				e.printStackTrace();
				Log.d("Availing show chating", "error on sending reqest");
			}

			Log.d("SelectConnActivity", "ok");
		}
	}

	private boolean has_been_unshown = false;

	public void setTabViewName(String s) {
		this.tabViewName = s;
	}

	public String getTabViewName() {
		return this.tabViewName;
	}

	public void ShowCurrentKeyboard() {
		if (has_been_unshown) {
			final InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
			currentEditTextToken = et.getWindowToken();
		}

	}

	public void HideCurrentKeyboard() {
		if (this.currentEditTextToken != null) {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromInputMethod(this.currentEditTextToken, 0);
			this.has_been_unshown = true;
		}
	}

	private boolean started = false;

	public String getChatJID() {
		return this.chatname;
	}

	public String getNickname() {
		return this.nickname;
	}

	private void startTasks() {
		if (!started) {
			readmessage = new ReadMessageClass();
			readmessage.execute();
			started = true;
		}
	}

	/**
	 * Metodo statico di factory che permette di ottenere un riferimento ad un
	 * Fragment per la visualizzazione di una Label
	 * 
	 * @param label
	 *            La label associata
	 * @return Il Fragment associato al mese passato
	 */
	public static TabFragment getInstance(String label) {
		TabFragment fragment = new TabFragment();
		return fragment;
	}

	/**
	 * Effettua la creazione del fragment adoperando il Bundle che contiene
	 * tutti i parametri che sono necesari alla sua inizializazione
	 * 
	 * @argument bund Costruttore
	 */
	public static TabFragment chatFragment(Bundle bund) {
		TabFragment self = new TabFragment();
		if (bund.getString(CONNECTIONID) == null)
			Log.e("chatFragment", "null connectionid");
		if (bund.getString(CHATNAME) == null)
			Log.e("chatFragment", "null connectionid");
		self.quirckBundle(bund);
		self.setArguments(bund);
		return self;
	}

	public Bundle cloneState() {
		if (this.mlv == null) {
			IFMessage messages[] = new IFMessage[0];
			bundle.putParcelableArray(STATE, messages);
		} else {
			IFMessage messages[] = new IFMessage[this.mlv.getList().size()];
			messages = this.mlv.getList().toArray(messages);
			bundle.putParcelableArray(STATE, messages);
			if (!bundle.containsKey(NICKNAME))
				Log.e("cloneState::NICKNAME", "savings not contain nickname");
			else
				Log.d("cloneState::NICKNAME", "contains nickname");

		}
		// Contiene già id di connessione e pw
		return bundle;
	}

	public TabFragment quirckBundle(Bundle b) {
		if (b == null)
			b = this.bundle;
		if (b != null) {
			this.bundle = b;
			this.chatname = bundle.getString(CHATNAME);
			this.connectionid = bundle.getString(CONNECTIONID);
			this.password = bundle.getString(PASSWORD);
			this.username = bundle.getString(USERNAME);
			this.utenti = (XUser[]) bundle.getParcelableArray(USERS);
			this.nickname = bundle.getString(NICKNAME);

			if (this.nickname == null)
				Log.e("quirckBundle::NICKNAME", "received null nick");

			if (this.username == null) {
				this.username = this.connectionid.split("/")[1] + "@"
						+ this.connectionid.split("@")[0];
			}

			Log.w("TabFragment::onCreateView", (this.chatname == null ? "null"
					: this.chatname)
					+ " "
					+ (this.connectionid == null ? "null" : this.connectionid)
					+ " "
					+ (this.password == null ? "null" : this.password)
					+ " "
					+ (this.username == null ? "null" : this.username)
					+ " " + (this.utenti == null ? "null[]" : "objs()"));
		}
		return this;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		IFMessage msgs[];

		Log.d("FRAGMENT", "createView");
		if (bundle == null)
			bundle = getArguments();

		quirckBundle(bundle);

		/* Connessione AIDL */
		if (mconn == null)
			mconn = new Connector();
		if (j == null) {
			j = new Intent("my.amppercent.remoteservice.IBinding");
			j.putExtra("version", "1.0");
			getActivity().startService(j);
			getActivity().bindService(j, mconn, Activity.BIND_AUTO_CREATE);
		}

		if (bundle.containsKey(STATE))
			msgs = (IFMessage[]) bundle.getParcelableArray(STATE);
		else
			msgs = null;

		this.view = inflater.inflate(R.layout.express, container, false);

		// Impongo la visualizzazione della tastiera virtuale (Con i fragment
		// compariva solamente in 4, ed al click
		// con il dito.
		final InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY);

		this.et = (EditText) this.view.findViewById(R.id.Comment_newmessage);
		this.et.setFocusableInTouchMode(true);
		this.et.setSelected(true);
		this.et.setFocusableInTouchMode(true);
		this.et.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() != MotionEvent.ACTION_UP) {
					ShowCurrentKeyboard();
				}
				return false;
			}
		});

		/*
		 * Caricamento della vista all'interno del Fragment
		 */
		if (msgs != null) {

			for (IFMessage x : msgs)
				im.add(x);
			ac = new AdapterChat(this.getActivity(), R.layout.element_view, 0,
					im);
		} else
			ac = new AdapterChat(this.getActivity(), R.layout.element_view, 0,
					new LinkedList<IFMessage>());
		this.mlv = new ListViewAdapting<IFMessage>(this, R.id.listview3, ac);

		if (this.view == null)
			Log.e("Inflating Fragment", "null view");

		if (!this.has_been_unshown) {
			this.has_been_unshown = true;
			callWhenShowing();
		}

		return this.view;

	}

	public void onResume() {
		super.onResume();
		this.mlv.refresh();
	}

	public void callWhenShowing() {
		if (this.mlv != null) {
			ShowCurrentKeyboard();
			this.mlv.refresh();
		}
	}

	public void callWhenHidingOrShowing() {
		this.has_been_unshown = true;
		HideCurrentKeyboard();
	}

	public void onActivityCreated(Bundle bund) {
		Log.d("FRAGMENT", "onActivityCreated");
		super.onActivityCreated(bund);
		messaggio = (Button) getView().findViewById(R.id.Comment_button);
		messaggio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendMesage(((EditText) (getView()
						.findViewById(R.id.Comment_newmessage))).getText()
						.toString());
				((EditText) (getView().findViewById(R.id.Comment_newmessage)))
						.setText("");
			}
		});

	}

	/**
	 * Effettua unicamente la chiusura della chat. La chiusura della tab è
	 * demandata all'Acvitity principale (removeTab)
	 */
	public void closeChat() {

		if (readmessage != null)
			readmessage.kill();
		else
			Log.e("FRAGMENT", "closeChat: null readmessage");
		try {
			myservice.available_chat(this.connectionid, this.password,
					this.chatname, false);
			getActivity().unbindService(mconn);
		} catch (Throwable e) {
			Log.d("Availing show chating", "error on chat");
		}

	}

	/**
	 * Effettua l'invio del messaggio: questa funzione è scatenata dalla
	 * pressione del pulsante "Send"
	 * 
	 * @param text
	 */
	private void sendMesage(String text) {
		try {
			if (myservice == null)
				Log.e("sendMessage", "myservice is NUll");
			Log.d("sendMessage", this.connectionid + " " + this.password + " "
					+ this.chatname + " " + text);
			myservice.sendMessageTo(this.connectionid, this.password,
					this.chatname, text);
			mlv.add_and_update(new IFMessage(this.username, this.chatname, text));
		} catch (Throwable e) {
			e.printStackTrace();
			Log.d("Error sending mesage", text);
		}
	}

	public void sendFile(String filepath, String descr) {
		try {
			myservice.sendFile(this.connectionid, this.password, this.chatname,
					filepath, descr);
		} catch (Throwable e) {
		}
	}

	/**
	 * Classe che effettua la lettura sincrona dei nuovi messaggi della chat
	 * 
	 * @author giacomo
	 * 
	 */
	public class ReadMessageClass extends myATask<Void, String, Void> {

		public String[] updating(Void... params) {
			try {
				while (!State.main_is_visible)
					;
				IFMessage ifm = myservice.recvMessage(connectionid, password,
						chatname);
				if ((ifm != null) && (!State.main_is_visible))
					Log.w("READMESSAGECLASS", "will return null");
				if (ifm == null) {
					return null;
				} else {
					String str[] = ifm.getArray();
					Log.d("ReadMessageClass:updating",
							"message received not null " + str[0] + " "
									+ str[1]);
					return ifm.getArray();
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		}

		public Void finalResult(Void... messages) {
			return null;
		}

		public void progressUpdate(String... messages) {
			if (messages != null) {
				Log.e("ReadMessageClass:progressUpdate", messages[0] + " "
						+ messages[1]);
				mlv.add_and_update(new IFMessage(messages[0], username,
						messages[1]));
			}
		}

		public void beforeLoop(Void... params) {
			while (!State.main_is_visible)
				;
			return;
		}

	}

	public View getInnerView() {
		return this.view;

	}

}
