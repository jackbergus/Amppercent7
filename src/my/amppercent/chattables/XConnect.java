package my.amppercent.chattables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import my.amppercent.project.Amppercent4Activity;
import my.amppercent.project.DownloadManager;
import my.amppercent.remoteservice.IFMessage;
import my.amppercent.remoteservice.IntentManage;
import my.amppercent.remoteservice.XUser;
import my.amppercent.remoteservice.chatAdapter;
import my.amppercent.types.Couple;
import my.amppercent.types.myNotification;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Gestione della connessione al server e della creazione delle chat
 * 
 * @author jack
 * 
 */
public class XConnect extends Observable {

	/**
	 * @author jack
	 */
	enum Acceptance {
		all,

		ask,

		none;
	}

	private Context ctxt = null;
	private ConnectionConfiguration cc;
	private XMPPConnection xmpp;
	private boolean connected;
	private boolean logged;
	private boolean available;
	private Acceptance acceptance;
	private String loginname; // Nome Utente
	private String host;
	private String service;
	private String status;
	private Integer port;
	private String password;
	private XRoster mychicken;
	private ChatManager cm;
	private FileTransferManager ftm;
	private Presence.Mode presence;
	private PacketCollector mgroupchat_coll = null;
	private PacketCollector mchat_coll = null;
	private ConcurrentHashMap<String, myChat> chatList;
	private HashMap<String, FileTransferRequest> filereq_string;

	/**************************/
	/** Getters and Setters */
	/**************************/

	public void setAcceptance(Acceptance a) {
		this.acceptance = a;
	}

	public Acceptance getAcceptance() {
		return this.acceptance;
	}

	public XMPPConnection getConnect() {
		return this.xmpp;
	}

	/**
	 * Cambia il comportamento della chat, decidendo se lanciare o meno intent
	 * di visualizzazione
	 * 
	 * @param avail
	 */
	public boolean setAvailability(boolean avail, String jid) {
		UberChat m = null;
		try {
			if (this.chatList.containsKey(jid))
				m = this.chatList.get(jid);
		} catch (Throwable t) {
			return false;
		}
		if (m != null) {
			m.setChatVisibility(avail);
			return true;
		} else
			return false;
	}

	public String getMy_jid() {
		return this.loginname;
	}

	public void setContext(Context c) {
		this.ctxt = c;
	}

	/**********************/
	/** Inizializzatori */
	/**********************/

	public XConnect(String host, Integer port, String serviceName,
			Observer recvhandler, Context ctx) {
		this(host, port, serviceName, recvhandler, false);
		this.ctxt = ctx;
	}

	public String getId() {
		return chatAdapter.getId(this.host, this.port, this.service,
				this.loginname);
	}

	public XConnect(String host, Integer port, String serviceName,
			Observer recvhandler, boolean setSASL) {

		this.host = host;
		this.port = port;
		this.service = serviceName;

		this.loginname = null;
		this.password = null;
		this.mychicken = null;
		this.ftm = null;
		this.cc = new ConnectionConfiguration(host, port, serviceName);

		this.cc.setReconnectionAllowed(true);

		this.xmpp = new XMPPConnection(cc);
		this.chatList = new ConcurrentHashMap<String, myChat>();
		this.filereq_string = new HashMap<String, FileTransferRequest>();

		this.cc.setSASLAuthenticationEnabled(setSASL);// / for gtalk and true
														// for
		this.acceptance = Acceptance.ask;
	}

