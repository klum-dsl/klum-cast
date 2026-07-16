package poc.probe;

import poc.consumer.InvalidRawConstraint;
import poc.consumer.NamedConstraint;
import poc.metadata.NamedCheckBinding;
import poc.metadata.RawCheckBinding;
import poc.spi.Check;

public final class BindingProbe {
    private BindingProbe() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> rawType = InvalidRawConstraint.class.getAnnotation(RawCheckBinding.class).value();
        if (Check.class.isAssignableFrom(rawType)) {
            throw new AssertionError("Raw binding unexpectedly rejected no invalid type");
        }

        String nestedName = NamedConstraint.class.getAnnotation(NamedCheckBinding.class).value();
        Class<?> namedType = Class.forName(nestedName);
        if (!Check.class.isAssignableFrom(namedType)) {
            throw new AssertionError("Named nested check did not resolve as a Check");
        }

        System.out.println("raw-invalid-runtime-check=" + rawType.getName());
        System.out.println("named-nested-resolved=" + namedType.getName());
    }
}
