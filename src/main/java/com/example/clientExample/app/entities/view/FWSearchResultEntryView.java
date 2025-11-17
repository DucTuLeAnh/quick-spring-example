package com.example.clientExample.app.entities.view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record FWSearchResultEntryView(
        FWSearchResultEntryKey mainKey,
        String entryHeader,
        Set<String> projectIds,
        Set<String> objectIds,
        List<String> objectNames,
        String timeInAsString,
        String timeOutAsString,
        String dateInAsString,
        String dateOutAsString,
        LocalDateTime sortTime,
        List<String> notes,
        List<String> customModerators,
        List<String> customArts) {

}
