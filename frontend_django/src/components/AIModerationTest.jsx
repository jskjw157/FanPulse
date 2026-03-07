import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Alert,
  LinearProgress,
  Chip,
  Stack,
  Divider,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Slider,
  FormControlLabel,
  Switch,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Tooltip,
} from '@mui/material';
import {
  Psychology as AIIcon,
  Send as SendIcon,
  CheckCircle as SafeIcon,
  Warning as WarningIcon,
  Block as BlockIcon,
  RateReview as ReviewIcon,
  Speed as SpeedIcon,
  ExpandMore as ExpandMoreIcon,
  Cached as CacheIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';
import {
  checkContentModeration,
  batchCheckContentModeration,
  getModerationStatus,
} from '../api/client';

// 조치별 색상 및 아이콘
const ACTION_CONFIG = {
  allow: { color: 'success', icon: <SafeIcon />, label: '허용' },
  warning: { color: 'warning', icon: <WarningIcon />, label: '경고' },
  review: { color: 'info', icon: <ReviewIcon />, label: '검토 대기' },
  block: { color: 'error', icon: <BlockIcon />, label: '차단' },
};

// 카테고리 한글명
const CATEGORY_LABELS = {
  profanity: '욕설/비속어',
  spam: '스팸',
  adult: '성인 콘텐츠',
  violence: '폭력',
  hate: '혐오 발언',
  harassment: '괴롭힘',
  clean: '정상',
  unknown: '알 수 없음',
};

function AIModerationTest() {
  // 상태 관리
  const [text, setText] = useState('');
  const [batchTexts, setBatchTexts] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);
  const [batchResults, setBatchResults] = useState(null);
  const [statusInfo, setStatusInfo] = useState(null);

  // 설정
  const [useCache, setUseCache] = useState(true);
  const [thresholds, setThresholds] = useState({
    profanity: 0.7,
    spam: 0.8,
    hate: 0.7,
  });
  const [showSettings, setShowSettings] = useState(false);

  // 상태 확인
  useEffect(() => {
    loadStatus();
  }, []);

  const loadStatus = async () => {
    try {
      const data = await getModerationStatus();
      setStatusInfo(data);
    } catch (err) {
      console.error('Failed to load moderation status:', err);
    }
  };

  // 단일 검사
  const handleCheck = async () => {
    if (!text.trim()) {
      setError('검사할 텍스트를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    try {
      const data = await checkContentModeration(text, {
        useCache,
        thresholds: showSettings ? thresholds : undefined,
      });
      setResult(data);
    } catch (err) {
      if (err.request) {
        setError('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
      } else {
        setError('검사 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 일괄 검사
  const handleBatchCheck = async () => {
    const texts = batchTexts
      .split('\n')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    if (texts.length === 0) {
      setError('검사할 텍스트를 입력해주세요. (줄바꿈으로 구분)');
      return;
    }

    if (texts.length > 50) {
      setError('최대 50개까지 일괄 검사할 수 있습니다.');
      return;
    }

    setLoading(true);
    setError('');
    setBatchResults(null);

    try {
      const data = await batchCheckContentModeration(texts, { useCache });
      setBatchResults({ ...data, texts });
    } catch (err) {
      if (err.request) {
        setError('서버에 연결할 수 없습니다.');
      } else {
        setError('일괄 검사 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 임계값 변경
  const handleThresholdChange = (category, value) => {
    setThresholds((prev) => ({ ...prev, [category]: value }));
  };

  // 조치 정보 가져오기
  const getActionConfig = (action) => {
    return ACTION_CONFIG[action] || ACTION_CONFIG.allow;
  };

  return (
    <Box>
      <Typography
        variant="h5"
        gutterBottom
        sx={{ fontWeight: 600, mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}
      >
        <AIIcon color="primary" />
        AI 콘텐츠 모더레이션
      </Typography>

      {/* 상태 표시 */}
      {statusInfo && (
        <Paper sx={{ p: 2, mb: 3, backgroundColor: statusInfo.available ? 'success.50' : 'warning.50' }}>
          <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
            <Chip
              icon={statusInfo.available ? <SafeIcon /> : <WarningIcon />}
              label={statusInfo.available ? 'AI 모더레이션 사용 가능' : 'AI 모델 로드 필요'}
              color={statusInfo.available ? 'success' : 'warning'}
              size="small"
            />
            {statusInfo.gpu_available && (
              <Chip label={`GPU: ${statusInfo.gpu_name || 'Available'}`} size="small" variant="outlined" />
            )}
            {statusInfo.models_loaded?.length > 0 && (
              <Chip label={`로드된 모델: ${statusInfo.models_loaded.join(', ')}`} size="small" variant="outlined" />
            )}
          </Stack>
        </Paper>
      )}

      {/* 설정 패널 */}
      <Accordion expanded={showSettings} onChange={() => setShowSettings(!showSettings)} sx={{ mb: 3 }}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Stack direction="row" spacing={1} alignItems="center">
            <SettingsIcon fontSize="small" />
            <Typography>고급 설정</Typography>
          </Stack>
        </AccordionSummary>
        <AccordionDetails>
          <Stack spacing={3}>
            <FormControlLabel
              control={<Switch checked={useCache} onChange={(e) => setUseCache(e.target.checked)} />}
              label={
                <Stack direction="row" spacing={1} alignItems="center">
                  <CacheIcon fontSize="small" />
                  <span>캐시 사용 (동일 텍스트 빠른 응답)</span>
                </Stack>
              }
            />

            <Divider />

            <Typography variant="subtitle2">카테고리별 임계값 설정</Typography>
            <Typography variant="caption" color="text.secondary">
              임계값이 낮을수록 더 엄격하게 필터링됩니다. (0.0 ~ 1.0)
            </Typography>

            {Object.entries(thresholds).map(([category, value]) => (
              <Box key={category}>
                <Typography variant="body2" gutterBottom>
                  {CATEGORY_LABELS[category] || category}: {value.toFixed(2)}
                </Typography>
                <Slider
                  value={value}
                  onChange={(_, newValue) => handleThresholdChange(category, newValue)}
                  min={0}
                  max={1}
                  step={0.05}
                  marks={[
                    { value: 0.5, label: '엄격' },
                    { value: 0.7, label: '보통' },
                    { value: 0.9, label: '관대' },
                  ]}
                  valueLabelDisplay="auto"
                />
              </Box>
            ))}
          </Stack>
        </AccordionDetails>
      </Accordion>

      {/* 알림 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* 단일 검사 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          단일 텍스트 검사
        </Typography>

        <TextField
          fullWidth
          multiline
          rows={4}
          label="검사할 텍스트"
          placeholder="검사할 댓글이나 게시글 내용을 입력하세요..."
          value={text}
          onChange={(e) => setText(e.target.value)}
          sx={{ mb: 2 }}
        />

        <Button
          variant="contained"
          startIcon={<SendIcon />}
          onClick={handleCheck}
          disabled={loading || !text.trim()}
        >
          AI 모더레이션 검사
        </Button>

        {/* 단일 검사 결과 */}
        {result && (
          <Card sx={{ mt: 3 }} variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                {/* 메인 결과 */}
                <Stack direction="row" spacing={2} alignItems="center">
                  <Chip
                    icon={getActionConfig(result.action).icon}
                    label={getActionConfig(result.action).label}
                    color={getActionConfig(result.action).color}
                  />
                  <Typography variant="body2" color="text.secondary">
                    {result.is_flagged ? '부적절한 콘텐츠가 감지되었습니다.' : '정상적인 콘텐츠입니다.'}
                  </Typography>
                </Stack>

                {/* 상세 정보 */}
                <Divider />

                <Stack direction="row" spacing={3} flexWrap="wrap">
                  {result.highest_category && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        감지된 카테고리
                      </Typography>
                      <Typography variant="body2" fontWeight={500}>
                        {CATEGORY_LABELS[result.highest_category] || result.highest_category}
                      </Typography>
                    </Box>
                  )}

                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      최고 점수
                    </Typography>
                    <Typography variant="body2" fontWeight={500}>
                      {(result.highest_score * 100).toFixed(1)}%
                    </Typography>
                  </Box>

                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      신뢰도
                    </Typography>
                    <Typography variant="body2" fontWeight={500}>
                      {(result.confidence * 100).toFixed(1)}%
                    </Typography>
                  </Box>

                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      처리 시간
                    </Typography>
                    <Stack direction="row" spacing={0.5} alignItems="center">
                      <SpeedIcon fontSize="small" color="action" />
                      <Typography variant="body2">{result.processing_time_ms}ms</Typography>
                    </Stack>
                  </Box>

                  {result.cached && (
                    <Tooltip title="캐시된 결과">
                      <Chip icon={<CacheIcon />} label="캐시" size="small" variant="outlined" />
                    </Tooltip>
                  )}
                </Stack>

                {/* 카테고리별 점수 */}
                {result.categories && result.categories.length > 0 && (
                  <>
                    <Divider />
                    <Typography variant="subtitle2">카테고리별 분석</Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>카테고리</TableCell>
                            <TableCell align="right">점수</TableCell>
                            <TableCell align="right">임계값</TableCell>
                            <TableCell align="center">상태</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {result.categories.map((cat, idx) => (
                            <TableRow key={idx}>
                              <TableCell>{CATEGORY_LABELS[cat.category] || cat.category}</TableCell>
                              <TableCell align="right">{(cat.score * 100).toFixed(1)}%</TableCell>
                              <TableCell align="right">{(cat.threshold * 100).toFixed(0)}%</TableCell>
                              <TableCell align="center">
                                <Chip
                                  size="small"
                                  label={cat.is_flagged ? '위험' : '정상'}
                                  color={cat.is_flagged ? 'error' : 'success'}
                                />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </>
                )}

                {/* 오류 표시 */}
                {result.error && (
                  <Alert severity="warning" sx={{ mt: 1 }}>
                    {result.error}
                  </Alert>
                )}
              </Stack>
            </CardContent>
          </Card>
        )}
      </Paper>

      {/* 일괄 검사 */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          일괄 텍스트 검사
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          여러 텍스트를 한 번에 검사합니다. 줄바꿈으로 구분하세요. (최대 50개)
        </Typography>

        <TextField
          fullWidth
          multiline
          rows={6}
          label="검사할 텍스트 목록"
          placeholder="첫 번째 텍스트&#10;두 번째 텍스트&#10;세 번째 텍스트"
          value={batchTexts}
          onChange={(e) => setBatchTexts(e.target.value)}
          sx={{ mb: 2 }}
        />

        <Button
          variant="contained"
          startIcon={<SendIcon />}
          onClick={handleBatchCheck}
          disabled={loading || !batchTexts.trim()}
        >
          일괄 검사
        </Button>

        {/* 일괄 검사 결과 */}
        {batchResults && (
          <Card sx={{ mt: 3 }} variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Typography variant="subtitle1">
                    검사 결과: 총 {batchResults.total}개
                  </Typography>
                  <Chip
                    label={`${batchResults.flagged_count}개 위험`}
                    color={batchResults.flagged_count > 0 ? 'error' : 'success'}
                    size="small"
                  />
                </Stack>

                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ backgroundColor: 'grey.100' }}>
                        <TableCell>#</TableCell>
                        <TableCell>텍스트</TableCell>
                        <TableCell align="center">조치</TableCell>
                        <TableCell align="center">카테고리</TableCell>
                        <TableCell align="right">점수</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {batchResults.results.map((item, idx) => (
                        <TableRow key={idx} hover>
                          <TableCell>{idx + 1}</TableCell>
                          <TableCell>
                            <Typography
                              variant="body2"
                              sx={{
                                maxWidth: 300,
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                                whiteSpace: 'nowrap',
                              }}
                            >
                              {batchResults.texts[idx]}
                            </Typography>
                          </TableCell>
                          <TableCell align="center">
                            <Chip
                              size="small"
                              icon={getActionConfig(item.action).icon}
                              label={getActionConfig(item.action).label}
                              color={getActionConfig(item.action).color}
                            />
                          </TableCell>
                          <TableCell align="center">
                            {item.highest_category
                              ? CATEGORY_LABELS[item.highest_category] || item.highest_category
                              : '-'}
                          </TableCell>
                          <TableCell align="right">
                            {(item.highest_score * 100).toFixed(1)}%
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Stack>
            </CardContent>
          </Card>
        )}
      </Paper>
    </Box>
  );
}

export default AIModerationTest;
