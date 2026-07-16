package poc.probe;

import poc.compat.Binding;
import poc.compat.CompatConstraint;

public final class SignatureProbe {
    private SignatureProbe() {
    }

    public static void main(String[] args) {
        Class<?> value = CompatConstraint.class.getAnnotation(Binding.class).value();
        System.out.println("typed-consumer-with-raw-binding=" + value.getName());
    }
}
