package org.unicef.rapidreg.db.impl;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.unicef.rapidreg.db.TracingPhotoDao;
import org.unicef.rapidreg.model.CasePhoto;
import org.unicef.rapidreg.model.CasePhoto_Table;
import org.unicef.rapidreg.model.TracingPhoto;
import org.unicef.rapidreg.model.TracingPhoto_Table;

import java.util.List;

public class TracingPhotoDaoImpl implements TracingPhotoDao {
    @Override
    public List<TracingPhoto> getAllTracingsPhoto(long tracingId) {
        return SQLite.select()
                .from(TracingPhoto.class)
                .where(TracingPhoto_Table.tracing_id.eq(tracingId))
                .queryList();
    }

    public void deleteTracingPhotosByCaseId(long tracingId) {
        SQLite.delete().from(CasePhoto.class).where(CasePhoto_Table.case_id.eq(tracingId)).execute();
    }
}
