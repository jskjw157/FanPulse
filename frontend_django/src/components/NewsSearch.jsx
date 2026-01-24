import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Chip,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Link,
  Pagination,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import SaveIcon from '@mui/icons-material/Save';
import { searchNews } from '../api/client';

function NewsSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [sort, setSort] = useState('date');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [saveToJson, setSaveToJson] = useState(true);
  const [savedFile, setSavedFile] = useState(null);
  const [displayCount, setDisplayCount] = useState(20);

  const ITEMS_PER_PAGE = displayCount;

  const handleSearch = async (newPage = 1) => {
    if (!query.trim()) {
      setError('검색어를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError(null);
    setSavedFile(null);

    try {
      const start = (newPage - 1) * ITEMS_PER_PAGE + 1;
      const data = await searchNews(query, {
        display: ITEMS_PER_PAGE,
        start: start,
        sort: sort,
        save: saveToJson,
        fetchContent: true,
      });

      if (data.success) {
        setResults(data);
        setPage(newPage);
        setTotalPages(Math.min(Math.ceil(data.total / ITEMS_PER_PAGE), 50));

        // 저장 결과 표시
        if (data.saved && data.saved_file) {
          setSavedFile(data.saved_file);
        }
      } else {
        setError(data.error || '검색에 실패했습니다.');
        setResults(null);
      }
    } catch (err) {
      setError(err.message || '서버 연결에 실패했습니다.');
      setResults(null);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch(1);
    }
  };

  const handlePageChange = (event, value) => {
    handleSearch(value);
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom fontWeight="bold">
        뉴스 검색
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        네이버 뉴스 API를 통해 최신 뉴스를 검색합니다.
      </Typography>

      {/* 검색 영역 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="flex-start">
            <TextField
              fullWidth
              label="검색어"
              placeholder="예: K-POP, 아이돌, 컴백"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={loading}
            />
            <TextField
              type="number"
              label="개수"
              value={displayCount}
              onChange={(e) => setDisplayCount(Math.min(100, Math.max(1, parseInt(e.target.value) || 20)))}
              disabled={loading}
              inputProps={{ min: 1, max: 100 }}
              sx={{ width: 100 }}
            />
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel>정렬</InputLabel>
              <Select
                value={sort}
                label="정렬"
                onChange={(e) => setSort(e.target.value)}
                disabled={loading}
              >
                <MenuItem value="date">최신순</MenuItem>
                <MenuItem value="sim">관련도순</MenuItem>
              </Select>
            </FormControl>
            <Button
              variant="contained"
              onClick={() => handleSearch(1)}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} /> : <SearchIcon />}
              sx={{ minWidth: 100, height: 56 }}
            >
              검색
            </Button>
          </Stack>
          <FormControlLabel
            control={
              <Checkbox
                checked={saveToJson}
                onChange={(e) => setSaveToJson(e.target.checked)}
                disabled={loading}
              />
            }
            label={
              <Stack direction="row" alignItems="center" spacing={0.5}>
                <SaveIcon fontSize="small" />
                <span>JSON 파일로 저장 (원본 기사 내용 포함)</span>
              </Stack>
            }
            sx={{ mt: 1 }}
          />
        </CardContent>
      </Card>

      {/* 저장 완료 메시지 */}
      {savedFile && (
        <Alert severity="success" sx={{ mb: 3 }}>
          <strong>{results?.saved_count || 0}개</strong> 뉴스가 저장되었습니다: <code>{savedFile}</code>
        </Alert>
      )}

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* 검색 결과 */}
      {results && (
        <Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            총 <strong>{results.total.toLocaleString()}</strong>건의 뉴스가 검색되었습니다.
          </Typography>

          <Stack spacing={2}>
            {results.items.map((item, index) => (
              <Card key={index} variant="outlined">
                <CardContent>
                  <Link
                    href={item.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    underline="hover"
                    color="primary"
                    sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
                  >
                    <Typography variant="subtitle1" fontWeight="medium">
                      {item.title}
                    </Typography>
                    <OpenInNewIcon fontSize="small" />
                  </Link>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    {item.description}
                  </Typography>
                  <Stack direction="row" spacing={1} sx={{ mt: 1.5 }}>
                    <Chip
                      label={item.pubDateFormatted || item.pubDate}
                      size="small"
                      variant="outlined"
                    />
                    {item.originallink !== item.link && (
                      <Chip
                        label="원본"
                        size="small"
                        component="a"
                        href={item.originallink}
                        target="_blank"
                        clickable
                        variant="outlined"
                        color="secondary"
                      />
                    )}
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Stack>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
              <Pagination
                count={totalPages}
                page={page}
                onChange={handlePageChange}
                color="primary"
                disabled={loading}
              />
            </Box>
          )}
        </Box>
      )}

      {/* 초기 상태 안내 */}
      {!results && !error && !loading && (
        <Card variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <SearchIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography color="text.secondary">
            검색어를 입력하고 검색 버튼을 클릭하세요.
          </Typography>
        </Card>
      )}
    </Box>
  );
}

export default NewsSearch;
