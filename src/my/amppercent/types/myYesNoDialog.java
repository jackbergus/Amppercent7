package my.amppercent.types;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;


/**
 * Predispone l'implementazione ad un dialogo Yes/No
 * @author jack
 *
 */
public abstract class myYesNoDialog implements DialogInterface.OnClickListener  {

private AlertDialog.Builder builder;
	
	public static  ProgressDialog newProgressDialog(Context context) {
		ProgressDialog toret = new ProgressDialog(context);
		toret.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		toret.setMessage("Waiting for Roster...");
		return toret;
	}
	
	public myYesNoDialog(Activity a,String message,String yes,String no) {
		super();
		builder = new AlertDialog.Builder(a);
		builder.setMessage(message).setPositiveButton(yes, this).setNegativeButton(no, this);
	}
	
	public void show() {
		builder.show();
	}
	
	public Dialog create() {
		return builder.create();
	}
	
	public abstract void onClickYes(DialogInterface dialog, int which);
	
	public abstract void onClickNo(DialogInterface dialog, int which);
	
	
	public void onClick(DialogInterface dialog, int which) {
        switch (which){
        case DialogInterface.BUTTON_POSITIVE:
        	onClickYes(dialog,which);
            break;

        case DialogInterface.BUTTON_NEGATIVE:
        	onClickNo(dialog,which);
            break;
        }

	}
	
}
