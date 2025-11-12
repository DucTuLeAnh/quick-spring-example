package com.example.clientExample.app.entities;

import java.util.List;

public record FWObjectResponse(Integer isError, List<FWObject> objects) {
}
