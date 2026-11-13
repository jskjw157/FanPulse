import { expect, test, type Page } from '@playwright/test'
import { mkdir } from 'node:fs/promises'
import { join } from 'node:path'

const postId = '11111111-1111-1111-1111-111111111111'
const ownerId = '22222222-2222-2222-2222-222222222222'
const commentId = '44444444-4444-4444-4444-444444444444'
const otherId = '66666666-6666-6666-6666-666666666666'
const originalPost = '오늘 공연 정말 즐거웠어요! 다음 무대도 함께 응원해요 💜'
const originalComment = '저도 현장에 있었어요. 정말 멋진 공연이었어요!'
const replyText = '다음 공연에서도 만나요!'

async function mockCommunity(page: Page, owner = true) {
  const state = {
    commentStatus: 'APPROVED' as 'APPROVED' | 'PENDING' | 'BLOCKED',
    failPostEdit: false,
    failDelete: false,
    postDeleted: false,
    postContent: originalPost,
    mutations: [] as { method: string; path: string; body: unknown }[],
  }
  const post = () => ({
    id: postId, authorId: ownerId, authorName: '무대위의별', artistId: null,
    artistName: null, artistProfileImageUrl: null, content: state.postContent,
    imageUrl: null, likeCount: 12, commentCount: comments.length, createdAt: '2026-11-06T09:00:00Z',
  })
  let comments = [{
    id: commentId, postId, userId: ownerId, authorName: '무대위의별',
    content: originalComment, status: 'APPROVED', parentCommentId: null as string | null,
    createdAt: '2026-11-06T09:10:00Z',
  }, {
    id: '55555555-5555-5555-5555-555555555555', postId, userId: otherId,
    authorName: '함께응원해', content: replyText, status: 'APPROVED',
    parentCommentId: commentId, createdAt: '2026-11-06T09:20:00Z',
  }]
  await page.route('**/api/v1/**', async (route) => {
    const path = new URL(route.request().url()).pathname.replace('/api/v1', '')
    const method = route.request().method()
    const json = (data: unknown, status = 200) => route.fulfill({ status, json: { success: true, data } })
    if (method === 'OPTIONS') return route.fulfill({ status: 204 })
    if (path === '/auth/me') return route.fulfill({ json: {
      authenticated: true, user: { id: owner ? ownerId : otherId, email: 'fan@example.test', username: 'fan' },
    } })
    if (method === 'PUT' || method === 'DELETE') {
      state.mutations.push({ method, path, body: method === 'PUT' ? route.request().postDataJSON() : null })
    }
    if (path === `/community/posts/${postId}`) {
      if (method === 'PUT') {
        if (state.failPostEdit) return route.fulfill({ status: 503, json: { success: false } })
        state.postContent = route.request().postDataJSON().content
        return json(post())
      }
      if (method === 'DELETE') {
        if (state.failDelete) return route.fulfill({ status: 403, json: { success: false } })
        state.postDeleted = true
        return route.fulfill({ status: 204 })
      }
      return state.postDeleted ? route.fulfill({ status: 404 }) : json(post())
    }
    if (path === `/community/posts/${postId}/comments/${commentId}`) {
      if (method === 'DELETE') {
        if (state.failDelete) return route.fulfill({ status: 403, json: { success: false } })
        comments = comments.filter((comment) => comment.id !== commentId)
        return route.fulfill({ status: 204 })
      }
      const edited = { ...comments[0], content: route.request().postDataJSON().content, status: state.commentStatus }
      comments = state.commentStatus === 'APPROVED' ? [edited, comments[1]] : comments.slice(1)
      return json(edited, state.commentStatus === 'PENDING' ? 202 : 200)
    }
    if (path === `/community/posts/${postId}/comments`) {
      return json({ content: comments, page: 0, size: 20, totalElements: comments.length, totalPages: comments.length ? 1 : 0 })
    }
    if (path === `/community/me/posts/${postId}/state`) return json({ liked: false, saved: true })
    if (path === '/community/posts' || path === '/community/me/saved') {
      return json({ content: state.postDeleted ? [] : [post()], page: 0, size: 20,
        totalElements: state.postDeleted ? 0 : 1, totalPages: state.postDeleted ? 0 : 1, last: true })
    }
    return route.fulfill({ status: 404, json: { success: false } })
  })
  return state
}

