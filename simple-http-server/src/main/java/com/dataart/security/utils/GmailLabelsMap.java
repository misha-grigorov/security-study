package com.dataart.security.utils;

import java.util.HashMap;
import java.util.List;

public class GmailLabelsMap extends HashMap<String, List<GmailLabelInfo>> {
    private List<GmailLabelInfo> labels;

    public List<GmailLabelInfo> getLabels() {
        return labels;
    }

    public void setLabels(List<GmailLabelInfo> labels) {
        this.labels = labels;
    }
}
