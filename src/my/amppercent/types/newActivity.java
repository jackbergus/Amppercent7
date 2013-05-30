package my.amppercent.types;

import my.amppercent.remoteservice.IBinding;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class newActivity extends Activity {

	private BroadcastReceiver breceiver = null;

	private String what_to_breceive = null;

	private boolean breceiver_setter = false;

	private boolean visible = false;

	private Bundle getBundle = null;

	private String aidl_class_name = null;

	private boolean could_set_visible = false;

	private Display display;

	private int rotation;

	// Connessione al servizio remoto per effettuare AIDL

	private IBinding myservice = null;// Identifica inoltre se il servizio è
										// attivo o meno

	private ServiceConnection mconn = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			Log.d("ServiceConnection@MAIN", "quitted");
			myservice = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			myservice = IBinding.Stub.asInterface(service);
			Log.d("ServiceConnection@MAIN", "ok");
			onAIDLConnected(name, service, myservice);
		}
	};
	private Intent j = null;

	public void onAIDLConnected(ComponentName name, IBinder service,
			IBinding myservice) {
		Log.d("newActivity", "onAIDLConnected");
	}

	protected void setBroadcastReceiver(BroadcastReceiver brec, String str) {
		this.breceiver = brec;
		this.what_to_breceive = str;
	}

	protected void setAIDL(Class<?> cl) {
		this.aidl_class_name = cl.getName();
	}

	/**
	 * Funzione di attivazione dei BroadcastReceivers
	 */
	protected void activateBReceiver() {
		if ((!this.breceiver_setter) && (this.breceiver != null)) {
			registerReceiver(this.breceiver, new IntentFilter(
					this.what_to_breceive));
			breceiver_setter = true;
		}
	}

	/**
	 * Inizializzazione del servizio remoto
	 */
	protected void startAIDL() {
		if (this.aidl_class_name != null) {
			if (j == null) {
				j = new Intent(this.aidl_class_name);
				j.putExtra("version", "1.0");
				startService(j);
				bindService(j, mconn, BIND_AUTO_CREATE);
			}
		}
	}

	protected void setCouldSetMainVisible(boolean value) {
		this.could_set_visible = value;
	}

	/**
	 * Terminazione del servizio remoto
	 */
	protected void stopAIDL() {
		if (this.mconn != null) {
			unbindService(this.mconn);
			this.mconn = null;
		}
	}

	/**
	 * Funzione di disattivazione dei BroadcastReceivers
	 */
	protected void deactivateBReceiver() {
		if (this.breceiver_setter) {
			unregisterReceiver(this.breceiver);
			this.breceiver_setter = false;
		}
	}

	/**
	 * Settaggio della visibiltà del form
	 * 
	 * @param val
	 */
	private void setVisiblity(boolean val) {
		/*
		 * NOTA: originariamente, quando si settava la visibilità, si aggiornava
		 * solamente this.visible
		 */
		if (this.could_set_visible) {
			State.main_is_visible = val;
		}
		this.visible = val;
	}

	/**
	 * Funzione da implementare per effettuare la inizializzazione. Verrà
	 * utilizzata all'atto della create;
	 */
	public void initializer() {
		Log.d("newActivity", "initializer");
	}

	/**
	 * Se l'activity è stata chiamata con un intent, allora dovrà essere
	 * eseguita l'azione opportuna
	 */
	public void onIntent(Intent i) {

	}

	public Bundle getSavedInstance() {
		return this.getBundle;
	}

	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		Log.d("newACTIVITY", "INITING");

		this.display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		this.rotation = display.getRotation();

		this.getBundle = savedInstance;
		// Chiamo la funzione di inizializzazione
		Log.d("INIT", "Initializing");
		initializer();

		Intent i = getIntent();
		if (i != null)
			onIntent(i);

		if (this.could_set_visible)
			State.main_is_active = true;

		// Attivazione di un BroadcastReceiver programmatico
		activateBReceiver();

	}

	/**
	 * Gestione dell'evento di rotazione
	 */
	protected void onRotate() {
		Log.d("newACTIVITY", "ROTATING");

	}

	protected void onPause() {
		super.onPause();
		Log.d("newACTIVITY", "PAUSING");
		if (this.rotation != display.getRotation())
			onRotate();
		setVisiblity(false);
		stopAIDL();
	}

	public boolean isRotating() {
		return (this.rotation != display.getRotation());
	}

	protected void onResume() {
		super.onResume();
		Log.d("newACTIVITY", "RESUMING");
		setVisiblity(true);
		startAIDL();
	}

	protected void onDestroy() {
		if (this.could_set_visible)
			State.main_is_active = false;
		deactivateBReceiver();
		super.onDestroy();

	}

	public IBinding getIBinding() {
		return this.myservice;
	}

	public boolean isVisible() {
		return this.visible;
	}

}
