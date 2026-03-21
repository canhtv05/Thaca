package com.thaca.auth.dtos.search;

import java.util.List;

public record SearchResponse<T>(List<T> data, Long totalCount) {}