	/**
	 * Inizializzazione dell'ascoltatore dei trasferimenti files
	 */
	public void getFileTransferManager_Listener() {

		if ((this.ftm == null) & (this.xmpp != null)) {
			ServiceDiscoveryManager sdm = ServiceDiscoveryManager
					.getInstanceFor(this.xmpp);
			if (sdm == null)
				sdm = new ServiceDiscoveryManager(this.xmpp);
			sdm.addFeature("http://jabber.org/protocol/disco#info");
			sdm.addFeature("http://jabber.org/protocol/disco#item");
			sdm.addFeature("jabber:iq:privacy");

			FileTransferNegotiator.setServiceEnabled(xmpp, true);
			this.ftm = new FileTransferManager(this.xmpp);
			this.ftm.addFileTransferListener(new FileTransferListener() {

				public void fileTransferRequest(FileTransferRequest arg0) {
					Log.d("getFileTransferManager::FileTransferRequest",
							"start");
					if (arg0 != null) {
						Log.d("getFileTransferManager::FileTransferRequest",
								"arg not null");
						if (ctxt != null) {
							Log.d("getFileTransferManager::FileTransferRequest",
									"inner..."
											+ arg0.getRequestor().split("/")[0]
											+ " " + arg0.getFileName());
							myNotification mn = new myNotification(ctxt,
									DownloadManager.class, arg0.getRequestor()
											.split("/")[0],
									myNotification.FILE_REQUEST);
							mn.setConnPassword(getId(), password);
							String log = IntentManage.getId(arg0.getRequestor()
									.split("/")[0], arg0.getFileName());
							Log.d("getFileTransferManager::FileTransfweRequest (log) ",
									log);
							filereq_string.put(log, arg0);
							Log.d("getFileTransferManager::FileTransfweRequest",
									"ok... Notifying.");
							mn.notify_case(arg0.getRequestor().split("/")[0],
									arg0.getFileName());

						} else
							Log.e("setting Listener", "Null Context");
						//
					}
				}
			});

		}
	}

	/**
	 * Effettua l'ottenimento della richiesta remota di accettazione dei files
	 * 
	 * @return Viene restituito un array [nomefile, descrizione]
	 */
	public String[] getFileTransferRequest() {
		Set<String> keys = filereq_string.keySet();
		if (keys == null)
			return null;
		String[] arr = new String[keys.size()];
		arr = keys.toArray(arr);
		String[] toret = new String[2];
		toret[0] = arr[0];
		toret[1] = filereq_string.get(toret[0]).getDescription();
		return toret;
	}

	/**
	 * Gestisce la richiesta di trasferimento del file
	 * 
	 * @param accept
	 *            Valore di accettazione
	 * @param filename
	 *            Nome del file da gestire
	 * @param saveto
	 *            Percorso dove effettuare il salvataggio del file
	 * @return Valore di stato del salvataggio del file
	 */
	public boolean handleFileTransferRequest(boolean accept, String filename,
			String saveto) {
		FileTransferRequest req = filereq_string.get(filename);
		Log.d("XConn:: handleFileTransferRequest1", filename);
		if (req == null) {
			Log.d("XConn:: handleFileTransferRequest2", "req is null for "
					+ filename);
			for (String x : filereq_string.keySet())
				Log.e("XConn:: handleFileTransferRequest -- existance", x);
			if (filereq_string.keySet().size() <= 0)
				Log.e("XConn:: handleFileTransferRequest", "no elems");
		} else
			Log.d("XConn:: handleFileTransferRequest1", "ok");
		if ((req == null)) {
			Log.d("XConn:: handleFileTransferRequest", "returning false");
			return false;
		} else {
			Log.d("XConn:: handleFileTransferRequest", "evaluating acceptance");
			if (!accept) {
				req.reject();
				filereq_string.remove(filename);
				return true;
			} else {
				Log.d("XConn:: handleFileTransferRequest", "incoming...");
				IncomingFileTransfer trans = req.accept();
				Log.d("XConn:: handleFileTransferRequest", "incoming2..");
				try {
					String path = Environment.getExternalStorageDirectory()
							.getAbsolutePath();
					if (!path.endsWith(File.pathSeparator))
						path += File.separator;
					path += saveto;
					File f = new File(path);
					Log.d("XConn:: handleFileTransferRequest", "incoming3... "
							+ path);
					try {
						if (f.exists())
							f.delete();
						f.createNewFile();
						f.setWritable(true);
					} catch (IOException e) {
						e.printStackTrace();
					}

					Log.d("File Size:", Long.valueOf(trans.getFileSize())
							.toString());

					trans.recieveFile(f);

					Toast.makeText(this.ctxt,
							"File " + filename + " completato",
							Toast.LENGTH_SHORT).show();

				} catch (XMPPException e) {
					e.printStackTrace();
					Log.d("XConn:: handleFileTransferRequest",
							"returning error ");
					return false;
				}

			}
		}
		return true;
	}

