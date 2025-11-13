package com.example.clientExample.app.entities;

import java.util.List;

public record FWEventResponse(Boolean isError, Integer nextCursor, List<FWEvent> events) {
}
