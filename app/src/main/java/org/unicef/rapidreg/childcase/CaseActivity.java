package org.unicef.rapidreg.childcase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.unicef.rapidreg.IntentSender;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordActivity;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.utils.Utils;

public class CaseActivity extends RecordActivity {
    public static final String TAG = CaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationView.setCheckedItem(R.id.nav_cases);
        navigationView.setItemTextColor(caseColor);

        boolean showAddPage = getIntent().getBooleanExtra(IntentSender.SHOW_ADD_PAGE, false);
        turnToFeature(showAddPage ? CaseFeature.ADD_MINI : CaseFeature.LIST, null, null);
    }

    @Override
    protected void processBackButton() {
        if (currentFeature.isListMode()) {
            logOut(this);
        } else if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_cases);
        } else {
            CaseService.clearAudioFile();
            turnToFeature(CaseFeature.LIST, null, null);
        }
    }

    @Override
    protected void navCaseAction() {
        if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_cases);
        } else {
            CaseService.clearAudioFile();
            turnToFeature(CaseFeature.LIST, null, null);
        }
    }

    @Override
    protected void navTracingAction() {
        if (currentFeature.isEditMode()) {
            showQuitDialog(R.id.nav_tracing);
        } else {
            CaseService.clearAudioFile();
            intentSender.showTracingActivity(this);
        }
    }

    @Override
    protected void showQuitDialog(final int clickedButton) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.quit)
                .setMessage(R.string.quit_without_saving)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CaseService.clearAudioFile();
                        switch (clickedButton) {
                            case R.id.nav_cases:
                                turnToFeature(CaseFeature.LIST, null, null);
                                break;
                            case R.id.nav_tracing:
                                intentSender.showTracingActivity(CaseActivity.this);
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

        Utils.changeDialogDividerColor(this, dialog);
    }

    @Override
    protected void showHideDetail() {
        textAreaState = textAreaState.getNextState();

        showHideMenu.setIcon(textAreaState.getResId());
        CaseListFragment listFragment = (CaseListFragment) getSupportFragmentManager()
                .findFragmentByTag(CaseListFragment.class.getSimpleName());
        listFragment.toggleMode(textAreaState.isDetailShow());
    }

    @Override
    protected void search() {
        turnToFeature(CaseFeature.SEARCH, null, null);
    }

    @Override
    protected void save() {
        SaveCaseEvent event = new SaveCaseEvent();
        EventBus.getDefault().postSticky(event);
    }
}
