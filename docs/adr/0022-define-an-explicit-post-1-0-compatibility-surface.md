# Define an explicit post-1.0 compatibility surface

Beginning with 1.0, KlumCast treats published artifact coordinates and stable module names; exported metadata,
built-in-annotation, and SPI packages; public annotation/interface/data-type shape; diagnostic codes and structured
argument names; binding resolution and activation behavior; declared dependency edges; and the documented Java/Groovy
support matrix as compatibility commitments. Compiler internals, service-provider implementation names, built-in check
implementation names, default message wording, and rendering details are not API. Removing a supported Groovy generation
requires a major release unless it is already outside a documented support window.
