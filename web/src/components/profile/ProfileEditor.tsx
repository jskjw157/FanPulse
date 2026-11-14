"use client";

import { useEffect, useId, useRef, useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/contexts/AuthContext";
import { updateMyProfile } from "@/lib/api/user";
import { FanPulseApiError } from "@/lib/api-client";

export default function ProfileEditor({ username, onSaved }: {
  username: string;
  onSaved: () => void;
}) {
  const { user, isLoading, updateUser } = useAuth();
  const { t } = useTranslation();
  const inputId = useId();
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(username);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const request = useRef<AbortController | null>(null);

  // MyPage keys the editor by account + sessionVersion, discarding drafts on revalidation.
  useEffect(() => () => request.current?.abort(), []);

  async function save(event: FormEvent) {
    event.preventDefault();
    const normalized = draft.trim();
    if (!/^[a-zA-Z0-9_-]{2,50}$/.test(normalized)) {
      setError("profile.invalid");
      return;
    }
    if (saving || isLoading || !user) return;
    const controller = new AbortController();
    request.current = controller;
    setSaving(true);
    setError(null);
    try {
      const updated = await updateMyProfile(normalized, user.id, controller.signal);
      if (controller.signal.aborted) return;
      if (updated.id !== user?.id) throw new Error("Profile user mismatch");
      updateUser(updated);
      setEditing(false);
      onSaved();
    } catch (cause) {
      if (controller.signal.aborted) return;
      setError(cause instanceof FanPulseApiError && cause.code === "USERNAME_ALREADY_EXISTS"
        ? "profile.duplicate"
        : cause instanceof FanPulseApiError && (cause.code === "VALIDATION_FAILED" || cause.code === "INVALID_REQUEST")
          ? "profile.invalid"
          : "profile.failed");
    } finally {
      if (!controller.signal.aborted) setSaving(false);
    }
  }

  if (isLoading || !user) return null;

  if (!editing) {
    return <button type="button" className="mt-3 rounded-xl border border-white/60 px-4 py-2 text-sm font-medium text-white hover:bg-white/10"
      onClick={() => { setDraft(username); setError(null); setEditing(true); }}>
      {t("profile.edit")}
    </button>;
  }

  return (
    <form onSubmit={save} className="mt-4 rounded-2xl bg-white p-4 text-gray-900" aria-busy={saving} noValidate>
      <label htmlFor={inputId} className="block text-sm font-semibold">{t("profile.username")}</label>
      <p id={`${inputId}-hint`} className="mt-1 text-xs text-gray-600">{t("profile.hint")}</p>
      <input id={inputId} value={draft} autoFocus disabled={saving} autoComplete="nickname"
        aria-invalid={!!error} aria-describedby={`${inputId}-hint${error ? ` ${inputId}-error` : ""}`}
        onChange={(event) => { setDraft(event.target.value); setError(null); }}
        className="mt-2 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-purple-600 disabled:opacity-60" />
      {error && <p id={`${inputId}-error`} role="alert" className="mt-2 text-sm text-red-700">{t(error)}</p>}
      <div className="mt-3 flex gap-2">
        <button type="submit" disabled={saving} className="rounded-lg bg-purple-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50">
          {t(saving ? "common.saving" : "common.save")}
        </button>
        <button type="button" disabled={saving} onClick={() => setEditing(false)} className="rounded-lg border border-gray-300 px-4 py-2 text-sm disabled:opacity-50">
          {t("common.cancel")}
        </button>
      </div>
    </form>
  );
}
