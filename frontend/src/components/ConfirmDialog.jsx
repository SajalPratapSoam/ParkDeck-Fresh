export default function ConfirmDialog({
  open,
  title,
  body,
  confirmLabel = 'Confirm',
  busy = false,
  onConfirm,
  onClose,
}) {
  if (!open) {
    return null;
  }
  return (
    <div className="fixed inset-0 z-40 flex items-end justify-center bg-ink/40 p-4 sm:items-center">
      <div role="dialog" aria-modal="true" className="w-full max-w-md rounded-3xl bg-paper p-6 shadow-2xl">
        <h2 className="text-3xl">{title}</h2>
        <p className="mt-3 text-sm leading-6 text-muted">{body}</p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-full px-4 py-2 text-sm font-semibold text-ink"
          >
            Go back
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={onConfirm}
            className="rounded-full bg-forest px-4 py-2 text-sm font-semibold text-cream disabled:opacity-60"
          >
            {busy ? 'Working…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
