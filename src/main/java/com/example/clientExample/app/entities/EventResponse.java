package com.example.clientExample.app.entities;

import java.util.List;

public record EventResponse(Boolean isError, Integer cursor, List<Event> events) {
}
