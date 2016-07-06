package org.unicef.rapidreg.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.data.Blob;

import org.unicef.rapidreg.db.CaseFormDao;
import org.unicef.rapidreg.db.TracingRequestFormDao;
import org.unicef.rapidreg.db.impl.CaseFormDaoImpl;
import org.unicef.rapidreg.db.impl.TracingRequestFormDaoImpl;
import org.unicef.rapidreg.forms.childcase.CaseFormRoot;
import org.unicef.rapidreg.model.CaseForm;
import org.unicef.rapidreg.model.TracingRequestForm;

public class TracingRequestFormService {
    public static final String TAG = TracingRequestFormService.class.getSimpleName();
    private static final TracingRequestFormService TRACING_REQUEST_FORM_SERVICE
            = new TracingRequestFormService(new TracingRequestFormDaoImpl());
    private static TracingRequestForm tracingRequestForm;
    private TracingRequestFormDao tracingRequestFormDao;

    public static TracingRequestFormService getInstance() {
        return TRACING_REQUEST_FORM_SERVICE;
    }

    public TracingRequestFormService(TracingRequestFormDao tracingRequestFormDao) {
        this.tracingRequestFormDao = tracingRequestFormDao;
    }

    public boolean isFormReady() {
        Blob form = tracingRequestFormDao.getTracingRequestFormContent();

        return form != null;
    }

    public TracingRequestForm getCurrentForm() {
        if (tracingRequestForm == null) {
            updateCachedForm();
        }
        return tracingRequestForm;
    }

    public void saveOrUpdateCaseForm(TracingRequestForm tracingRequestForm) {
        TracingRequestForm existingTracingRequestForm = tracingRequestFormDao.getTracingRequestForm();

        if (existingTracingRequestForm == null) {
            Log.d(TAG, "save new tracing request form");
            tracingRequestForm.save();
        } else {
            Log.d(TAG, "update existing tracingRequest form");
            existingTracingRequestForm.setForm(tracingRequestForm.getForm());
            existingTracingRequestForm.update();
        }

        updateCachedForm();
    }

    private void updateCachedForm() {
        Blob form = tracingRequestFormDao.getTracingRequestFormContent();

        String formJson = new String(form.getBlob());
        tracingRequestForm = TextUtils.isEmpty(formJson) ?
                null : new Gson().fromJson(formJson, TracingRequestForm.class);
    }

    public static class FormLoadStateMachine {
        private int retryNum = 0;
        private int maxRetryNum;

        public static FormLoadStateMachine getInstance(int maxRetryNum) {
            return new FormLoadStateMachine(maxRetryNum);
        }

        private FormLoadStateMachine(int maxRetryNum) {
            this.maxRetryNum = maxRetryNum;
            retryNum = 0;
        }

        public void addOnce() {
            retryNum++;
        }

        public boolean hasReachMaxRetryNum() {
            return retryNum >= maxRetryNum;
        }

        public int getCurrentNum() {
            return retryNum;
        }
    }
}
