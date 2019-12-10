package com.appskimo.app.bible.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.Window;

public abstract class CommonDialog extends DialogFragment {
    protected boolean shown;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (shown) return;
        super.show(manager, tag);
        shown = Boolean.TRUE;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        shown = Boolean.FALSE;
        super.onDismiss(dialog);
    }

    @SuppressWarnings("unused")
    public boolean isShown() {
        return shown;
    }
}
