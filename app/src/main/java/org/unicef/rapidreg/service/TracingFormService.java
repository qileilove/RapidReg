package org.unicef.rapidreg.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.data.Blob;

import org.unicef.rapidreg.db.TracingFormDao;
import org.unicef.rapidreg.db.impl.TracingFormDaoImpl;
import org.unicef.rapidreg.forms.TracingFormRoot;
import org.unicef.rapidreg.model.TracingForm;

public class TracingFormService {
    public static final String TAG = TracingFormService.class.getSimpleName();
    private static final TracingFormService TRACING_REQUEST_FORM_SERVICE
            = new TracingFormService(new TracingFormDaoImpl());
    private static TracingFormRoot tracingRequestForm;
    private TracingFormDao tracingFormDao;

    public static TracingFormService getInstance() {
        return TRACING_REQUEST_FORM_SERVICE;
    }

    public TracingFormService(TracingFormDao tracingFormDao) {
        this.tracingFormDao = tracingFormDao;
    }

    public boolean isFormReady() {
        Blob form = tracingFormDao.getTracingRequestFormContent();

        return form != null;
    }

    public TracingFormRoot getCurrentForm() {
        if (tracingRequestForm == null) {
            updateCachedForm();
        }
        return tracingRequestForm;
    }

    public void saveOrUpdateCaseForm(TracingForm tracingForm) {
        TracingForm existingTracingForm = tracingFormDao.getTracingRequestForm();

        if (existingTracingForm == null) {
            Log.d(TAG, "save new tracing request form");
            tracingForm.save();
        } else {
            Log.d(TAG, "update existing tracingRequest form");
            existingTracingForm.setForm(tracingForm.getForm());
            existingTracingForm.update();
        }

        updateCachedForm();
    }

    private void updateCachedForm() {
        Blob form = tracingFormDao.getTracingRequestFormContent();

        String formJson = new String(form.getBlob());
        tracingRequestForm = TextUtils.isEmpty(formJson) ?
                null : new Gson().fromJson(formJson, TracingFormRoot.class);
    }
}
