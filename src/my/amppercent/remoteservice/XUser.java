package my.amppercent.remoteservice;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.jivesoftware.smack.packet.Presence;

import android.os.Parcel;
import android.os.Parcelable;

public class XUser extends Observable implements Parcelable {

	/**
	 * @author jack
	 */
	public enum Status {

		none_offline, // start (grey)

		DoNotDisturb, // dnd (red)

		ExtendedAway, // xa (yellow)

		Away, // away (yellow)

		Chat // chat (green)
	}

	public static List<XUser> getFromArray(Parcelable[] purz) {
		List<XUser> xul = new LinkedList<XUser>();
		if (purz.length > 0) {
			for (Parcelable x : purz) {
				xul.add((XUser) x);
			}
		}
		return xul;
	}

	private String jid;
	private String uname;
	private String status;
	private Status state;
	private Observer obs;
	public boolean selected = false;

	public XUser(String jid, String uname) {
		this.jid = jid;
		this.uname = uname;
		this.status = "";
		this.state = Status.none_offline;
		this.obs = null;
	}

	public void setStatus(String status) {
		this.status = status;
		if (this.obs != null) {
			this.setChanged();
			this.notifyObservers();
		}
	}

	public String getStatus() {
		return this.status;
	}

	public void setState(Status s) {
		if (s == null)
			this.state = Status.none_offline;
		this.state = s;
		if (this.obs != null) {
			this.setChanged();
			this.notifyObservers();
		}
	}

	public void setState(String str) {
		this.state = stringToState(str);
	}

	public static Status stringToState(String str) {
		if (str == null)
			return Status.none_offline;
		else if (str.equals(Status.Chat.toString()))
			return Status.Chat;
		else if (str.equals(Status.Away.toString()))
			return Status.Away;
		else if (str.equals(Status.DoNotDisturb.toString()))
			return Status.DoNotDisturb;
		else if (str.equals(Status.ExtendedAway.toString()))
			return Status.ExtendedAway;
		else
			return Status.none_offline;
	}

	public static String stateToString(Status s) {
		return s.toString();
	}

	public static Presence.Mode stateToMode(Status s) {
		switch (s) {
		case Away:
			return Presence.Mode.away;
		case Chat:
			return Presence.Mode.chat;
		case DoNotDisturb:
			return Presence.Mode.dnd;
		default:
			return Presence.Mode.xa;
		}
	}

	public static Status presenceToStatus(Presence.Mode v) {
		if (v == null)
			return Status.none_offline;
		else {
			switch (v) {
			case available:
				return Status.Chat;
			case away:
				return Status.Away;
			case chat:
				return Status.Chat;
			case dnd:
				return Status.DoNotDisturb;
			case xa:
				return Status.ExtendedAway;
			default:
				return Status.none_offline;
			}
		}
	}

	public void setState(Presence.Mode v) {

		this.state = presenceToStatus(v);

		if (this.obs != null) {
			this.setChanged();
			this.notifyObservers();
		}
	}

	public Status getState() {
		return this.state;
	}

	public String getNickname() {
		return this.uname;
	}

	public String getJid() {
		return this.jid;
	}

	public XUser setObserver(Observer obs) {
		if (obs != null) {
			if (this.obs != null)
				this.deleteObserver(this.obs);
		}
		this.obs = obs;
		this.addObserver(this.obs);
		return this;
	}

	public void setNickname(String nick) {
		this.uname = nick;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.jid);
		dest.writeString(this.uname);
		dest.writeString(this.status);
		dest.writeString(this.state.toString());
	}

	public XUser(Parcel purzel) {
		this.jid = purzel.readString();
		this.uname = purzel.readString();
		this.status = purzel.readString();
		setState(purzel.readString());
	}

	public static final Parcelable.Creator<XUser> CREATOR = new Parcelable.Creator<XUser>() {
		public XUser createFromParcel(Parcel in) {
			return new XUser(in);
		}

		public XUser[] newArray(int size) {
			return new XUser[size];
		}
	};

}
