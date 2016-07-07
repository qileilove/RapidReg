package org.unicef.rapidreg.tracing;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.view.BaseActivity;

public class TracingRequestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNavMenuItem(R.id.nav_tracing);
    }

    @Override
    protected void navSyncAction() {
        intentSender.showSyncActivity(this);
    }

    @Override
    protected void navCaseAction() {
        intentSender.showCasesActivity(this, null, false);
    }

    @Override
    protected void navTracingRequestAction() {

    }

    @Override
    protected void processBackButton() {
        logOut(this);
    }
}
