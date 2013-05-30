package my.amppercent.remoteservice;
import  my.amppercent.remoteservice.XUser;
import  my.amppercent.remoteservice.IFMessage;

interface IBinding {
	String connect_n_login(String host, int port, String service, String username, String password, 
							boolean doSecure, String status, boolean available);
	void kill_connection(String connectionid, String password);
	List<XUser> getbuddyList(String connectionid, String password);
	List<String> getConnectionList();
	boolean ChatWith(String connectionid,String password, String jid);
	void stopChatWith(String connectionid,String password, String jid);
	void sendMessageTo(String id, String password, String jid, String text);
	IFMessage recvMessage(String id, String password, String from);
	String getNickname(String id, String password, String jid);
	String getGroupChatRequest(String id, String password);
	String getChatRequest(String id, String password);
	String[] getFileRequest(String id, String password);
	boolean handleFileRequest(String id, String password, boolean acceptance, String filename, String saveto);
	boolean sendFile(String id, String password, String jid, String file, String descr);
	void setState(String id, String password, boolean avail, String info, String mode);
	String getMode(String id, String password);
	String getStatus(String id, String password);
	boolean getAvail(String id, String password);
	boolean available_chat(String id, String password, String jidwith, boolean avail);
	
	void closeChats(String id, String passowrd);
	void killme();
	
}