package com.shipmentservice.query;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindShipmentsQuery {
    private final int offset;
    private final int limit;
}
