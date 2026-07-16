package poc.impl;

import poc.spi.Check;
import poc.spi.CheckContext;
import poc.split.SplitTypedConstraint;

import java.util.List;

public final class SplitTypedCheck implements Check {
    private Class<SplitTypedConstraint> controlType = SplitTypedConstraint.class;

    @Override
    public List<String> check(CheckContext context) {
        return List.of(controlType.getName());
    }
}
