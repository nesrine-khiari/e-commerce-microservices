package com.accountservice.upcasters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.axonframework.serialization.SerializedType;
import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;

@Order(0)
@Component
public class WithdrawMoneyEvent0_to_1Upcaster extends SingleEventUpcaster {

    private static final String PAYLOAD_TYPE = "com.accountservice.event.WithdrawMoneyEvent";

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation intermediateRepresentation) {
        SerializedType payloadType = intermediateRepresentation.getData().getType();
        return PAYLOAD_TYPE.equals(payloadType.getName())
                && payloadType.getRevision() == null;
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation intermediateRepresentation) {
        return intermediateRepresentation.upcastPayload(
                new SimpleSerializedType(PAYLOAD_TYPE, "1"),
                JsonNode.class,
                event -> {
                    if (!(event instanceof ObjectNode)) {
                        return event;
                    }
                    ((ObjectNode) event).put("timestamp", new Date().toString() + "UPCASTED DATE");
                    return event;
                }
        );
    }
}
