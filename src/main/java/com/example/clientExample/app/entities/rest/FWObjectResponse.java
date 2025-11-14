package com.example.clientExample.app.entities.rest;

import java.util.List;

public record FWObjectResponse(Integer isError, List<FWObject> objects) {
}
