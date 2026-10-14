module consumer.contributor {
    requires com.blackbuild.klum.cast.compiler;
    requires consumer.typed;
    requires @GROOVY_MODULE@;

    exports fixture.contributor;

    provides com.blackbuild.klum.cast.compiler.EffectiveDeclarationContributor
            with fixture.contributor.TypedContributor;
}