	/**
	 * Effettua la connessione con le credenziali fornite
	 * 
	 * @param doSecure
	 *            Setta o meno le politiche di sicurezza
	 * @return
	 */
	public boolean establish(boolean doSecure) {
		if (this.connected)
			return true;
		else {
			try {
				Log.d("xconnect", "connecting...");
				if (doSecure)
					SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				xmpp.connect();

				Log.d("xconnect", "established");
				return true;
			} catch (XMPPException e) {
				Log.e("xconnect", e.getLocalizedMessage());
				return false;
			}
		}
	}

	/**
	 * @param status
	 * @param avail
	 * @param pm
	 */
	public void setStatus(String status, boolean avail, Presence.Mode pm) {
		Presence presence = new Presence(avail ? Presence.Type.available
				: Presence.Type.unavailable);
		presence.setStatus(status);
		presence.setMode(pm);
		this.xmpp.sendPacket(presence);
		this.status = status;
		this.available = avail;
	}

	public String getStatus() {
		return this.status;
	}

	public String getMode() {
		return XUser.stateToString(XUser.presenceToStatus(presence));
	}

	/**
	 * Effettua il login ad una connessione preventivamente instaurata
	 * 
	 * @param username
	 * @param password
	 * @param status
	 * @param doSecure
	 * @param available
	 * @return
	 */
	public boolean login(String username, String password, String status,
			boolean doSecure, boolean available) {
		if (this.logged)
			return true;
		if (!this.connected) {
			boolean val = establish(doSecure);
			if (!val)
				return false;
		}
		try {

			Log.d("xconnect", "logging " + username + " " + password);
			this.xmpp.login(username, password);
			Log.d("xconnect", "logging done");
			this.loginname = username;
			this.password = password;
			setStatus(status, available, (available ? Presence.Mode.chat
					: Presence.Mode.xa));
			this.logged = true;

			this.xmpp.getChatManager().addChatListener(
					new ChatManagerListener() {

						public void chatCreated(Chat arg0,
								boolean createdLocally) {
							// Possibile estensione: ora suppongo di accettare
							// tutte le chat
							String from = arg0.getParticipant();
							if (!createdLocally) {

								String s = Amppercent4Activity
										.outKeySet(chatList.keySet());
								if (!chatList.containsKey(from.split("/")[0])) {
									Log.d("ChatCreated here",
											from.split("/")[0] + " --- " + s);
									createChat(arg0);
								}

							}
						}
					});

			Log.d("xconnect", "packet filter setted");

			this.mychicken = new XRoster(this.xmpp.getRoster());
			this.cm = this.xmpp.getChatManager();

			listenGroupChat_invite();
			listenChat_invite();
			getFileTransferManager_Listener();

			return true;

		} catch (XMPPException e) {
			Log.e("xconnect", e.getMessage());
			this.logged = false;
			return false;
		}

	}

	public boolean sendMessage2(String text, String dest) {
		if (this.chatList.containsKey(dest)) {
			myChat m = this.chatList.get(dest);
			m.send(text);
			return true;
		} else
			return false;
	}

	public IFMessage recvMessage(String from) {
		if (this.chatList.containsKey(from)) {
			IFMessage toret;
			Log.e("XConnect::recvMessage", "1. getting chat");
			myChat mc = this.chatList.get(from);
			Log.e("XConnect::recvMessage", "2. reading chat");
			Message im = mc.read(true, true);
			if (im == null)
				toret = null;
			else {
				toret = new IFMessage(im);
				toret.setMittNick(mychicken.getNickname(from));
			}
			return toret;
		} else {
			Log.e("XConnect::recvMessage", "null option");
			return null;
		}
	}

	/***
	 * Restituisce il Roster per la gestione degli stati degli utenti
	 * 
	 * @return
	 */
	public XRoster getRoster() {
		if ((this.logged) && (this.cc != null)) {
			Log.d("roster", "ok case");
			return this.mychicken;
		} else
			return null;
	}

