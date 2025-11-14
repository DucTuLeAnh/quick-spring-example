package com.example.clientExample.app.entities.view;


import java.util.stream.Stream;

public record FWSearchResultEntryKey(String projectID, String bookingNumber, String projectBinderID) {

    boolean isValidMainKey(){
        return Stream.of(projectID, bookingNumber, projectBinderID).allMatch(s -> s != null && !s.isBlank());
    }

    boolean isValidBackupKey(){
        return !isValidMainKey() && Stream.of(projectID, bookingNumber).allMatch(s -> s != null && !s.isBlank());
    }
}
