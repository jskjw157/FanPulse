import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import URLSummarize from './components/URLSummarize'
import TextSummarize from './components/TextSummarize'
import NewsSearch from './components/NewsSearch'
import SavedNewsList from './components/SavedNewsList'
import SummarizedNews from './components/SummarizedNews'
import History from './components/History'
import Settings from './components/Settings'
// 댓글 필터링 컴포넌트
import CommentFilterTest from './components/CommentFilterTest'
import FilterRuleManagement from './components/FilterRuleManagement'
import FilteredLogs from './components/FilteredLogs'
// AI 모더레이션 컴포넌트
import AIModerationTest from './components/AIModerationTest'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<URLSummarize />} />
        <Route path="/url-summarize" element={<URLSummarize />} />
        <Route path="/text-summarize" element={<TextSummarize />} />
        <Route path="/news-search" element={<NewsSearch />} />
        <Route path="/saved-news" element={<SavedNewsList />} />
        <Route path="/summarized-news" element={<SummarizedNews />} />
        <Route path="/history" element={<History />} />
        <Route path="/settings" element={<Settings />} />
        {/* 댓글 필터링 라우트 */}
        <Route path="/comment-filter-test" element={<CommentFilterTest />} />
        <Route path="/filter-rules" element={<FilterRuleManagement />} />
        <Route path="/filter-logs" element={<FilteredLogs />} />
        {/* AI 모더레이션 라우트 */}
        <Route path="/ai-moderation" element={<AIModerationTest />} />
      </Routes>
    </Layout>
  )
}

export default App
