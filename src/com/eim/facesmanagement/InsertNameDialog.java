package com.eim.facesmanagement;

import com.eim.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;

public class InsertNameDialog extends DialogFragment {
	OnClickListener okOnClickListener, cancelOnClickListener;

	public InsertNameDialog(OnClickListener okOnClickListener,
			OnClickListener cancelOnClickListener) {
		this.okOnClickListener = okOnClickListener;
		this.cancelOnClickListener = cancelOnClickListener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_insert_person, null);

		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.action_insert_person)
				.setTitle(R.string.alert_dialog_insert_person)
				.setView(v)
				.setPositiveButton(R.string.alert_dialog_ok, okOnClickListener)
				.setNegativeButton(R.string.alert_dialog_cancel,
						cancelOnClickListener).create();
	}
}
