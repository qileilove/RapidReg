package org.unicef.rapidreg.model;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.unicef.rapidreg.db.PrimeroDB;

@Table(database = PrimeroDB.class)
public class Tracing extends BaseModel {
}
