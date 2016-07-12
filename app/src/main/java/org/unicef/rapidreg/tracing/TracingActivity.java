package org.unicef.rapidreg.tracing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordActivity;
import org.unicef.rapidreg.childcase.CaseFeature;
import org.unicef.rapidreg.childcase.CaseRegisterWrapperFragment;
import org.unicef.rapidreg.event.SaveTracingEvent;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.forms.TracingFormRoot;
import org.unicef.rapidreg.service.RecordService;
import org.unicef.rapidreg.service.TracingFormService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TracingActivity extends RecordActivity {
    public static final String TAG = TracingActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        turnToFeature(TracingFeature.LIST, null);
    }

    @Override
    protected void navCaseAction() {
        if (currentFeature.isDetailMode()) {
            showQuitDialog(R.id.nav_tracing);
        } else {
            CaseFieldValueCache.clearAudioFile();
            intentSender.showCasesActivity(this, null, false);
        }
    }

    @Override
    protected void navTracingAction() {
        if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_sync);
        } else {
            CaseFieldValueCache.clearAudioFile();
            intentSender.showSyncActivity(this);
        }
    }

    @Override
    protected void processBackButton() {
        if (currentFeature.isListMode()) {
            logOut(this);
        } else if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_cases);
        } else {
            CaseFieldValueCache.clearAudioFile();
            turnToFeature(CaseFeature.LIST, null);
        }
    }

    @Override
    protected void search() {
        turnToFeature(TracingFeature.SEARCH, null);
    }

    @Override
    protected void save() {
        clearFocusToMakeLastFieldSaved();

        if (validateRequiredField()) {
            SaveTracingEvent event = new SaveTracingEvent();
            EventBus.getDefault().postSticky(event);
            turnToFeature(TracingFeature.LIST, null);
        }
    }

    @Override
    protected void showQuitDialog(final int clickedButton) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quit)
                .setMessage(R.string.quit_without_saving)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CaseFieldValueCache.clearAudioFile();
                        switch (clickedButton) {
                            case R.id.nav_cases:
                                turnToFeature(TracingFeature.LIST, null);
                                break;
                            case R.id.nav_sync:
                                intentSender.showSyncActivity(TracingActivity.this);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void clearFocusToMakeLastFieldSaved() {
        CaseRegisterWrapperFragment fragment =
                (CaseRegisterWrapperFragment) getSupportFragmentManager()
                        .findFragmentByTag(CaseRegisterWrapperFragment.class.getSimpleName());

        if (fragment != null) {
            fragment.clearFocus();
        }
    }

    private boolean validateRequiredField() {
        TracingFormRoot tracingForm = TracingFormService.getInstance().getCurrentForm();
        List<String> requiredFieldNames = new ArrayList<>();

        for (Section section : tracingForm.getSections()) {
            Collections.addAll(requiredFieldNames, RecordService.getInstance()
                    .fetchRequiredFiledNames(section.getFields()).toArray(new String[0]));
        }

        for (String field : requiredFieldNames) {
            if (TextUtils.isEmpty(CaseFieldValueCache.getValues().get(field))) {
                Toast.makeText(TracingActivity.this, R.string.required_field_is_not_filled,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }
}

