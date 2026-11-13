'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import PageHeader from '@/components/layout/PageHeader';
import PageWrapper from '@/components/layout/PageWrapper';
import { useNotifications } from '@/hooks/useNotifications';
import { useAuth } from '@/contexts/AuthContext';

function formatCreatedAt(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  }).format(new Date(value));
}

function NotificationsContent() {
  const [unreadOnly, setUnreadOnly] = useState(false);
  const { user } = useAuth();
  const router = useRouter();
  const {
    notifications, state, error, retry, markAllRead, markingAll, mutationError,
    markRead, readingId, notice, unreadCount, page, totalPages, last, goToPage,
  } = useNotifications(unreadOnly, user?.id);
  const busy = markingAll || readingId !== null;

  const openNotification = async (id: string) => {
    const fresh = await markRead(id);
    if (fresh?.targetType === 'POST' && fresh.targetId && fresh.targetAvailable === true) {
      router.push(`/post-detail?id=${encodeURIComponent(fresh.targetId)}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <PageHeader
        title="알림"
        rightAction={
          unreadCount > 0 ? (
            <button
              type="button"
              onClick={() => void markAllRead()}
              disabled={busy || state !== 'success'}
              className="text-sm font-semibold text-purple-700 disabled:opacity-50"
            >
              모두 읽음 처리
            </button>
          ) : undefined
        }
      />
      <PageWrapper>
        <main className="mx-auto max-w-3xl px-4 py-6">
          {mutationError && <p role="alert" className="mb-4 text-sm text-red-600">{mutationError}</p>}
          {notice && <p role="status" className="mb-4 rounded-xl bg-amber-50 p-4 text-sm text-amber-900">{notice}</p>}
          {state === 'success' && <p className="mb-4 text-sm text-gray-600">읽지 않은 알림 {unreadCount}개</p>}
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
                  className={`overflow-hidden rounded-2xl border ${notification.isRead ? 'border-gray-200 bg-white' : 'border-purple-200 bg-purple-50'}`}
                >
                  <button
                    type="button"
                    onClick={() => void openNotification(notification.id)}
                    disabled={busy}
                    aria-busy={readingId === notification.id}
                    className="w-full p-4 text-left transition-colors hover:bg-purple-100 focus-visible:outline-2 focus-visible:outline-purple-600 disabled:opacity-60"
                  >
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0 break-words">
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
                  {notification.targetType === 'POST' && notification.targetAvailable === false && (
                    <p className="mt-2 text-xs text-amber-800">삭제되었거나 사용할 수 없는 게시글입니다.</p>
                  )}
                  </button>
                </li>
              ))}
            </ul>
          )}
          {state === 'success' && totalPages > 1 && (
            <nav aria-label="알림 페이지" className="mt-6 flex items-center justify-center gap-4">
              <button type="button" disabled={page === 0 || busy} onClick={() => void goToPage(page - 1)}
                className="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm disabled:opacity-40">이전</button>
              <span className="text-sm text-gray-600">{page + 1} / {totalPages}</span>
              <button type="button" disabled={last || busy} onClick={() => void goToPage(page + 1)}
                className="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm disabled:opacity-40">다음</button>
            </nav>
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
