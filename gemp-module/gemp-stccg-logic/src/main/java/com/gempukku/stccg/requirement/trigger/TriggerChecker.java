package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.requirement.Requirement;

public interface TriggerChecker extends Requirement {
    boolean isBefore();
}
