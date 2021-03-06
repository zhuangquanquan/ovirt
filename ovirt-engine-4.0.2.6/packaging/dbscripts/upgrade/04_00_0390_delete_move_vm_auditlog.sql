UPDATE audit_log
SET deleted = true
WHERE log_type IN (
  82, -- USER_MOVED_VM
  83, -- USER_FAILED_MOVE_VM
  84, -- USER_MOVED_TEMPLATE
  85, -- USER_FAILED_MOVE_TEMPLATE
  91, -- USER_MOVED_VM_FINISHED_SUCCESS
  92, -- USER_MOVED_VM_FINISHED_FAILURE
  93, -- USER_MOVED_TEMPLATE_FINISHED_SUCCESS
  94  -- USER_MOVED_TEMPLATE_FINISHED_FAILURE
);