	public myChat createChat(String withz) {
		String with = withz.split("/")[0];
		myChat mc = new myChat(this, cm, with, this.ctxt, password);
		mc.setChatVisibility(false);
		Set<String> ks = this.chatList.keySet();
		if (ks == null) {
			Log.d("XConnect:createChat", "inserting " + with);
			chatList.put(with, mc);
		} else {
			if (ks.contains(with))
				Log.w("XConnect: createGroupChat", "already contains " + with);
			else {
				Log.d("XConnect:createChat", "inserting " + with);
				chatList.put(with, mc);
			}
		}
		return mc;
	}

	private void createChat(Chat c) {

		Set<String> ks = this.chatList.keySet();
		String with = c.getParticipant().split("/")[0];
		if (ks == null) {
			Log.d("XConnect:createChat", "inserting " + with);
			chatList.put(with, new myChat(this, c, this.ctxt, password));
		} else {
			if (ks.contains(with))
				Log.w("XConnect: createGroupChat", "already contains " + with);
			else {
				Log.d("XConnect:createChat", "inserting " + with);
				chatList.put(with, new myChat(this, c, this.ctxt, password));
			}
		}

	}

	public void closeChat(String with) {
		if (this.chatList.containsKey(with)) {
			myChat mc = this.chatList.get(with);
			mc.doLeave();
		}
	}

	public void closeChats() {
		if (this.chatList.size() > 0) {
			for (String key : this.chatList.keySet()) {
				closeChat(key);
			}
		}

	}

	/**
	 * Chiude tutte le comunicazioni
	 */
	public void close() {
		if (this.xmpp != null)
			try {
				this.xmpp.disconnect();
			} catch (Throwable e) {
			}

		if (this.mchat_coll != null)
			this.mchat_coll.cancel();

		if (this.mgroupchat_coll != null)
			this.mgroupchat_coll.cancel();

		if (this.chatList != null)
			this.chatList.clear();

	}

	/**
	 * Possibile estensione, visualizzare notifica dell'accettazione o
	 * meno.
	 * 
	 * @author jack
	 * 
	 */
	class AcceptanceSubscribePacketFilter implements PacketFilter {

		public boolean accept(Packet packet) {
			if (packet instanceof Presence) {
				if (((Presence) packet).getType().equals(
						Presence.Type.subscribe)) {
					Log.d("subscribe", "subscr");
				} else
					Log.d("subnot", "subscr");
			}
			return false;
		}
	}

	public void listenGroupChat_invite() {
		if (mgroupchat_coll != null)
			mgroupchat_coll = this.xmpp
					.createPacketCollector(new PacketExtensionFilter("x",
							"jabber:x:conference"));
	}

	public void listenChat_invite() {
		if (mchat_coll != null)
			mchat_coll = this.xmpp
					.createPacketCollector(new AcceptanceSubscribePacketFilter());
	}

	/**
	 * Estrae da un collettore un pacchetto
	 * 
	 * @param pcoll
	 *            Collettore dal quale effettuare l'estrazione
	 * @param synch
	 *            Indica se la richiesta è sincrona o meno
	 * @param timeout
	 *            Se è sincrona e il timeout è negativo effettua una richiesta
	 *            bloccante, se invece il timeout è positivo attende per massimo
	 *            i secondi indicati. Se è invece asincrona, restituisce null se
	 *            l'elemento non è presente.
	 * @return
	 */
	private Packet extract(PacketCollector pcoll, boolean synch, long timeout) {
		Packet msg;
		if (pcoll == null)
			return null;
		if ((synch) && (timeout >= 0))
			msg = pcoll.nextResult(timeout);
		else if (synch)
			msg = pcoll.nextResult();
		else
			msg = pcoll.pollResult();
		return msg;
	}

	/**
	 * Coppia identificante una richiesta di adesione ad una chat
	 */
	public class ChatReq extends Couple<Packet, String> {

		public ChatReq(Packet x, String y) {
			super(x, y);
		}

	}

