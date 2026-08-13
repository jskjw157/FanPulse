'use client';

import { useState } from 'react';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useNotifications } from '@/hooks/useNotifications';

function formatCreatedAt(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  }).format(new Date(value));
}

function NotificationsContent() {
  const [unreadOnly, setUnreadOnly] = useState(false);
  const { notifications, state, error, retry, markAllRead, markingAll } = useNotifications(unreadOnly);
  const unreadCount = notifications.filter((row) => !row.isRead).length;

  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader
        title="알림"
        rightAction={
          unreadCount > 0 ? (
            <button
              type="button"
              onClick={() => void markAllRead()}
              disabled={markingAll}
              className="text-sm font-semibold text-purple-700 disabled:opacity-50"
            >
              모두 읽음 처리
            </button>
          ) : undefined
        }
      />
      <PageWrapper>
        <main className="mx-auto max-w-3xl px-4 py-6">
          <div className="mb-5 flex gap-2" role="group" aria-label="알림 필터">
            <button
              type="button"
              onClick={() => setUnreadOnly(false)}
              aria-pressed={!unreadOnly}
              className={`rounded-full px-4 py-2 text-sm font-semibold ${!unreadOnly ? 'bg-purple-600 text-white' : 'bg-white text-gray-600'}`}
            >
              전체
            </button>
            <button
              type="button"
              onClick={() => setUnreadOnly(true)}
              aria-pressed={unreadOnly}
              className={`rounded-full px-4 py-2 text-sm font-semibold ${unreadOnly ? 'bg-purple-600 text-white' : 'bg-white text-gray-600'}`}
            >
              읽지 않음
            </button>
          </div>

          {state === 'loading' && <p className="py-16 text-center text-gray-500">알림을 불러오는 중입니다</p>}

          {state === 'error' && (
            <div className="py-16 text-center">
              <p className="text-gray-600">{error}</p>
              <button
                type="button"
                onClick={() => void retry()}
                className="mt-4 rounded-full bg-purple-600 px-5 py-2 text-sm font-semibold text-white"
              >
                다시 시도
              </button>
            </div>
          )}

          {state === 'success' && notifications.length === 0 && (
            <div className="py-16 text-center">
              <i className="ri-notification-off-line text-5xl text-gray-300" aria-hidden="true" />
              <p className="mt-4 text-gray-600">
                {unreadOnly ? '읽지 않은 알림이 없습니다' : '알림이 없습니다'}
              </p>
            </div>
          )}

          {state === 'success' && notifications.length > 0 && (
            <ul className="space-y-3">
              {notifications.map((notification) => (
                <li
                  key={notification.id}
                  className={`rounded-2xl border p-4 ${notification.isRead ? 'border-gray-200 bg-white' : 'border-purple-200 bg-purple-50'}`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      {notification.type && (
                        <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-purple-700">{notification.type}</p>
                      )}
                      <p className="text-sm leading-6 text-gray-900">{notification.message}</p>
                    </div>
                    {!notification.isRead && <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-purple-600" aria-label="읽지 않음" />}
                  </div>
                  <time className="mt-2 block text-xs text-gray-500" dateTime={notification.createdAt}>
                    {formatCreatedAt(notification.createdAt)}
                  </time>
                </li>
              ))}
            </ul>
          )}
        </main>
      </PageWrapper>
    </div>
  );
}

export default function NotificationsPage() {
  return (
    <ProtectedRoute>
      <NotificationsContent />
    </ProtectedRoute>
  );
}
