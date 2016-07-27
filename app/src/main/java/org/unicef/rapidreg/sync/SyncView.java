package org.unicef.rapidreg.sync;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface SyncView extends MvpView {

    void showSyncProgressDialog();

    void hideSyncProgressDialog();

    void showSyncCancelConfirmDialog();

    void showSyncErrorMessage();

    void showSyncSuccessMessage(String msg);

    void setDataViews(String syncDate, String hasSyncAmount, String notSyncAmount);

    void setProgressMax(int max);

    void setProgressIncrease();

    void showAttemptSyncDialog();

    void setSyncProgressDialogTitle(String title);
}
