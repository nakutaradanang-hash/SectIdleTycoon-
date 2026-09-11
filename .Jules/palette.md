## 2025-02-18 - Pre-selecting Active State in Selection Dialogs
**Learning:** In modal selection dialogs (e.g. task assignment RadioGroups), leaving all options unselected obscures the user's current selection and risks destructive default behavior (such as resetting an active task to NONE upon accidental confirmation).
**Action:** Always retrieve the target entity's active state and pre-select/check the corresponding UI control when initializing modal dialogs.
