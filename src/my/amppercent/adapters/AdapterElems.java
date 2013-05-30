package my.amppercent.adapters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Estensione dell'Array Adapter, allo scopo di puntualizzare l'inserimento e la
 * cancellazione degli elementi
 * 
 * @author jack
 * 
 * @param <T>
 */
public class AdapterElems<T> extends ArrayAdapter<T> {

	private int resource;
	private Context context;
	private List<T> list;

	/**
	 * Restituisce la lista interna di tutti gli elementi contenuti all'interno
	 * della lista formatasi
	 * 
	 * @return
	 */
	public List<T> getList() {
		return this.list;
	}

	/**
	 * 
	 * @param context
	 *            Contesto di visualizzatore della lista
	 * @param resource
	 *            Risorsa Layout associata alla visualizzazione di un singolo
	 *            elemento.
	 * @param textViewResourceId
	 *            (Mantenuta per compatibilità con Android)
	 * @param objects
	 *            Lista di lementi che si vuole associare
	 */
	public AdapterElems(Context context, int resource, int textViewResourceId,
			List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		this.resource = resource;
		this.context = context;

		if (objects != null) {
			this.list = new LinkedList<T>();
			for (T x : objects)
				this.list.add(x);
		} else
			objects = null;
	}

	/**
	 * La particolarità di questo metodo è quella di trasformare l'array passato
	 * in lista, di modo da rendere sempre modificabili gli elementi contenuti
	 * 
	 * @param context
	 * @param resource
	 * @param textViewResourceId
	 * @param objects
	 */
	public AdapterElems(Context context, int resource, int textViewResourceId,
			T[] objects) {
		super(context, resource, textViewResourceId, objects);
		this.resource = resource;
		this.context = context;
		this.list = new LinkedList<T>();
		if (objects != null)
			for (T x : objects) {
				this.list.add(x);
			}
	}

	/**
	 * Questa funzione effettua l'override della preesistente lato Android.
	 * Tuttavia le sottoclassi dovranno ora richiamare sempre questa funzione.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = li.inflate(this.resource, null);
		return convertView;
	}

	@Override
	public void addAll(T... ls) {
		super.addAll(ls);
		for (T x : ls) {
			this.list.add(x);
		}

	}

	@Override
	public void addAll(Collection<? extends T> ls) {
		super.addAll(ls);
		this.list.addAll(ls);

	}

	@Override
	public void add(T x) {
		super.add(x);
		this.list.add(x);
	}

	@Override
	public void remove(T x) {
		super.remove(x);
		this.list.remove(x);
	}

}
