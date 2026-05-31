package io.github.weiz1103.agentflow.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data @SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitEvent extends BaseEvent {
    @Override public String getType() { return "wait"; }
}

