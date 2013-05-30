package my.amppercent.chattables;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import my.amppercent.remoteservice.XUser;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;

import android.util.Log;

/**
 * Implementa la gestione dei contatti lato XMPP
 * 
 * @author giacomo
 * 
 */
public class XRoster extends Observable {

	/**
	 * @author jack
	 */
	enum Status {
		entriesAdded, entriesUpdated, entriesRemoved, presenceChanged;
	}

	private Map<String, XUser> jid_nameview;
	private Roster roster;
	private Status state;

	public Roster getRoster() {
		return this.roster;
	}

	public Presence getPresence(String jid) {
		Presence presence = this.roster.getPresence(jid);
		return presence;
	}

	public Map<String, XUser> get_buddyList() {
		return this.jid_nameview;
	}

	public Collection<RosterEntry> get_buddyList_fromRoster() {
		this.roster.reload();
		return this.roster.getEntries();
	}

	public String getNickname(String jid) {
		return this.roster.getEntry(jid).getName();
	}

	public Map<String, XUser> get_buddyList2() {

		if (jid_nameview.size() > 0)
			jid_nameview.clear();

		try {
			// Se si toglie la connessione dal server, questo metodo "esplode"
			this.roster.reload();
		} catch (Throwable e) {
			/*
			 * Voglio mantenere comunque in memoria all'interno del roster i
			 * nomi precedentemente ottenuti: quindi continuo nell'esecuzione
			 * del codice
			 */
		}

		for (RosterEntry x : this.roster.getEntries()) {

			XUser u = new XUser(x.getUser(), x.getName());

			if (x.getStatus() == ItemStatus.SUBSCRIPTION_PENDING)
				Log.d("pending subscription", x.getName());
			if (x.getStatus() == ItemStatus.UNSUBSCRIPTION_PENDING)
				Log.d("pending unsubscription", x.getName());
			Presence presence = this.roster.getPresence(x.getUser());

			Presence.Mode setpresence;

			if ((presence.isAvailable()) && (presence.getMode() == null))
				setpresence = Presence.Mode.available;
			else
				setpresence = presence.getMode();

			u.setState(setpresence);
			if (jid_nameview.containsKey(x.getUser()))
				jid_nameview.remove(x.getUser());
			jid_nameview.put(x.getUser(), u);
		}
		return this.jid_nameview;
	}

	public void setSubscriptionMode(SubscriptionMode s) {
		this.roster.setSubscriptionMode(s);
	}

	public SubscriptionMode getSubscriptionMode() {
		return this.roster.getSubscriptionMode();
	}

	/**
	 * @return
	 * @uml.property name="state"
	 */
	public Status getState() {
		return this.state;
	}

	public XRoster(Roster r, SubscriptionMode s) {
		this.roster = r;
		setSubscriptionMode(s);
		this.roster.addRosterListener(new RosterListener() {

			public void entriesAdded(Collection<String> addresses) {
				state = Status.entriesAdded;
				for (String x : addresses) {
					if (jid_nameview.get(x) == null) {
						roster.reload();

						XUser user = new XUser(x, roster.getEntry(x).getName());

						Presence.Mode setpresence;
						Presence presence = roster.getPresence(x);
						if ((presence.isAvailable())
								&& (presence.getMode() == null))
							setpresence = Presence.Mode.available;
						else
							setpresence = presence.getMode();

						user.setState(Presence.Mode.available);
						jid_nameview.put(x, user);
						Log.d(roster.getEntry(x).getName() + " add",
								setpresence.toString());
					}
				}

			}

			public void entriesUpdated(Collection<String> addresses) {
				state = Status.entriesUpdated;

				for (String x : addresses) {
					Presence presence = roster.getPresence(x);
					Presence.Mode setpresence;
					if ((presence.isAvailable())
							&& (presence.getMode() == null))
						setpresence = Presence.Mode.available;
					else
						setpresence = presence.getMode();

					Log.d(roster.getEntry(x).getName() + " up",
							setpresence.toString());
					jid_nameview.get(x).setState(setpresence);
				}

			}

			public void entriesDeleted(Collection<String> addresses) {
				state = Status.entriesUpdated;
				for (String x : addresses) {
					jid_nameview.remove(x);
					Log.d("entriesDeleted", roster.getEntry(x).getName());
				}

			}

			public void presenceChanged(Presence presence) {
				if ((presence == null) || (roster == null))
					return;

				Presence.Mode setpresence;
				RosterEntry re = roster.getEntry(presence.getFrom());
				if (re == null)
					return;

				if ((presence.isAvailable()) && (presence.getMode() == null))
					setpresence = Presence.Mode.available;
				else
					setpresence = presence.getMode();

				state = Status.presenceChanged;
				String from = presence.getFrom();
				jid_nameview.get(from).setState(setpresence);
				jid_nameview.get(from).setStatus(presence.getStatus());
			}

		});
		this.jid_nameview = new HashMap<String, XUser>();
	}

	public XRoster(Roster r) {
		this(r, SubscriptionMode.accept_all);

	}

	public XRoster(Roster r, Observer o) {
		this(r);
		this.addObserver(o);
	}

	public XUser addContact(String jid, String name) {
		return addContact(jid, name, null);
	}

	public XUser addContact(String jid, String name, String groups[]) {
		List<String> ls = new LinkedList<String>();
		for (String x : groups) {
			if (this.roster.getGroup(x) != null) {
				ls.add(x);
			}
		}
		try {
			if (ls.size() > 0) {
				String[] fruits = new String[ls.size()];
				this.roster.createEntry(jid, name, ls.toArray(fruits));
			} else
				this.roster.createEntry(jid, name, null);
			XUser u = new XUser(jid, name);
			jid_nameview.put(jid, u);
			return u;
		} catch (XMPPException e) {
			return null;
		}
	}

	public void addGroup(String name) {
		if (this.roster.getGroup(name) == null) {
			this.roster.createGroup(name);
		}
	}

}
