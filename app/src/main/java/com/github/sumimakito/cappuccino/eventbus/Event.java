/*
 * Copyright 2013-2017 Sumi Makito
 * Copyright 2016-2017 Bitcat Interactive Lab.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Sumi Makito <sumimakito@hotmail.com>, June 2016
 */

package com.github.sumimakito.cappuccino.eventbus;

public class Event {
    private Object eventBody;
    private String eventHeader;

    private Event(String header) {
        this.eventHeader = header;
    }

    public static Event create(String header) {
        return new Event(header);
    }

    public static Event duplicate(Event event) {
        return new Event(event.header());
    }

    public Object body() {
        return eventBody;
    }

    public Event body(Object eventBody) {
        this.eventBody = eventBody;
        return this;
    }

    public String header() {
        return eventHeader;
    }

    public boolean equals(Event event) {
        return event != null && header().equals(event.header());
    }
}
