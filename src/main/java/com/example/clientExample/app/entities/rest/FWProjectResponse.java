package com.example.clientExample.app.entities.rest;

import java.util.List;

public record FWProjectResponse(Integer isError, List<FWProject> projects, Integer nextCursor) {
}
