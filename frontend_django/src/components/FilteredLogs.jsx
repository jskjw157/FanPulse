import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Alert,
  LinearProgress,
  Chip,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Visibility as ViewIcon,
  Block as BlockIcon,
  VisibilityOff as HideIcon,
  RateReview as ReviewIcon,
  History as LogIcon,
} from '@mui/icons-material';
import { getFilteredLogs } from '../api/client';

const ACTIONS = [
  { value: '', label: '전체', color: 'default' },
  { value: 'block', label: '차단', color: 'error', icon: <BlockIcon /> },
  { value: 'hide', label: '숨김', color: 'warning', icon: <HideIcon /> },
  { value: 'review', label: '검토 대기', color: 'info', icon: <ReviewIcon /> },
];

function FilteredLogs() {
  const [logs, setLogs] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 페이지네이션
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  // 필터
  const [actionFilter, setActionFilter] = useState('');

  // 상세 보기 다이얼로그
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);

  // 로그 목록 로드
  const loadLogs = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getFilteredLogs({
        limit: rowsPerPage,
        offset: page * rowsPerPage,
        action: actionFilter || undefined,
      });
      setLogs(data.logs || []);
      setTotal(data.total || 0);
    } catch (err) {
      if (err.request) {
        setError('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
      } else {
        setError('로그를 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
  }, [page, rowsPerPage, actionFilter]);

  // 페이지 변경
  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  // 페이지 크기 변경
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // 필터 변경
  const handleActionFilterChange = (event) => {
    setActionFilter(event.target.value);
    setPage(0);
  };

  // 상세 보기
  const openDetail = (log) => {
    setSelectedLog(log);
    setDetailOpen(true);
  };

  // 조치 정보 반환
  const getActionInfo = (action) => {
    const found = ACTIONS.find((a) => a.value === action);
    return found || { label: action, color: 'default' };
  };

  // 날짜 포맷팅
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  // 텍스트 줄임
  const truncateText = (text, maxLength = 50) => {
    if (!text) return '-';
    return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom sx={{ fontWeight: 600, mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
        <LogIcon color="primary" />
        필터링 로그
      </Typography>

      {/* 상단 필터 및 액션 바 */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>조치 필터</InputLabel>
            <Select
              value={actionFilter}
              label="조치 필터"
              onChange={handleActionFilterChange}
            >
              {ACTIONS.map((action) => (
                <MenuItem key={action.value} value={action.value}>
                  {action.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadLogs}
            disabled={loading}
          >
            새로고침
          </Button>

          <Box sx={{ flex: 1 }} />

          <Typography variant="body2" color="text.secondary">
            총 {total}개의 로그
          </Typography>
        </Stack>
      </Paper>

      {/* 알림 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* 로그 테이블 */}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ backgroundColor: 'grey.100' }}>
              <TableCell>시간</TableCell>
              <TableCell>원본 내용</TableCell>
              <TableCell>매칭 패턴</TableCell>
              <TableCell>조치</TableCell>
              <TableCell align="center">상세</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {logs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    {loading ? '로딩 중...' : '필터링 로그가 없습니다.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              logs.map((log) => (
                <TableRow key={log.id} hover>
                  <TableCell>
                    <Typography variant="caption">
                      {formatDate(log.created_at)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Tooltip title={log.original_content}>
                      <Typography
                        variant="body2"
                        sx={{
                          maxWidth: 300,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {truncateText(log.original_content, 40)}
                      </Typography>
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    <code style={{ fontSize: '12px' }}>
                      {truncateText(log.matched_pattern, 20)}
                    </code>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getActionInfo(log.action_taken).label}
                      size="small"
                      color={getActionInfo(log.action_taken).color}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Tooltip title="상세 보기">
                      <IconButton
                        size="small"
                        onClick={() => openDetail(log)}
                        color="primary"
                      >
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[10, 25, 50, 100]}
          labelRowsPerPage="페이지당 행:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} / ${count}`}
        />
      </TableContainer>

      {/* 상세 보기 다이얼로그 */}
      <Dialog open={detailOpen} onClose={() => setDetailOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>필터링 로그 상세</DialogTitle>
        <DialogContent dividers>
          {selectedLog && (
            <Stack spacing={2}>
              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  필터링 시간
                </Typography>
                <Typography>{formatDate(selectedLog.created_at)}</Typography>
              </Box>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  조치
                </Typography>
                <Chip
                  label={getActionInfo(selectedLog.action_taken).label}
                  color={getActionInfo(selectedLog.action_taken).color}
                  size="small"
                />
              </Box>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  매칭된 패턴
                </Typography>
                <Paper variant="outlined" sx={{ p: 1, backgroundColor: 'grey.50' }}>
                  <code>{selectedLog.matched_pattern}</code>
                </Paper>
              </Box>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  원본 내용
                </Typography>
                <Paper variant="outlined" sx={{ p: 2, backgroundColor: 'grey.50' }}>
                  <Typography
                    variant="body2"
                    sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}
                  >
                    {selectedLog.original_content}
                  </Typography>
                </Paper>
              </Box>

              {selectedLog.comment_id && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    댓글 ID
                  </Typography>
                  <Typography variant="body2" fontFamily="monospace">
                    {selectedLog.comment_id}
                  </Typography>
                </Box>
              )}

              {selectedLog.filter_rule_id && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    필터 규칙 ID
                  </Typography>
                  <Typography variant="body2" fontFamily="monospace">
                    {selectedLog.filter_rule_id}
                  </Typography>
                </Box>
              )}
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailOpen(false)}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default FilteredLogs;
