package org.unicef.rapidreg.forms.tracing_request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TracingRequestFormRoot {
    @SerializedName("Enquiries")
    @Expose
    private List<TracingRequestSection> sections = new ArrayList<>();

    public List<TracingRequestSection> getSections() {
        return sections;
    }

    public void setSections(List<TracingRequestSection> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<Enquiries>").append("\n");
        for (TracingRequestSection section : sections) {
            sb.append(section).append("\n");
        }
        return sb.toString();
    }
}
