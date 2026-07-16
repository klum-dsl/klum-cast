# Coding style

Follow the conventions already established in the surrounding module.

## Imports and qualified names

- Import referenced Java and Groovy types and use their simple names in handwritten source.
- When two referenced types have the same simple name, import the more frequent type and qualify the other only at the
  ambiguous use sites.
- Fully qualified names are appropriate for string-valued class bindings, generated-source constraints, or another
  documented technical necessity. Keep the reason visible in the API or a short comment.
- Generated output is governed by its generator; handwritten templates should still use imports when possible.

Code review treats an unnecessary fully qualified name as a style violation rather than an optional preference.
