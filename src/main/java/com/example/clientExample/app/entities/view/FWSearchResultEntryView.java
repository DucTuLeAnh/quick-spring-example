package com.example.clientExample.app.entities.view;

import java.time.LocalDateTime;
import java.util.List;

public record FWSearchResultEntryView(
        FWSearchResultEntryKey mainKey,
        String entryHeader,
        List<String> objectNames,
        String dateTimeInAsString,
        String dateTimeOutAsString,
        LocalDateTime sortTime,
        List<String> notes,
        List<String> customModerators,
        List<String> customArts) {
}
