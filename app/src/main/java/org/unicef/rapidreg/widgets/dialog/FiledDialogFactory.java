package org.unicef.rapidreg.widgets.dialog;

import android.content.Context;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.unicef.rapidreg.exception.DialogException;
import org.unicef.rapidreg.forms.Field;

public class FiledDialogFactory {
    public static BaseDialog createDialog(Field.FieldType fieldType, Context context,
                                          Field field,
                                          TextView resultView) throws DialogException {
        try {
            return fieldType.getClz().getConstructor(Context.class, Field.class, TextView.class)
                    .newInstance(context, field, resultView);
        } catch (Exception e) {
            throw new DialogException(String.format("fieldType: %s", fieldType), e);
        }
    }

    public static BaseDialog createDialog(Field.FieldType fieldType, Context context,
                                          Field field, TextView resultView,
                                          ViewSwitcher viewSwitcher) throws DialogException {
        try {
            return fieldType.getClz().getConstructor(Context.class, Field.class,
                    TextView.class, ViewSwitcher.class)
                    .newInstance(context, field, resultView, viewSwitcher);
        } catch (Exception e) {
            throw new DialogException(String.format("fieldType: %s", fieldType), e);
        }
    }
}
