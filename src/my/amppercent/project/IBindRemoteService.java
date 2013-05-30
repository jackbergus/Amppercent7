package my.amppercent.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;

import my.amppercent.remoteservice.IBinding;
import my.amppercent.remoteservice.IFMessage;
import my.amppercent.remoteservice.XUser;
import my.amppercent.remoteservice.chatAdapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

/**
 * Offre l'interfaccia pubblica per il servizio remoto esterno
 * 
 * @author giacomo
 * 
 */
public class IBindRemoteService extends Service {

	chatAdapter chat_adapter;

	public void onCreate() {
		Log.d("IBindRemoteService", "onCreate");
		chat_adapter = new chatAdapter(IBindRemoteService.this);
		startup();
	}

	/**
	 * Procedura di configurazione
	 */
	private void startup() {
		ProviderManager pm = ProviderManager.getInstance();

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new IBBProviders.Open());
		//
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new IBBProviders.Close());
		//
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new IBBProviders.Data());

		//
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());

		// Setting Smack configurations
		SmackConfiguration.setKeepAliveInterval(10000);
		SmackConfiguration.setPacketReplyTimeout(10000);

	}

	public IBinder onBind(Intent arg0) {
		return new IBinding.Stub() {

			public String connect_n_login(String host, int port,
					String service, String username, String password,
					boolean doSecure, String status, boolean available)
					throws RemoteException {

				if (chat_adapter.newConnection(host, port, service, username,
						password, doSecure, status, available) != null) {

					return chatAdapter.getId(host, port, service, username);
				} else
					return null;
			}

			public void kill_connection(String connectionid, String password)
					throws RemoteException {
				chat_adapter.killConnection(connectionid, password);
			}

			public List<XUser> getbuddyList(String connectionid, String password)
					throws RemoteException {
				Collection<XUser> user = chat_adapter.getUserStatus2(
						connectionid, password);
				if (user == null)
					return new ArrayList<XUser>();
				else
					return new ArrayList<XUser>(user);
			}

			public List<String> getConnectionList() throws RemoteException {
				return new ArrayList<String>(chat_adapter.getServerConnList());
			}

			public boolean ChatWith(String connectionid, String password,
					String jid) throws RemoteException {
				Log.d("ChatWith::" + TabFragment.CONNECTIONID,
						(connectionid == null ? "null" : connectionid));
				Log.d("ChatWith::" + TabFragment.PASSWORD,
						(password == null ? "null" : password));
				Log.d("ChatWith::" + "jid", (jid == null ? "null" : jid));
				return chat_adapter.startChatWith(connectionid, password, jid);
			}

			public void stopChatWith(String connectionid, String password,
					String jid) throws RemoteException {
				chat_adapter.stopChatWith(connectionid, password, jid);
			}

			public void sendMessageTo(String id, String password, String jid,
					String text) throws RemoteException {
				chat_adapter.sendMessage(id, password, jid, text);
			}

			public IFMessage recvMessage(String id, String password, String from)
					throws RemoteException {
				return chat_adapter.recvMessage(id, password, from);
			}

			public String getNickname(String id, String password, String jid)
					throws RemoteException {
				return chat_adapter.getNickname(id, password, jid);
			}

			public String getGroupChatRequest(String id, String password)
					throws RemoteException {
				return chat_adapter.getMChatRequest(id, password);
			}

			public String getChatRequest(String id, String password)
					throws RemoteException {
				return chat_adapter.getChatReString(id, password);
			}

			public String[] getFileRequest(String id, String password)
					throws RemoteException {
				return chat_adapter.getFileRequest(id, password);
			}

			public boolean handleFileRequest(String id, String password,
					boolean acceptance, String filename, String saveto)
					throws RemoteException {
				return chat_adapter.handleFileReq(id, password, acceptance,
						filename, saveto);
			}

			public boolean sendFile(String id, String password, String jid,
					String file, String descr) throws RemoteException {
				return chat_adapter.sendFile(id, password, jid, file, descr);
			}

			public boolean available_chat(String id, String password,
					String jidwith, boolean avail) throws RemoteException {
				return chat_adapter
						.available_chat(id, password, jidwith, avail);
			}

			public void killme() throws RemoteException {
				chat_adapter.destroy();
				stopSelf();
			}

			public void closeChats(String id, String password)
					throws RemoteException {
				chat_adapter.closeChats(id, password);
			}

			public void setState(String id, String password, boolean avail,
					String info, String mode) throws RemoteException {
				chat_adapter.setStatesetState(id, password, avail, info, mode);
			}

			public String getMode(String id, String password)
					throws RemoteException {
				return chat_adapter.getMode(id, password);
			}

			public String getStatus(String id, String password)
					throws RemoteException {
				return chat_adapter.getStatus(id, password);
			}

			public boolean getAvail(String id, String password)
					throws RemoteException {
				return chat_adapter.getChatteursAvail(id, password);
			}

		};
	}

}