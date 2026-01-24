import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  LinearProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Paper,
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import ContentPasteIcon from '@mui/icons-material/ContentPaste';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';
import PsychologyIcon from '@mui/icons-material/Psychology';
import { summarize } from '../api/client';
import { getSettings, saveToHistory } from '../utils/storage';
import ResultCard from './ResultCard';

function URLSummarize() {
  const [url, setUrl] = useState('');
  const [language, setLanguage] = useState('ko');
  const [maxLength, setMaxLength] = useState(200);
  const [minLength, setMinLength] = useState(50);
  const [summarizeMethod, setSummarizeMethod] = useState('rule');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);

  useEffect(() => {
    const settings = getSettings();
    setLanguage(settings.defaultLanguage);
    setMaxLength(settings.defaultMaxLength);
    setMinLength(settings.defaultMinLength);
  }, []);

  const handleSampleFill = () => {
    setUrl('https://news.ycombinator.com/');
    setError(null);
  };

  const validateForm = () => {
    if (!url.trim()) {
      setError('URL을 입력해주세요.');
      return false;
    }

    try {
      new URL(url);
    } catch {
      setError('유효한 URL 형식이 아닙니다.');
      return false;
    }

    if (minLength > maxLength) {
      setError('최소 길이는 최대 길이보다 작아야 합니다.');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setResult(null);

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const data = await summarize({
        input_type: 'url',
        url: url.trim(),
        language,
        max_length: maxLength,
        min_length: minLength,
        summarize_method: summarizeMethod,
      });

      setResult(data);
      saveToHistory(data);
    } catch (err) {
      console.error('Summarization error:', err);

      if (err.response?.data) {
        const errorData = err.response.data;
        if (errorData.details) {
          const messages = Object.entries(errorData.details)
            .map(([field, errors]) => `${field}: ${errors}`)
            .join('\n');
          setError(messages);
        } else {
          setError(errorData.error || '요약 생성에 실패했습니다.');
        }
      } else if (err.request) {
        setError('서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해주세요.');
      } else {
        setError('요청 처리 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        URL 요약
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        뉴스 기사나 블로그 URL을 입력하면 자동으로 내용을 추출하여 요약합니다.
      </Typography>

      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="URL"
                placeholder="https://example.com/article"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                disabled={loading}
                variant="outlined"
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle2" gutterBottom>
                요약 방식
              </Typography>
              <ToggleButtonGroup
                value={summarizeMethod}
                exclusive
                onChange={(e, newMethod) => {
                  if (newMethod !== null) {
                    setSummarizeMethod(newMethod);
                  }
                }}
                disabled={loading}
                fullWidth
              >
                <ToggleButton value="rule">
                  <Tooltip title="단어 빈도 기반 추출형 요약 (빠름)">
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <AutoFixHighIcon />
                      <span>알고리즘</span>
                    </Box>
                  </Tooltip>
                </ToggleButton>
                <ToggleButton value="ai">
                  <Tooltip title="AI 모델 기반 요약 (더 자연스러움, 처음 실행 시 모델 다운로드 필요)">
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PsychologyIcon />
                      <span>AI 모델</span>
                    </Box>
                  </Tooltip>
                </ToggleButton>
              </ToggleButtonGroup>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                {summarizeMethod === 'rule'
                  ? '단어 빈도 기반으로 중요 문장을 추출합니다.'
                  : 'AI 모델이 텍스트를 이해하고 요약합니다. (처음 사용 시 모델 다운로드에 시간이 걸릴 수 있습니다)'}
              </Typography>
            </Grid>

            <Grid item xs={12} sm={4}>
              <FormControl fullWidth>
                <InputLabel>언어</InputLabel>
                <Select
                  value={language}
                  label="언어"
                  onChange={(e) => setLanguage(e.target.value)}
                  disabled={loading}
                >
                  <MenuItem value="ko">한국어</MenuItem>
                  <MenuItem value="en">English</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                type="number"
                label="최소 길이"
                value={minLength}
                onChange={(e) => setMinLength(parseInt(e.target.value) || 50)}
                disabled={loading}
                inputProps={{ min: 10, max: 500 }}
              />
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                type="number"
                label="최대 길이"
                value={maxLength}
                onChange={(e) => setMaxLength(parseInt(e.target.value) || 200)}
                disabled={loading}
                inputProps={{ min: 50, max: 1000 }}
              />
            </Grid>

            <Grid item xs={12} sx={{ display: 'flex', gap: 2 }}>
              <Button
                type="submit"
                variant="contained"
                startIcon={<SendIcon />}
                disabled={loading}
                size="large"
              >
                요약하기
              </Button>
              <Button
                variant="outlined"
                startIcon={<ContentPasteIcon />}
                onClick={handleSampleFill}
                disabled={loading}
              >
                샘플 채우기
              </Button>
            </Grid>
          </Grid>
        </form>
      </Paper>

      {loading && (
        <Box sx={{ mb: 3 }}>
          <LinearProgress />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {summarizeMethod === 'ai'
              ? '기사를 추출하고 AI 모델로 요약하는 중입니다... (처음 실행 시 모델 다운로드로 시간이 걸릴 수 있습니다)'
              : '기사를 추출하고 요약하는 중입니다...'}
          </Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {result && <ResultCard result={result} />}
    </Box>
  );
}

export default URLSummarize;
