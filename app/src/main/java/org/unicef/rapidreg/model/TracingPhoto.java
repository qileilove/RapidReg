package org.unicef.rapidreg.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.unicef.rapidreg.db.PrimeroDB;

@Table(database = PrimeroDB.class)
@ModelContainer
public class TracingPhoto extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    Blob photo;

    @Column
    Blob thumbnail;

    @ForeignKey(references = {@ForeignKeyReference(
            columnName = "tracing_id",
            columnType = long.class,
            foreignKeyColumnName = "id"
    )})
    Tracing tracing;

    @Column
    String path;

    public TracingPhoto() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Blob getPhoto() {
        return photo;
    }

    public void setPhoto(Blob photo) {
        this.photo = photo;
    }

    public String getPath() {
        return path;
    }

    public Tracing getTracing() {
        return tracing;
    }

    public void setTracing(Tracing tracing) {
        this.tracing = tracing;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Blob getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Blob thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "TracingPhoto{" +
                "id=" + id +
                ", photo=" + photo +
                ", thumbnail=" + thumbnail +
                ", tracing=" + tracing +
                ", path='" + path + '\'' +
                '}';
    }
}

