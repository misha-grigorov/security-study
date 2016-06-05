package com.dataart.security.utils;

public class GmailLabelInfo {

    private String id;
    private String name;
    private String messageListVisibility;
    private String labelListVisibility;
    private String type;

    public GmailLabelInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageListVisibility() {
        return messageListVisibility;
    }

    public void setMessageListVisibility(String messageListVisibility) {
        this.messageListVisibility = messageListVisibility;
    }

    public String getLabelListVisibility() {
        return labelListVisibility;
    }

    public void setLabelListVisibility(String labelListVisibility) {
        this.labelListVisibility = labelListVisibility;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
