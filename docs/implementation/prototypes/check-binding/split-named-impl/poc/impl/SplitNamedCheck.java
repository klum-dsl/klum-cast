package poc.impl;

import poc.spi.Check;
import poc.spi.CheckContext;
import poc.split.SplitNamedConstraint;

import java.util.List;

public final class SplitNamedCheck implements Check {
    private Class<SplitNamedConstraint> controlType = SplitNamedConstraint.class;

    @Override
    public List<String> check(CheckContext context) {
        return List.of(controlType.getName());
    }
}
