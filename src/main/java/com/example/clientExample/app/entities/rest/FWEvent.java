package com.example.clientExample.app.entities.rest;

import com.example.clientExample.shared.SafeLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.List;


//todo: add customs object
public record FWEvent(String type,
                      String eventID,
                      String mainHeader,
                      String objectID,
                      String objectName,
                      String projectID,
                      String projectName,
                      String projectBinderID,
                      String projectBinderName,
                      @JsonDeserialize(using = SafeLocalDateTimeDeserializer.class)
                      LocalDateTime dateTimeInAsString,
                      @JsonDeserialize(using = SafeLocalDateTimeDeserializer.class)
                      LocalDateTime dateTimeOutAsString,
                      String note,
                      List<FWCustoms> customs) {





}
