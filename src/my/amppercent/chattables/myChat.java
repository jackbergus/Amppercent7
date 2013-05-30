package my.amppercent.chattables;

import java.util.LinkedList;
import java.util.Queue;
import my.amppercent.types.myNotification;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.util.Log;

/**
 * Chat contentente la coda dei messaggi in arrivo
 * 
 * @author jack
 * 
 */
public class myChat implements UberChat {

	private Chat gat;

	private boolean cvisible;

	private MessageListener ml = null;

	private boolean quitted = false;

	/**
	 * Questa struttura dati non è concorrente in quanto sarà già protetta
	 * all'interno di una mappa al livello soprastante
	 */
	private Queue<Message> messages;
	private String con;
	private XConnect conn;

	public myChat(XConnect cona, ChatManager cm, String with, Context ct,
			final String password) {
		this.messages = new LinkedList<Message>();
		this.con = with;
		this.conn = cona;
		this.cvisible = true; // Se io voglio creare la chat, allora divento
								// visibile
		final Context contesto = ct;
		Log.d("Created Chat cm", "£");
		this.ml = new MessageListener() {

			public void processMessage(Chat arg0, Message m) {
				Log.d("myChat::() (ChatManager cm) arrived", m.getBody());
				messages.add(m);
				if (!cvisible) {
					myNotification w = new myNotification(contesto, null, m
							.getFrom().split("/")[0],
							myNotification.BROADCAST_CHAT_REQUEST);
					if (conn.getId() == null)
						Log.d("myChat::() (ChatManager cm)",
								"conn.getId() is null");
					else
						Log.w("myChat::() (ChatManager cm)", "conn ok");
					w.setConnPassword(conn.getId(), password);
					w.notify_case(m.getFrom().split("/")[0], m.getBody());
				}
				Log.d("myChat::() (ChatManager cm)", "chat2 with "
						+ m.getFrom().split("/")[0] + " " + m.getBody());
			}
		};
		this.gat = cm.createChat(with, ml);
	}

	public myChat(XConnect cona, Chat c, Context ct, final String password) {
		this.messages = new LinkedList<Message>();
		this.cvisible = false;
		this.con = c.getParticipant();
		this.conn = cona;
		this.gat = c;
		final Context contesto = ct;
		Log.w("Created Chat c", "£");
		this.gat.addMessageListener(new MessageListener() {

			public void processMessage(Chat arg0, Message m) {
				Log.d("myChat::() (Chat c) arrived", m.getBody());
				messages.add(m);
				if (!cvisible) {
					myNotification w = new myNotification(contesto, null, m
							.getFrom().split("/")[0],
							myNotification.BROADCAST_CHAT_REQUEST);
					if (conn.getId() == null)
						Log.d("myChat::() (Chat c)", "conn.getId() is null");
					else
						Log.w("myChat::() (Chat c)", "conn ok");
					w.setConnPassword(conn.getId(), password);
					w.notify_case(m.getFrom().split("/")[0], m.getBody());
				}
				Log.d("myChat::() (Chat c)",
						"chat2 with " + m.getFrom().split("/")[0] + " "
								+ m.getBody());
			}
		});
	}

	public boolean send(String msg) {
		if (gat == null)
			return false;
		try {
			Log.d("myChat::send", "sending message");
			this.gat.sendMessage(msg);
		} catch (XMPPException e) {
			Log.e("Exception in chat with " + this.con, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public Message recv(boolean wait) {
		return read(true, wait);
	}

	public Message read(boolean pop, boolean wait) {
		Message msg = null;

		if (!cvisible)
			return null;

		if (pop) {

			if (wait) {
				boolean docontinue = true;

				while ((docontinue) && (!this.quitted)) {
					// Lo ottengo senza estrarlo per mantenerlo in memoria
					// nel caso in cui poi la visualizzazione sia non più
					// presente
					msg = this.messages.peek();
					while ((msg == null)) {
						msg = this.messages.peek();
						if (msg != null) {
							if (msg.getBody() == null) {
								// In quanto nullo lo elimino comunque
								msg = this.messages.poll();
								msg = null;
								Log.d("body nullo", "nullo");
							} else
								Log.d("body ok", "ok");
						}

						if (!cvisible)
							return null;

					}

					/* Se non è più visibile, esco dall'attesa */
					if (!cvisible)
						return null;

					/* Quest provoca la non lettura del messaggio */
					if ((msg.getBody() == null)) {
						docontinue = true;
						msg = null;
					} else {
						msg = this.messages.poll();
						docontinue = false;
					}
				}
				try {
					Log.d("myChat::read", msg.getBody());
				} catch (Throwable e) {
				}
			} else {
				if (cvisible)
					msg = this.messages.poll();
				else
					return null;
			}

		} else
			Log.e("myChat:read", "pop=false unimplemented");

		return msg;
	}

	/**
	 * Si lascia la chat
	 */
	public void doLeave() {
		Presence leave = new Presence(Presence.Type.unavailable);
		leave.setTo(this.con);
		if (this.conn == null)
			return;
		this.conn.sendPacket(leave);
		this.quitted = true;
	}

	public void put(Message m) {
		this.messages.add(m);
	}

	public void setChatVisibility(boolean see) {
		this.quitted = !see;
		this.cvisible = see;
	}

}
