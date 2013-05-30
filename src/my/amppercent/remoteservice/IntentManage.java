package my.amppercent.remoteservice;

import my.amppercent.project.TabFragment;
import my.amppercent.types.myNotification;
import android.content.*;
import android.os.*;

/**
 * Parcellizzazione di un Intent che può essere inviato
 * 
 * @author jack
 * 
 */
public class IntentManage implements Parcelable {

	public final static int DOWNLOAD = my.amppercent.project.R.layout.download_view;

	public final static String KIND = "kind";
	public final static String HANDLED = "handled";

	public int describeContents() {
		return 0;
	}

	public static String getId(String firstarg, String secondarg) {
		return firstarg + "@" + secondarg;
	}

	public String getId() {
		return getId(this.first_argument, this.second_argument);
	}

	public IntentManage(Parcel p) {
		this.first_argument = p.readString();
		this.second_argument = p.readString();
		this.connid = p.readString();
		this.passwo = p.readString();
		this.KindRequest = p.readInt();
		this.handled = (p.readInt() == 1 ? true : false);
	}

	public IntentManage(Intent i) {
		this.first_argument = i.getStringExtra(myNotification.FIRST_BUNDLE);
		this.second_argument = i.getStringExtra(myNotification.SECOND_BUNDLE);
		this.connid = i.getStringExtra(TabFragment.CONNECTIONID);
		this.passwo = i.getStringExtra(TabFragment.PASSWORD);
		this.KindRequest = i.getIntExtra(IntentManage.KIND, -1);
		this.handled = (i.getIntExtra(IntentManage.HANDLED, 0) == 1 ? true
				: false);
	}

	public static final Parcelable.Creator<IntentManage> CREATOR = new Parcelable.Creator<IntentManage>() {

		public IntentManage createFromParcel(Parcel arg0) {
			return new IntentManage(arg0);
		}

		public IntentManage[] newArray(int size) {
			return new IntentManage[size];
		}
	};

	public void writeToParcel(Parcel p1, int p2) {
		p1.writeString(first_argument);
		p1.writeString(second_argument);
		p1.writeString(connid);
		p1.writeString(passwo);
		p1.writeInt(KindRequest);
		p1.writeInt((this.handled ? 1 : 0));
	}

	public String first_argument;
	public String second_argument;
	public Integer KindRequest;
	public String connid;
	public String passwo;
	public boolean handled;
}
