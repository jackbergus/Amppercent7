package my.amppercent.remoteservice;

import org.jivesoftware.smack.packet.Message;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Messaggio tra mittente e destinatario
 * 
 * @author giacomo
 * 
 */
public class IFMessage implements Parcelable {
	private XUser mittente;
	private XUser destinat;
	private String body;

	public IFMessage(String mittuid, String destuid, String body) {

		this.mittente = new XUser(mittuid, mittuid);
		this.destinat = new XUser(destuid, destuid);
		this.body = body;
	}

	public IFMessage(Parcel purzel) {
		this.mittente = new XUser(purzel.readString(), purzel.readString());
		this.destinat = new XUser(purzel.readString(), purzel.readString());
		this.body = purzel.readString();
	}

	public IFMessage(Message androidmessage) {
		this(androidmessage.getFrom(), androidmessage.getTo(), androidmessage
				.getBody());
	}

	public IFMessage(Message androidmessage, String mittnick, String destnick) {
		this.mittente = new XUser(androidmessage.getFrom(), mittnick);
		this.mittente = new XUser(androidmessage.getTo(), destnick);
		this.body = androidmessage.getBody();
	}

	public XUser getMitt() {
		return this.mittente;
	}

	public XUser getDest() {
		return this.destinat;
	}

	public String getMessage() {
		return this.body;
	}

	public IFMessage setMittNick(String nick) {
		this.mittente.setNickname(nick);
		return this;
	}

	public String[] getArray() {
		String[] sa = new String[2];
		sa[0] = this.mittente.getNickname();
		sa[1] = this.body;
		return sa;
	}

	public static final Parcelable.Creator<IFMessage> CREATOR = new Parcelable.Creator<IFMessage>() {

		public IFMessage createFromParcel(Parcel arg0) {
			return new IFMessage(arg0);
		}

		public IFMessage[] newArray(int size) {
			return new IFMessage[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mittente.getJid());
		dest.writeString(this.mittente.getNickname());
		dest.writeString(this.destinat.getJid());
		dest.writeString(this.destinat.getNickname());
		dest.writeString(this.body);
	}
}
