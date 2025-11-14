package com.example.clientExample.app.entities.view;

public record FWEventView(
                          String mainHeader,
                          String projectName,
                          String projectBinderName,
                          String objectName,
                          String dateTimeInAsString,
                          String dateTimeOutAsString,
                          String note,
                          String customModerator,
                          String customArt) {
}
