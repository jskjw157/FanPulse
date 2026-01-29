import axios from 'axios';

const API_BASE_URL = 'http://localhost:8000/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 3600000, // 1 hour (배치 요약 등 오래 걸리는 작업용)
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => {
    console.log('API Response:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('API Error:', error.response?.status, error.message);
    return Promise.reject(error);
  }
);

// API methods
export const healthCheck = async () => {
  const response = await apiClient.get('/health');
  return response.data;
};

export const summarize = async (data) => {
  const response = await apiClient.post('/summarize', data);
  return response.data;
};

export const searchNews = async (query, options = {}) => {
  const params = new URLSearchParams({
    query,
    display: options.display || 20,
    start: options.start || 1,
    sort: options.sort || 'date',
  });

  // JSON 저장 옵션
  if (options.save) {
    params.append('save', 'true');
    params.append('fetch_content', options.fetchContent !== false ? 'true' : 'false');
    params.append('async_save', options.asyncSave !== false ? 'true' : 'false');
  }

  const response = await apiClient.get(`/news/search?${params}`);
  return response.data;
};

// 저장된 뉴스 파일 목록 조회
export const getSavedNewsList = async () => {
  const response = await apiClient.get('/news/saved');
  return response.data;
};

// 저장된 뉴스 파일 상세 조회
export const getSavedNewsDetail = async (filename) => {
  const response = await apiClient.get(`/news/saved/${filename}`);
  return response.data;
};

// 저장된 뉴스 파일 삭제
export const deleteSavedNews = async (filename) => {
  const response = await apiClient.delete(`/news/saved/${filename}`);
  return response.data;
};

// 배치 요약 실행
export const batchSummarize = async (items, method = 'rule', options = {}) => {
  const response = await apiClient.post('/news/batch-summarize', {
    items,
    method,
    max_length: options.maxLength || 300,
    min_length: options.minLength || 50,
  });
  return response.data;
};

// 요약된 뉴스 파일 목록 조회
export const getSummarizedNewsList = async () => {
  const response = await apiClient.get('/news/summarized');
  return response.data;
};

// 요약된 뉴스 파일 상세 조회
export const getSummarizedNewsDetail = async (filename) => {
  const response = await apiClient.get(`/news/summarized/${filename}`);
  return response.data;
};

// 요약된 뉴스 파일 삭제
export const deleteSummarizedNews = async (filename) => {
  const response = await apiClient.delete(`/news/summarized/${filename}`);
  return response.data;
};

//######################
// MongoDB 조회 (주석 처리)
//######################
// export const getSavedNewsFromMongo = async (query = '', limit = 100, skip = 0) => {
//   const params = new URLSearchParams({ query, limit, skip });
//   const response = await apiClient.get(`/news/saved?${params}`);
//   return response.data;
// };
//
// export const getNewsDetailFromMongo = async (docId) => {
//   const response = await apiClient.get(`/news/saved/${docId}`);
//   return response.data;
// };
//
// export const deleteNewsFromMongo = async (docId) => {
//   const response = await apiClient.delete(`/news/saved/${docId}`);
//   return response.data;
// };
//######################

//######################
// 댓글 필터링 API
//######################

// 단일 댓글 필터링 테스트
export const testCommentFilter = async (content) => {
  const response = await apiClient.post('/comments/filter/test', { content });
  return response.data;
};

// 일괄 댓글 필터링 테스트
export const batchTestCommentFilter = async (comments) => {
  const response = await apiClient.post('/comments/filter/batch', { comments });
  return response.data;
};

// 필터링 규칙 목록 조회
export const getFilterRules = async (options = {}) => {
  const params = new URLSearchParams();
  if (options.isActive !== undefined) {
    params.append('is_active', options.isActive);
  }
  if (options.filterType) {
    params.append('filter_type', options.filterType);
  }
  const query = params.toString() ? `?${params}` : '';
  const response = await apiClient.get(`/comments/filter/rules${query}`);
  return response.data;
};

// 필터링 규칙 생성
export const createFilterRule = async (data) => {
  const response = await apiClient.post('/comments/filter/rules', data);
  return response.data;
};

// 필터링 규칙 상세 조회
export const getFilterRuleDetail = async (ruleId) => {
  const response = await apiClient.get(`/comments/filter/rules/${ruleId}`);
  return response.data;
};

// 필터링 규칙 수정
export const updateFilterRule = async (ruleId, data) => {
  const response = await apiClient.put(`/comments/filter/rules/${ruleId}`, data);
  return response.data;
};

// 필터링 규칙 삭제
export const deleteFilterRule = async (ruleId) => {
  const response = await apiClient.delete(`/comments/filter/rules/${ruleId}`);
  return response.data;
};

// 필터링 로그 조회
export const getFilteredLogs = async (options = {}) => {
  const params = new URLSearchParams({
    limit: options.limit || 50,
    offset: options.offset || 0,
  });
  if (options.action) {
    params.append('action', options.action);
  }
  const response = await apiClient.get(`/comments/filter/logs?${params}`);
  return response.data;
};

//######################
// AI 모더레이션 API
//######################

// AI 콘텐츠 모더레이션 단일 검사
export const checkContentModeration = async (text, options = {}) => {
  const response = await apiClient.post('/moderation/check', {
    text,
    use_cache: options.useCache !== false,
    thresholds: options.thresholds,
  });
  return response.data;
};

// AI 콘텐츠 모더레이션 일괄 검사
export const batchCheckContentModeration = async (texts, options = {}) => {
  const response = await apiClient.post('/moderation/batch', {
    texts,
    use_cache: options.useCache !== false,
  });
  return response.data;
};

// AI 모더레이션 상태 확인
export const getModerationStatus = async () => {
  const response = await apiClient.get('/moderation/status');
  return response.data;
};

export default apiClient;
