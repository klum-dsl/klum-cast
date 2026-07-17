module consumer.typed {
    requires com.blackbuild.klum.cast.annotations;
    requires com.blackbuild.klum.cast.spi;
    requires @GROOVY_MODULE@;

    exports fixture.typed;
}
