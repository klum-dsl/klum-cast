# Service loading is the only supported activation API

Service loading is KlumCast's only supported activation API at this stage. The compiler engine should remain internally
separable and directly testable, but KlumCast does not promise a public manual invocation hook without a concrete
compiler-embedding use case and a defined lifecycle contract. This avoids freezing Groovy compiler orchestration details
as an additional public SPI.
