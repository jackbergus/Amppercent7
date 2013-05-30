package my.amppercent.remoteservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import my.amppercent.chattables.XConnect;

/**
 * Effettua un adattatore di connessione, effettuando un'opportuna richiesta di
 * accesso per poter effettuare le operazioni legate alla connessione, chiedendo
 * sempre la password associata all'utente che ha effettuato la connessione
 * 
 * @author giacomo
 * 
 */
public class chatAdapter {

	private Map<String, XConnect> server_connections;
	private Map<String, String> connection_password;
	private Context context;

	public chatAdapter(Context ctx) {
		this.server_connections = new HashMap<String, XConnect>();
		this.connection_password = new HashMap<String, String>();
		this.context = ctx;
	}

	public static String getId(String host, Integer port, String service,
			String username) {
		return service + "@" + host + ":" + port.toString() + "/" + username;
	}

	public void destroy() {
		if (server_connections.size() > 0) {
			for (XConnect x : server_connections.values()) {
				x.close();
			}
			server_connections.clear();
		}
	}

	/**
	 * Crea una nuova connessione al server di chat, oppure ne fornisce una
	 * esistente
	 * 
	 * @param host
	 * @param port
	 * @param service
	 * @param username
	 * @param Pw
	 * @param doSecure
	 * @return
	 */
	public XConnect newConnection(String host, Integer port, String service,
			String username, String Pw, boolean doSecure) {
		return newConnection(host, port, service, username, Pw, doSecure, "",
				false);
	}

	public XConnect newConnection(String host, Integer port, String service,
			String username, String Pw, boolean doSecure, String status,
			boolean available) {
		String id = getId(host, port, service, username);
		if (this.server_connections.keySet().contains(id))
			return this.server_connections.get(id);
		else {
			XConnect conn = new XConnect(host, port, service, null,
					this.context);
			if (!conn.login(username, Pw, status, doSecure, available))
				return null;
			this.server_connections.put(id, conn);
			this.connection_password.put(id, Pw);
			return conn;
		}
	}

	public void setNickname(String nick) {

	}

	private void killConnection(String id) {
		if (this.server_connections.keySet().contains(id)) {
			this.server_connections.remove(id).close();
		}
	}

	public void killConnection(String host, Integer port, String service,
			String username, String pw) {
		String id = getId(host, port, service, username);
		killConnection(id, pw);
	}

	public void killConnection(String id, String pw) {
		if ((this.connection_password.containsKey(id))
				&& (this.connection_password.get(id).equals(pw)))
			killConnection(id);
	}

	private boolean Authenticate(String id, String password) {
		return ((this.connection_password.containsKey(id)) && (this.connection_password
				.get(id).equals(password)));
	}

	public Collection<XUser> getUserStatus(String id, String password) {
		if (this.server_connections.keySet().contains(id)) {
			if (Authenticate(id, password))
				return this.server_connections.get(id).getRoster()
						.get_buddyList().values();
		}
		return null;
	}

	public Collection<XUser> getUserStatus2(String id, String password) {
		if (this.server_connections.keySet().contains(id)) {
			if (Authenticate(id, password))
				return this.server_connections.get(id).getRoster()
						.get_buddyList2().values();
		}
		return null;
	}

	public boolean startChatWith(String connectionid, String password,
			String jid) {
		if (Authenticate(connectionid, password)) {
			if (this.server_connections.keySet().contains(connectionid)) {
				XConnect conn = this.server_connections.get(connectionid);
				conn.createChat(jid);
				return true;
			}
		}
		return false;
	}

	public void stopChatWith(String connectionid, String password, String jid) {
		Log.d("chatAdapter::stopChatWith", "stopping " + jid);
		if (Authenticate(connectionid, password)) {
			Log.d("chatAdapter::stopChatWith", "ok");
			if (this.server_connections.keySet().contains(connectionid)) {
				XConnect conn = this.server_connections.get(connectionid);
				conn.closeChat(jid);
			}
		}
	}

	private XConnect getConnection(String connectionid, String password) {
		if (Authenticate(connectionid, password)) {
			if (this.server_connections.keySet().contains(connectionid)) {
				return this.server_connections.get(connectionid);
			}
		}
		return null;
	}

	public void closeChats(String connectionid, String password) {
		if (Authenticate(connectionid, password)) {
			if (this.server_connections.keySet().contains(connectionid)) {
				XConnect xc = this.server_connections.get(connectionid);
				xc.closeChats();
			}
		}
	}

	public boolean startGroupChatWith(String connectionid, String password,
			String jid) {
		// VOID
		return false;
	}

	public void stopGroupChatWith(String connectionid, String password,
			String jid) {
		// VOID
	}

	/**
	 * Effettua l'invio di un messaggio sia per le multichat, sia per le chat
	 * normali
	 * 
	 * @param id
	 *            ID della connessione
	 * @param password
	 *            Password della connessione associata all'utente collegato
	 * @param jid
	 *            JID della destinazione
	 * @param message
	 *            Messaggio da inoltrare
	 */
	public void sendMessage(String id, String password, String jid,
			String message) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			Log.d("chatAdapter::sendMessage", message + " " + jid);
			if (!conn.sendMessage2(message, jid))
				Log.e("chapAdapter::sendMessage", "false returned");
		}
	}

	public IFMessage recvMessage(String id, String password, String jid) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			IFMessage im = conn.recvMessage(jid);
			if (im == null)
				Log.w("chatAdapter::recvMessage", "null message from " + jid);
			else
				Log.d("chatAdapter::recvMessage", "not null from " + jid);
			return im;
		} else
			return null;
	}

	public String getNickname(String id, String password, String jid) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getNickname(jid);
		} else
			return null;
	}

	public String getMChatRequest(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getMChatReq_String(true, 0);
		}
		return null;
	}

	public String getChatReString(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getChatReq_String(true, 0);
		}
		return null;
	}

	/**
	 * Ottiene tutte le connessioni attive
	 * 
	 * @return
	 */
	public Set<String> getServerConnList() {
		return this.server_connections.keySet();
	}

	public String[] getFileRequest(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getFileTransferRequest();
		}
		return null;
	}

	public boolean handleFileReq(String id, String password, boolean accept,
			String filename, String saveto) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			Log.d("chatAdapter::handleFileReq",
					"invoking conn.handleFileTransfer");
			return conn.handleFileTransferRequest(accept, filename, saveto);
		}
		return false;
	}

	public boolean sendFile(String id, String password, String jid,
			String filename, String descr) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.sendFile(jid, filename, descr);
		}
		return false;
	}

	/**
	 * Imposta il valore della visibilità di una chat esistente
	 * 
	 * @param id
	 * @param password
	 * @param jidwith
	 * @param avail
	 * @return Restituisce True se la chat esiste, altrimenti false
	 */
	public boolean available_chat(String id, String password, String jidwith,
			boolean avail) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			if (!avail)
				Log.e("STOPPING SHOW CHAT!!!!!!!!!", jidwith);
			return conn.setAvailability(avail, jidwith);
		} else
			return false;
	}

	public String getMode(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getMode();
		} else
			return null;
	}

	public boolean getChatteursAvail(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getChatteursAvail();
		} else
			return false;
	}

	public String getStatus(String id, String password) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			return conn.getStatus();
		} else
			return null;
	}

	public void setStatesetState(String id, String password, boolean avail,
			String info, String mode) {
		XConnect conn = getConnection(id, password);
		if (conn != null) {
			conn.setStatus(info, avail,
					XUser.stateToMode(XUser.stringToState(mode)));
		}
	}

}
