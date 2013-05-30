package my.amppercent.types;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * Questa classe serve per parametrizzare ed uniformare la gestione della Task
 * Asincrono
 * 
 * @author giacomo
 * 
 * @param <Params>
 *            Parametri dei valori in ingressi
 * @param <Progress>
 *            Valore della notifica ottenuta
 * @param <Result>
 *            Risultato finale del Task
 */
public abstract class myATask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private boolean doKill = true;

	/**
	 * Termina l'esecuzione della Task
	 */
	public final synchronized void kill() {
		this.doKill = false;
	}

	/**
	 * Effettua l'esecuzione della task
	 */
	@Override
	protected void onPreExecute() {
		Log.d("ReadMessageATask", "Reading from...");
	}

	/**
	 * Funzione che deve essere chiamata ad ogni iterazione del ciclo di
	 * richiesta
	 */
	public abstract Progress[] updating(Params... params);

	/**
	 * Funzione che restituisce il valore finale che deve essere fornito dalla
	 * funzione
	 * 
	 * @param messages
	 * @return
	 */
	public abstract Result finalResult(Params... messages);

	/**
	 * Funzione che consente di gestire gli elementi appena ottenuti dal
	 * servizio chiamato
	 * 
	 * @param messages
	 */
	public abstract void progressUpdate(Progress... messages);

	@Override
	protected final void onProgressUpdate(Progress... messages) {
		progressUpdate(messages);
	}

	/**
	 * Funzione che consente di effettuare una premessa al loop
	 * 
	 * @param params
	 */
	public abstract void beforeLoop(Params... params);

	@Override
	protected final Result doInBackground(Params... params) {
		Log.d("doinBackground", "Before loop");
		beforeLoop(params);
		while (doKill) {
			publishProgress(updating(params));
		}
		Log.d("doinBackground", "quit loop");
		return finalResult(params);
	}

}
