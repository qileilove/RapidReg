package org.unicef.rapidreg.widgets.dialog;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.unicef.rapidreg.forms.Field;

public class TextDialog extends BaseDialog {

    private EditText editText;

    public TextDialog(Context context, Field field, TextView resultView, ViewSwitcher viewSwitcher) {
        super(context, field, resultView, viewSwitcher);
    }

    @Override
    public void initView() {
        editText = new EditText(getContext());
        editText.setText(resultView.getText().toString());
        editText.setLines(20);
        getBuilder().setView(editText);
    }

    @Override
    public String getResult() {
        return editText.getText().toString();
    }
}
