package org.unicef.rapidreg.childcase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.forms.CaseFormRoot;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.service.CaseFormService;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CaseActivity extends RequestActivity {
    public static final String TAG = CaseActivity.class.getSimpleName();

    @Override
    protected void navSyncAction() {
        if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_sync);
        } else {
            CaseFieldValueCache.clearAudioFile();
            intentSender.showSyncActivity(this);
        }
    }

    @Override
    protected void navCaseAction() {
        if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_cases);
        } else {
            CaseFieldValueCache.clearAudioFile();
            turnToFeature(Feature.LIST, null);
        }
    }

    @Override
    protected void navTracingAction() {
        if (currentFeature.isDetailMode()) {
            showQuitDialog(R.id.nav_cases);
        } else {
            CaseFieldValueCache.clearAudioFile();
            intentSender.showTracingActivity(this);
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
            turnToFeature(Feature.LIST, null);
        }
    }

    @Override
    protected void save() {
        clearFocusToMakeLastFieldSaved();

        if (validateRequiredField()) {
            SaveCaseEvent event = new SaveCaseEvent();
            EventBus.getDefault().postSticky(event);
            turnToFeature(Feature.LIST, null);
        }
    }

    private void clearFocusToMakeLastFieldSaved() {
        RegisterWrapperFragment fragment =
                (RegisterWrapperFragment) getSupportFragmentManager()
                        .findFragmentByTag(RegisterWrapperFragment.class.getSimpleName());

        if (fragment != null) {
            fragment.clearFocus();
        }
    }

    private boolean validateRequiredField() {
        CaseFormRoot caseForm = CaseFormService.getInstance().getCurrentForm();
        List<String> requiredFieldNames = new ArrayList<>();

        for (Section section : caseForm.getSections()) {
            Collections.addAll(requiredFieldNames, CaseService.getInstance()
                    .fetchRequiredFiledNames(section.getFields()).toArray(new String[0]));
        }

        for (String field : requiredFieldNames) {
            if (TextUtils.isEmpty(CaseFieldValueCache.getValues().get(field))) {
                Toast.makeText(CaseActivity.this, R.string.required_field_is_not_filled,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void showQuitDialog(final int clickedButton) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quit)
                .setMessage(R.string.quit_without_saving)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CaseFieldValueCache.clearAudioFile();
                        switch (clickedButton) {
                            case R.id.nav_cases:
                                turnToFeature(Feature.LIST, null);
                                break;
                            case R.id.nav_sync:
                                intentSender.showSyncActivity(CaseActivity.this);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
