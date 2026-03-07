import { useState } from 'react';
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
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  FilterAlt as FilterIcon,
  PlaylistAddCheck as BatchIcon,
  Clear as ClearIcon,
  CheckCircle as PassIcon,
  Block as BlockIcon,
  Visibility as HideIcon,
  RateReview as ReviewIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { testCommentFilter, batchTestCommentFilter } from '../api/client';

function CommentFilterTest() {
  // 단일 테스트 상태
  const [singleContent, setSingleContent] = useState('');
  const [singleResult, setSingleResult] = useState(null);
  const [singleLoading, setSingleLoading] = useState(false);
  const [singleError, setSingleError] = useState('');

  // 일괄 테스트 상태
  const [batchComments, setBatchComments] = useState(['']);
  const [batchResult, setBatchResult] = useState(null);
  const [batchLoading, setBatchLoading] = useState(false);
  const [batchError, setBatchError] = useState('');

  // 단일 필터링 테스트
  const handleSingleTest = async () => {
    if (!singleContent.trim()) {
      setSingleError('댓글 내용을 입력해주세요.');
      return;
    }

    setSingleLoading(true);
    setSingleError('');
    setSingleResult(null);

    try {
      const result = await testCommentFilter(singleContent);
      setSingleResult(result);
    } catch (err) {
      if (err.response?.data?.error) {
        setSingleError(err.response.data.error);
      } else if (err.request) {
        setSingleError('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
      } else {
        setSingleError('필터링 테스트 중 오류가 발생했습니다.');
      }
    } finally {
      setSingleLoading(false);
    }
  };

  // 일괄 필터링 테스트
  const handleBatchTest = async () => {
    const validComments = batchComments.filter(c => c.trim());
    if (validComments.length === 0) {
      setBatchError('최소 하나의 댓글을 입력해주세요.');
      return;
    }

    setBatchLoading(true);
    setBatchError('');
    setBatchResult(null);

    try {
      const result = await batchTestCommentFilter(validComments);
      setBatchResult(result);
    } catch (err) {
      if (err.response?.data?.error) {
        setBatchError(err.response.data.error);
      } else if (err.request) {
        setBatchError('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
      } else {
        setBatchError('일괄 필터링 테스트 중 오류가 발생했습니다.');
      }
    } finally {
      setBatchLoading(false);
    }
  };

  // 일괄 테스트 댓글 추가
  const addBatchComment = () => {
    setBatchComments([...batchComments, '']);
  };

  // 일괄 테스트 댓글 삭제
  const removeBatchComment = (index) => {
    if (batchComments.length > 1) {
      setBatchComments(batchComments.filter((_, i) => i !== index));
    }
  };

  // 일괄 테스트 댓글 수정
  const updateBatchComment = (index, value) => {
    const updated = [...batchComments];
    updated[index] = value;
    setBatchComments(updated);
  };

  // 조치 아이콘 및 색상 반환
  const getActionInfo = (action) => {
    switch (action) {
      case 'block':
        return { icon: <BlockIcon />, color: 'error', label: '차단' };
      case 'hide':
        return { icon: <HideIcon />, color: 'warning', label: '숨김' };
      case 'review':
        return { icon: <ReviewIcon />, color: 'info', label: '검토 대기' };
      default:
        return { icon: <PassIcon />, color: 'success', label: '통과' };
    }
  };

  // 필터 타입 라벨
  const getFilterTypeLabel = (type) => {
    const labels = {
      keyword: '금칙어',
      regex: '정규식',
      spam: '스팸',
      url: 'URL',
      repeat: '반복 문자',
    };
    return labels[type] || type;
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
        댓글 필터링 테스트
      </Typography>

      {/* 단일 테스트 섹션 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FilterIcon color="primary" />
          단일 댓글 테스트
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          댓글 내용을 입력하면 등록된 필터링 규칙에 의해 차단되는지 테스트합니다.
        </Typography>

        <TextField
          fullWidth
          multiline
          rows={3}
          label="댓글 내용"
          placeholder="필터링 테스트할 댓글을 입력하세요..."
          value={singleContent}
          onChange={(e) => setSingleContent(e.target.value)}
          sx={{ mb: 2 }}
        />

        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <Button
            variant="contained"
            startIcon={<FilterIcon />}
            onClick={handleSingleTest}
            disabled={singleLoading}
          >
            필터링 테스트
          </Button>
          <Button
            variant="outlined"
            startIcon={<ClearIcon />}
            onClick={() => {
              setSingleContent('');
              setSingleResult(null);
              setSingleError('');
            }}
          >
            초기화
          </Button>
        </Stack>

        {singleLoading && <LinearProgress sx={{ mb: 2 }} />}
        {singleError && <Alert severity="error" sx={{ mb: 2 }}>{singleError}</Alert>}

        {singleResult && (
          <Card variant="outlined" sx={{ mt: 2 }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
                <Typography variant="subtitle1" fontWeight={600}>결과:</Typography>
                {singleResult.is_filtered ? (
                  <Chip
                    icon={getActionInfo(singleResult.action).icon}
                    label={`필터링됨 (${getActionInfo(singleResult.action).label})`}
                    color={getActionInfo(singleResult.action).color}
                    size="small"
                  />
                ) : (
                  <Chip
                    icon={<PassIcon />}
                    label="통과"
                    color="success"
                    size="small"
                  />
                )}
              </Stack>

              {singleResult.is_filtered && (
                <Box sx={{ pl: 2, borderLeft: '3px solid', borderColor: 'error.main' }}>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>규칙 이름:</strong> {singleResult.rule_name}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>필터 타입:</strong> {getFilterTypeLabel(singleResult.filter_type)}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>매칭된 패턴:</strong> <code>{singleResult.matched_pattern}</code>
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {singleResult.reason}
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        )}
      </Paper>

      <Divider sx={{ my: 3 }} />

      {/* 일괄 테스트 섹션 */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <BatchIcon color="primary" />
          일괄 댓글 테스트
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          여러 댓글을 한 번에 테스트합니다. 최대 100개까지 가능합니다.
        </Typography>

        {batchComments.map((comment, index) => (
          <Stack key={index} direction="row" spacing={1} sx={{ mb: 1 }}>
            <TextField
              fullWidth
              size="small"
              label={`댓글 ${index + 1}`}
              placeholder="댓글 내용..."
              value={comment}
              onChange={(e) => updateBatchComment(index, e.target.value)}
            />
            <Tooltip title="삭제">
              <span>
                <IconButton
                  onClick={() => removeBatchComment(index)}
                  disabled={batchComments.length === 1}
                  color="error"
                >
                  <DeleteIcon />
                </IconButton>
              </span>
            </Tooltip>
          </Stack>
        ))}

        <Stack direction="row" spacing={2} sx={{ mt: 2, mb: 2 }}>
          <Button
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={addBatchComment}
            disabled={batchComments.length >= 100}
          >
            댓글 추가
          </Button>
          <Button
            variant="contained"
            startIcon={<BatchIcon />}
            onClick={handleBatchTest}
            disabled={batchLoading}
          >
            일괄 테스트
          </Button>
          <Button
            variant="outlined"
            startIcon={<ClearIcon />}
            onClick={() => {
              setBatchComments(['']);
              setBatchResult(null);
              setBatchError('');
            }}
          >
            초기화
          </Button>
        </Stack>

        {batchLoading && <LinearProgress sx={{ mb: 2 }} />}
        {batchError && <Alert severity="error" sx={{ mb: 2 }}>{batchError}</Alert>}

        {batchResult && (
          <Box sx={{ mt: 2 }}>
            <Alert
              severity={batchResult.filtered_count > 0 ? 'warning' : 'success'}
              sx={{ mb: 2 }}
            >
              총 {batchResult.total}개 중 {batchResult.filtered_count}개가 필터링되었습니다.
            </Alert>

            <Stack spacing={1}>
              {batchResult.results.map((result, index) => (
                <Card key={index} variant="outlined">
                  <CardContent sx={{ py: 1, '&:last-child': { pb: 1 } }}>
                    <Stack direction="row" alignItems="center" spacing={2}>
                      <Typography variant="body2" sx={{ minWidth: 60 }}>
                        #{index + 1}
                      </Typography>
                      <Typography
                        variant="body2"
                        sx={{
                          flex: 1,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {batchComments.filter(c => c.trim())[index]}
                      </Typography>
                      {result.is_filtered ? (
                        <Chip
                          icon={getActionInfo(result.action).icon}
                          label={`${getActionInfo(result.action).label} - ${result.rule_name}`}
                          color={getActionInfo(result.action).color}
                          size="small"
                        />
                      ) : (
                        <Chip
                          icon={<PassIcon />}
                          label="통과"
                          color="success"
                          size="small"
                        />
                      )}
                    </Stack>
                  </CardContent>
                </Card>
              ))}
            </Stack>
          </Box>
        )}
      </Paper>
    </Box>
  );
}

export default CommentFilterTest;
