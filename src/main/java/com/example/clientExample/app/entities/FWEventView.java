package com.example.clientExample.app.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.List;

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
