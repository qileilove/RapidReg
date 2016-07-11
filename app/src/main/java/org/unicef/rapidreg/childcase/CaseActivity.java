package org.unicef.rapidreg.childcase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.data.Blob;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.event.NeedLoadFormsEvent;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.forms.childcase.CaseFormRoot;
import org.unicef.rapidreg.forms.childcase.CaseSection;
import org.unicef.rapidreg.model.CaseForm;
import org.unicef.rapidreg.network.AuthService;
import org.unicef.rapidreg.service.CaseFormService;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class CaseActivity extends RequestActivity {
    public static final String TAG = CaseActivity.class.getSimpleName();

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscriptions = new CompositeSubscription();

        turnToFeature(Feature.LIST, null);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        subscriptions.clear();
    }

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

    public void turnToDetailOrEditPage(Feature feature, long caseId) {
        try {

            Bundle args = new Bundle();
            args.putLong("case_id", caseId);

            currentFeature = feature;

            turnToFeature(feature, args);
        } catch (Exception e) {
            e.printStackTrace();
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

        for (CaseSection section : caseForm.getSections()) {
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onNeedLoadFormsEvent(final NeedLoadFormsEvent event) {
        Log.d("fengbo", "Case Load form");
        EventBus.getDefault().removeStickyEvent(event);
        final Gson gson = new Gson();

        subscriptions.add(AuthService.getInstance().getFormRx(event.getCookie(),
                Locale.getDefault().getLanguage(), true, "case")
                .flatMap(new Func1<CaseFormRoot, Observable<CaseFormRoot>>() {
                    @Override
                    public Observable<CaseFormRoot> call(CaseFormRoot caseFormRoot) {
                        if (caseFormRoot == null) {
                            return Observable.error(new Exception());
                        }
                        return Observable.just(caseFormRoot);
                    }
                })
                .retry(3)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CaseFormRoot>() {
                    @Override
                    public void call(CaseFormRoot caseFormRoot) {

                        CaseFormRoot form = caseFormRoot;
                        CaseForm caseForm = new CaseForm(new Blob(gson.toJson(form).getBytes()));
                        CaseFormService.getInstance().saveOrUpdateCaseForm(caseForm);

                        Log.i(TAG, "load form successfully");

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.i(TAG, throwable.getMessage());
                    }
                }));
    }
}
