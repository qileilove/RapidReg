package org.unicef.rapidreg.db.impl;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.unicef.rapidreg.db.CaseFormDao;
import org.unicef.rapidreg.db.TracingRequestFormDao;
import org.unicef.rapidreg.model.CaseForm;
import org.unicef.rapidreg.model.TracingRequestForm;

public class TracingRequestFormDaoImpl implements TracingRequestFormDao {

    @Override
    public TracingRequestForm getTracingRequestForm() {
        return SQLite.select().from(TracingRequestForm.class).querySingle();
    }

    @Override
    public Blob getTracingRequestFormContent() {
        TracingRequestForm tracingRequestForm = SQLite.select().from(TracingRequestForm.class).querySingle();

        return tracingRequestForm == null ? null : tracingRequestForm.getForm();
    }
}
