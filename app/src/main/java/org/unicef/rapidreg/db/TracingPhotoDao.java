package org.unicef.rapidreg.db;

import org.unicef.rapidreg.model.TracingPhoto;

import java.util.List;

public interface TracingPhotoDao {
    List<TracingPhoto> getAllTracingsPhoto(long tracingId);

    void deleteTracingPhotosByCaseId(long tracingId);
}
