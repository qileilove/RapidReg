package org.unicef.rapidreg.db;

import com.raizlabs.android.dbflow.data.Blob;

import org.unicef.rapidreg.model.CaseForm;
import org.unicef.rapidreg.model.TracingRequestForm;

public interface TracingRequestFormDao {
    TracingRequestForm getTracingRequestForm();

    Blob getTracingRequestFormContent();
}
