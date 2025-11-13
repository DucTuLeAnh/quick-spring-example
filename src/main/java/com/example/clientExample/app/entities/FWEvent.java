package com.example.clientExample.app.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;


//todo: add customs object
public record FWEvent(String type,
                      String eventID,
                      String objectID,
                      String projectID,
                      String mainHeader,
                      String projectName,
                      String projectBinderName,
                      String objectName,
                      @JsonDeserialize(using = SafeLocalDateTimeDeserializer.class)
                      LocalDateTime dateTimeInAsString,
                      @JsonDeserialize(using = SafeLocalDateTimeDeserializer.class)
                      LocalDateTime dateTimeOutAsString) {





}