	/**
	 * Ottiene una richiesta di affiliazione ad una chat, restituendo la stringa
	 * 
	 * @param synch
	 * @param timeout
	 * @return
	 */
	public ChatReq getMChatReq(boolean synch, long timeout) {
		Packet msg = extract(mgroupchat_coll, synch, timeout);
		if (msg == null)
			return null;
		for (PacketExtension x : msg.getExtensions()) {
			if (x instanceof GroupChatInvitation)
				return new ChatReq(msg,
						((GroupChatInvitation) x).getRoomAddress());
		}
		return null;
	}

	/**
	 * Ottiene le richieste di adesione ad una chat
	 * 
	 * @param synch
	 * @param timeout
	 * @return Stringa indicante la chat
	 */
	public String getMChatReq_String(boolean synch, long timeout) {
		Packet msg = extract(mgroupchat_coll, synch, timeout);
		if (msg == null)
			return null;
		for (PacketExtension x : msg.getExtensions()) {
			if (x instanceof GroupChatInvitation)
				return ((GroupChatInvitation) x).getRoomAddress();
		}
		return null;
	}

	public ChatReq getChatReq(boolean synch, long timeout) {
		Presence msg = (Presence) extract(mgroupchat_coll, synch, timeout);
		if (msg == null)
			return null;
		return new ChatReq(msg, msg.getFrom());
	}

	/**
	 * Ottiene le richieste di Chattare da un singolo individuo
	 * 
	 * @param synch
	 * @param timeout
	 * @return JID dell'utente che vuole chattare
	 */
	public String getChatReq_String(boolean synch, long timeout) {
		Presence msg = (Presence) extract(mchat_coll, synch, timeout);
		if (msg == null)
			return null;
		return msg.getFrom();
	}

	public String getNickname(String jid) {
		return getRoster().getNickname(jid);
	}

	public void replyReqest(ChatReq cr, boolean accept) {
		if (accept) {
			Presence presence = new Presence(Presence.Type.subscribed);
			presence.setTo(cr.snd());
			xmpp.sendPacket(presence);
		} else {
			Presence presence = new Presence(Presence.Type.unsubscribed);
			presence.setTo(cr.snd());
			xmpp.sendPacket(presence);
		}
	}

	public void replyReqest(String cr, boolean accept) {
		if (accept) {
			Presence presence = new Presence(Presence.Type.subscribed);
			presence.setTo(cr);
			xmpp.sendPacket(presence);
		} else {
			Presence presence = new Presence(Presence.Type.unsubscribed);
			presence.setTo(cr);
			xmpp.sendPacket(presence);
		}
	}

	public void sendPacket(Packet packet) {
		this.xmpp.sendPacket(packet);
	}

	static String generateSessionID() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("");
		buffer.append(Math.abs(new Random(20).nextLong()));

		return buffer.toString();
	}

	public boolean sendFile(final String jid, final String path,
			String description) {

		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(this.xmpp);
		if (sdm == null)
			sdm = new ServiceDiscoveryManager(this.xmpp);
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("http://jabber.org/protocol/disco#item");
		sdm.addFeature("jabber:iq:privacy");

		FileTransferNegotiator.IBB_ONLY = true;

		FileTransferNegotiator.setServiceEnabled(this.xmpp, true);
		FileTransferManager manage = new FileTransferManager(this.xmpp);
		Log.d("sending file", "set true");

		OutgoingFileTransfer.setResponseTimeout(10000);
		OutgoingFileTransfer oft = manage
				.createOutgoingFileTransfer(this.mychicken.getPresence(jid)
						.getFrom());

		try {
			Log.d("sending file", "try to send... " + path);
			oft.sendFile(new File(path), description);
			Log.d("sending file", "Accepted");
			while (!oft.isDone()) {
				Log.d("status", oft.getStatus().toString());
				Log.d("percent", Long.valueOf(oft.getBytesSent()).toString());
				if (oft.getStatus() == FileTransfer.Status.error) {
					Log.e("percent", "Error "
							+ Long.valueOf(oft.getBytesSent()).toString() + " "
							+ oft.getError() + " " + oft.getException());
					oft.cancel();
					return false;
				}
				Thread.sleep(9000);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Log.e("sendFile", path);
			return false;
		}
		return true;
	}

	public boolean getChatteursAvail() {
		return this.available;
	}

}