async function chooseAction(page: Page, label: string, action: string) {
  const summary = page.getByLabel(label, { exact: true })
  await summary.click()
  await summary.locator('..').getByRole('button', { name: action, exact: true }).click()
}

async function capture(page: Page, name: string) {
  const directory = process.env.COMMUNITY_SCREENSHOT_DIR
  if (!directory) return
  await mkdir(directory, { recursive: true })
  await page.screenshot({ path: join(directory, `${name}.png`), fullPage: true, animations: 'disabled' })
}

for (const layout of [
  { name: 'desktop', viewport: { width: 1440, height: 1000 } },
  { name: 'mobile', viewport: { width: 390, height: 844 } },
]) {
  test.describe(`Community ownership ${layout.name}`, () => {
    test.use({ viewport: layout.viewport })

    test('other users have no post actions and only their own comment actions', async ({ page }) => {
      await mockCommunity(page, false)
      await page.goto(`/post-detail?id=${postId}`)
      await expect(page.getByText(originalPost, { exact: true })).toBeVisible()
      await expect(page.getByLabel('게시글 작업', { exact: true })).toHaveCount(0)
      await expect(page.getByLabel(`댓글 작업: ${originalComment}`, { exact: true })).toHaveCount(0)
      await expect(page.getByLabel(`댓글 작업: ${replyText}`, { exact: true })).toBeVisible()
      await capture(page, `${layout.name}-default`)
    })

    test('owner post edit cancel/error/success, saved refresh and explicit deletion', async ({ page }) => {
      const state = await mockCommunity(page)
      await page.goto('/community')
      await page.getByRole('link', { name: `${originalPost} 상세 보기` }).click()
      await expect(page.getByLabel('게시글 작업', { exact: true })).toBeVisible()
      await page.getByLabel('게시글 작업', { exact: true }).click()
      await capture(page, `${layout.name}-owner-menu`)
      await page.getByRole('button', { name: '수정', exact: true }).click()
      await expect(page.getByLabel('게시글 내용 수정')).toHaveValue(originalPost)
      await page.getByLabel('게시글 내용 수정').fill('수정한 공연 후기 💜')
      await capture(page, `${layout.name}-post-edit`)
      await page.getByRole('button', { name: '취소', exact: true }).click()
      expect(state.mutations).toHaveLength(0)
      await expect(page.getByText(originalPost, { exact: true })).toBeVisible()
      await chooseAction(page, '게시글 작업', '수정')
      await page.getByLabel('게시글 내용 수정').fill('수정한 공연 후기 💜')
      state.failPostEdit = true
      await page.getByRole('button', { name: '수정 저장', exact: true }).click()
      await expect(page.getByRole('alert').filter({ hasText: '수정하지 못했습니다.' })).toBeVisible()
      await expect(page.getByLabel('게시글 내용 수정')).toHaveValue('수정한 공연 후기 💜')
      await capture(page, `${layout.name}-post-edit-error`)
      state.failPostEdit = false
      await page.getByRole('button', { name: '수정 저장', exact: true }).click()
      await expect(page.getByText('수정한 공연 후기 💜', { exact: true })).toBeVisible()
      expect(state.mutations[1].body).toEqual({ content: '수정한 공연 후기 💜' })
      await page.locator('a[href="/community"]:visible').first().click()
      await expect(page.getByText('수정한 공연 후기 💜', { exact: true })).toBeVisible()
      await page.goto('/saved')
      await expect(page.getByText('수정한 공연 후기 💜', { exact: true })).toBeVisible()
      await page.getByRole('link', { name: '수정한 공연 후기 💜 상세 보기' }).click()
      await chooseAction(page, '게시글 작업', '삭제')
      await expect(page.getByRole('dialog')).toBeVisible()
      await expect(page.getByRole('button', { name: '취소', exact: true })).toBeFocused()
      await capture(page, `${layout.name}-post-delete-confirm`)
      expect(state.postDeleted).toBe(false)
      await page.keyboard.press('Escape')
      await expect(page.getByRole('dialog')).toHaveCount(0)
      await chooseAction(page, '게시글 작업', '삭제')
      state.failDelete = true
      await page.getByRole('button', { name: '삭제 확인' }).click()
      await expect(page.getByRole('dialog').getByRole('alert')).toContainText('삭제하지 못했습니다.')
      await expect(page).toHaveURL(/post-detail/)
      await capture(page, `${layout.name}-post-delete-error`)
      state.failDelete = false
      await page.getByRole('button', { name: '삭제 확인' }).click()
      await expect(page).toHaveURL(/\/community$/)
      await expect(page.getByText('아직 작성된 게시글이 없습니다.')).toBeVisible()
      await page.goto('/saved')
      await expect(page.getByText('저장한 게시글이 없습니다.')).toBeVisible()
    })

    test('approved comment edit and parent deletion reload public replies/counts', async ({ page }) => {
      const state = await mockCommunity(page)
      await page.goto(`/post-detail?id=${postId}`)
      await expect(page.getByText(originalComment, { exact: true })).toBeVisible()
      await chooseAction(page, `댓글 작업: ${originalComment}`, '수정')
      await page.getByLabel('댓글 내용 수정').fill('수정된 응원 댓글')
      await page.getByText(replyText, { exact: true }).scrollIntoViewIfNeeded()
      await expect(page.getByText(replyText, { exact: true })).toBeInViewport()
      await capture(page, `${layout.name}-comment-edit`)
      await page.getByRole('button', { name: '취소', exact: true }).click()
      expect(state.mutations).toHaveLength(0)
      await chooseAction(page, `댓글 작업: ${originalComment}`, '수정')
      await page.getByLabel('댓글 내용 수정').fill('수정된 응원 댓글')
      await page.getByRole('button', { name: '수정 저장', exact: true }).click()
      await expect(page.getByText('수정된 응원 댓글', { exact: true })).toBeVisible()
      await chooseAction(page, '댓글 작업: 수정된 응원 댓글', '삭제')
      await expect(page.getByRole('dialog')).toContainText('작성된 답글은 유지됩니다.')
      await capture(page, `${layout.name}-comment-delete-confirm`)
      await page.getByRole('button', { name: '취소', exact: true }).click()
      expect(state.mutations).toHaveLength(1)
      await chooseAction(page, '댓글 작업: 수정된 응원 댓글', '삭제')
      await page.getByRole('button', { name: '삭제 확인' }).click()
      await expect(page.getByText('수정된 응원 댓글', { exact: true })).toHaveCount(0)
      await expect(page.getByText(replyText, { exact: true })).toBeVisible()
      await expect(page.getByRole('heading', { name: '댓글 1', exact: true })).toBeVisible()
      await capture(page, `${layout.name}-deleted-parent-reply`)
      expect(state.mutations[0].body).toEqual({ content: '수정된 응원 댓글' })
    })

    for (const status of ['PENDING', 'BLOCKED'] as const) {
      test(`${status} comment edits disappear instead of reinserting non-public content`, async ({ page }) => {
        const state = await mockCommunity(page)
        state.commentStatus = status
        await page.goto(`/post-detail?id=${postId}`)
        await expect(page.getByText(originalComment, { exact: true })).toBeVisible()
        await chooseAction(page, `댓글 작업: ${originalComment}`, '수정')
        await page.getByLabel('댓글 내용 수정').fill('검토할 수정 내용')
        await page.getByRole('button', { name: '수정 저장', exact: true }).click()
        await expect(page.getByRole('status')).toContainText('공개 목록에서 제외되었습니다.')
        await expect(page.getByText(originalComment, { exact: true })).toHaveCount(0)
        await expect(page.getByText('검토할 수정 내용', { exact: true })).toHaveCount(0)
        await expect(page.getByText(replyText, { exact: true })).toBeVisible()
        await expect(page.getByRole('heading', { name: '댓글 1', exact: true })).toBeVisible()
        await capture(page, `${layout.name}-comment-${status.toLowerCase()}`)
      })
    }
  })
}
