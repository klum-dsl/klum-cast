# Override diagnostic messages by code

Checks own default messages and stable check-scoped diagnostic codes. A validation annotation may expose optional
message-template members keyed to those codes, using named structured arguments rather than positional formatting. In a
composed validation annotation, the nearest applicable override in the composition path wins; absent overrides use the
check default. Unknown codes and invalid templates are technical configuration failures. Exact annotation names and
template syntax remain implementation details to select with issue #17.
