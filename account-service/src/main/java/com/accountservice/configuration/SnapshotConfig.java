package com.accountservice.configuration;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnapshotConfig {
    @Bean(name = "accountSnapshotTriggerDefinition")
    public SnapshotTriggerDefinition accountSnapshotTriggerDefinition(Snapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, 10);
        //A snapshot is triggered when the number of events applied on an aggregate exceeds the given threshold.
    }
}
