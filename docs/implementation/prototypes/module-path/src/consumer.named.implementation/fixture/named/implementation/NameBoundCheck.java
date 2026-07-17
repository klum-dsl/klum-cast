package fixture.named.implementation;

import com.blackbuild.klum.cast.spi.Check;
import com.blackbuild.klum.cast.spi.CheckContext;
import com.blackbuild.klum.cast.spi.Diagnostic;

import java.util.List;

public final class NameBoundCheck implements Check {
    @Override
    public List<Diagnostic> check(CheckContext context) {
        System.out.println("NAME-CHECK-RAN");
        return List.of();
    }
}
