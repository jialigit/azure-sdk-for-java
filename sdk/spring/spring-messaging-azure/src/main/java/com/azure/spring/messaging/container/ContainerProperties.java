package com.azure.spring.messaging.container;

public class ContainerProperties {
    private String destination;
    private String group;

    public ContainerProperties(String destination, String group) {
        this.destination = destination;
        this.group = group;
    }

    public String getDestination() {
        return destination;
    }

    public String getGroup() {
        return group;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
