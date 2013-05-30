package my.amppercent.chattables;

import org.jivesoftware.smack.packet.Message;

public interface UberChat {

	public void doLeave();

	public boolean send(String msg);

	public Message recv(boolean synch);

	public void setChatVisibility(boolean see);

}
