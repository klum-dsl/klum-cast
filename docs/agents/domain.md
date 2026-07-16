# Domain documentation

KlumCast uses a single-context layout:

```text
/
├── CONTEXT.md
├── docs/
│   └── adr/
└── klum-cast-*/
```

Before architecture or API work, read root `CONTEXT.md` and ADRs relevant to the area. Use the glossary's canonical terms
in issues, tests, documentation, and proposals. Surface conflicts with accepted ADRs instead of silently overriding them.

`CONTEXT.md` is a glossary and product-language boundary, not an implementation specification. Add an ADR only for an
accepted decision that is hard to reverse, surprising without context, and the result of a real trade-off. ADR filenames
use sequential numbers such as `0001-public-check-spi.md`.
