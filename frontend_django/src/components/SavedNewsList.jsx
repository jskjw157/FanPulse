import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Stack,
  IconButton,
  Chip,
  Collapse,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tooltip,
  Link,
  Checkbox,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Snackbar,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import DeleteIcon from '@mui/icons-material/Delete';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import FolderIcon from '@mui/icons-material/Folder';
import ArticleIcon from '@mui/icons-material/Article';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import SummarizeIcon from '@mui/icons-material/Summarize';
import { getSavedNewsList, getSavedNewsDetail, deleteSavedNews, batchSummarize } from '../api/client';

function SavedNewsList() {
  const navigate = useNavigate();
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expandedFile, setExpandedFile] = useState(null);
  const [fileContents, setFileContents] = useState({});
  const [deleteDialog, setDeleteDialog] = useState({ open: false, filename: null });

  // 선택 관련 상태
  const [selectedItems, setSelectedItems] = useState({});
  const [summarizeMethod, setSummarizeMethod] = useState('rule');
  const [summarizing, setSummarizing] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  // 파일 목록 로드
  const loadFiles = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getSavedNewsList();
      if (data.success) {
        setFiles(data.files || []);
      } else {
        setError(data.error || '파일 목록을 불러오는데 실패했습니다.');
      }
    } catch (err) {
      setError(err.message || '서버 연결에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 파일 상세 내용 로드
  const loadFileContent = async (filename) => {
    if (fileContents[filename]) {
      return;
    }

    try {
      const data = await getSavedNewsDetail(filename);
      if (data.success) {
        setFileContents((prev) => ({
          ...prev,
          [filename]: data.items || [],
        }));
      }
    } catch (err) {
      console.error('파일 내용 로드 실패:', err);
    }
  };

  // 파일 확장/축소 토글
  const handleToggleExpand = async (filename) => {
    if (expandedFile === filename) {
      setExpandedFile(null);
    } else {
      setExpandedFile(filename);
      await loadFileContent(filename);
    }
  };

  // 파일 삭제
  const handleDelete = async () => {
    const { filename } = deleteDialog;
    setDeleteDialog({ open: false, filename: null });

    try {
      const data = await deleteSavedNews(filename);
      if (data.success) {
        setFiles((prev) => prev.filter((f) => f.filename !== filename));
        setFileContents((prev) => {
          const newContents = { ...prev };
          delete newContents[filename];
          return newContents;
        });
        if (expandedFile === filename) {
          setExpandedFile(null);
        }
        // 선택 상태도 제거
        setSelectedItems((prev) => {
          const newSelected = { ...prev };
          delete newSelected[filename];
          return newSelected;
        });
      }
    } catch (err) {
      setError(err.message || '삭제에 실패했습니다.');
    }
  };

  // 개별 뉴스 선택/해제
  const handleSelectItem = (filename, index, item) => {
    setSelectedItems((prev) => {
      const fileSelected = prev[filename] || {};
      const key = `${index}`;

      if (fileSelected[key]) {
        const newFileSelected = { ...fileSelected };
        delete newFileSelected[key];

        if (Object.keys(newFileSelected).length === 0) {
          const newSelected = { ...prev };
          delete newSelected[filename];
          return newSelected;
        }

        return { ...prev, [filename]: newFileSelected };
      } else {
        return {
          ...prev,
          [filename]: { ...fileSelected, [key]: item },
        };
      }
    });
  };

  // 파일 전체 선택/해제
  const handleSelectAll = (filename) => {
    const items = fileContents[filename] || [];
    const fileSelected = selectedItems[filename] || {};
    const allSelected = items.length > 0 && Object.keys(fileSelected).length === items.length;

    if (allSelected) {
      // 전체 해제
      setSelectedItems((prev) => {
        const newSelected = { ...prev };
        delete newSelected[filename];
        return newSelected;
      });
    } else {
      // 전체 선택
      const newFileSelected = {};
      items.forEach((item, index) => {
        newFileSelected[`${index}`] = item;
      });
      setSelectedItems((prev) => ({
        ...prev,
        [filename]: newFileSelected,
      }));
    }
  };

  // 선택된 총 개수 계산
  const getTotalSelectedCount = () => {
    let count = 0;
    Object.values(selectedItems).forEach((fileSelected) => {
      count += Object.keys(fileSelected).length;
    });
    return count;
  };

  // 선택된 아이템 배열로 변환
  const getSelectedItemsArray = () => {
    const items = [];
    Object.values(selectedItems).forEach((fileSelected) => {
      Object.values(fileSelected).forEach((item) => {
        items.push(item);
      });
    });
    return items;
  };

  // 배치 요약 실행
  const handleBatchSummarize = async () => {
    const items = getSelectedItemsArray();
    if (items.length === 0) {
      setSnackbar({ open: true, message: '요약할 뉴스를 선택해주세요.', severity: 'warning' });
      return;
    }

    setSummarizing(true);
    setError(null);

    try {
      const result = await batchSummarize(items, summarizeMethod);

      if (result.success) {
        setSnackbar({
          open: true,
          message: `${result.count}개 뉴스 요약이 완료되었습니다.`,
          severity: 'success',
        });
        // 선택 초기화
        setSelectedItems({});
        // 요약 페이지로 이동
        navigate('/summarized-news');
      } else {
        setError(result.error || '요약에 실패했습니다.');
      }
    } catch (err) {
      setError(err.message || '서버 연결에 실패했습니다.');
    } finally {
      setSummarizing(false);
    }
  };

  // 파일 크기 포맷
  const formatSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  // 날짜 포맷
  const formatDate = (isoString) => {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleString('ko-KR');
  };

  useEffect(() => {
    loadFiles();
  }, []);

  const totalSelected = getTotalSelectedCount();

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            저장된 뉴스 목록
          </Typography>
          <Typography variant="body2" color="text.secondary">
            검색하여 저장된 뉴스 JSON 파일을 조회합니다. 뉴스를 선택하여 요약할 수 있습니다.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={loading ? <CircularProgress size={20} /> : <RefreshIcon />}
          onClick={loadFiles}
          disabled={loading}
        >
          새로고침
        </Button>
      </Stack>

      {/* 요약 컨트롤 */}
      {totalSelected > 0 && (
        <Card sx={{ mb: 3, bgcolor: 'primary.50', borderColor: 'primary.main' }} variant="outlined">
          <CardContent>
            <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
              <Stack direction="row" alignItems="center" spacing={2}>
                <Chip
                  icon={<ArticleIcon />}
                  label={`${totalSelected}개 선택됨`}
                  color="primary"
                />
                <FormControl size="small" sx={{ minWidth: 150 }}>
                  <InputLabel>요약 방식</InputLabel>
                  <Select
                    value={summarizeMethod}
                    label="요약 방식"
                    onChange={(e) => setSummarizeMethod(e.target.value)}
                    disabled={summarizing}
                  >
                    <MenuItem value="rule">
                      <Stack direction="row" alignItems="center" spacing={1}>
                        <SummarizeIcon fontSize="small" />
                        <span>알고리즘</span>
                      </Stack>
                    </MenuItem>
                    <MenuItem value="ai">
                      <Stack direction="row" alignItems="center" spacing={1}>
                        <AutoAwesomeIcon fontSize="small" />
                        <span>AI 모델</span>
                      </Stack>
                    </MenuItem>
                  </Select>
                </FormControl>
              </Stack>
              <Stack direction="row" spacing={1}>
                <Button
                  variant="outlined"
                  onClick={() => setSelectedItems({})}
                  disabled={summarizing}
                >
                  선택 해제
                </Button>
                <Button
                  variant="contained"
                  startIcon={summarizing ? <CircularProgress size={20} color="inherit" /> : <AutoAwesomeIcon />}
                  onClick={handleBatchSummarize}
                  disabled={summarizing}
                >
                  {summarizing ? '요약 중...' : '선택한 뉴스 요약하기'}
                </Button>
              </Stack>
            </Stack>
          </CardContent>
        </Card>
      )}

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* 로딩 */}
      {loading && files.length === 0 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {/* 파일 목록 */}
      {!loading && files.length === 0 && !error && (
        <Card variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <FolderIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography color="text.secondary">
            저장된 뉴스 파일이 없습니다.
          </Typography>
          <Typography variant="body2" color="text.secondary">
            뉴스 검색 페이지에서 "JSON 파일로 저장"을 체크하고 검색하세요.
          </Typography>
        </Card>
      )}

      {files.length > 0 && (
        <Stack spacing={2}>
          {files.map((file) => {
            const fileSelected = selectedItems[file.filename] || {};
            const fileItems = fileContents[file.filename] || [];
            const allSelected = fileItems.length > 0 && Object.keys(fileSelected).length === fileItems.length;
            const someSelected = Object.keys(fileSelected).length > 0 && !allSelected;

            return (
              <Card key={file.filename} variant="outlined">
                <CardContent sx={{ pb: 1 }}>
                  <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <Stack direction="row" alignItems="center" spacing={1}>
                      <ArticleIcon color="primary" />
                      <Typography variant="subtitle1" fontWeight="medium">
                        {file.filename}
                      </Typography>
                    </Stack>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Chip label={formatSize(file.size)} size="small" variant="outlined" />
                      <Chip label={formatDate(file.created_at)} size="small" variant="outlined" />
                      <Tooltip title="삭제">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => setDeleteDialog({ open: true, filename: file.filename })}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title={expandedFile === file.filename ? '접기' : '펼치기'}>
                        <IconButton size="small" onClick={() => handleToggleExpand(file.filename)}>
                          {expandedFile === file.filename ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                        </IconButton>
                      </Tooltip>
                    </Stack>
                  </Stack>
                </CardContent>

                {/* 파일 내용 (확장 시) */}
                <Collapse in={expandedFile === file.filename}>
                  <Box sx={{ px: 2, pb: 2 }}>
                    {fileContents[file.filename] ? (
                      <>
                        {/* 전체 선택 버튼 */}
                        <Stack direction="row" spacing={1} sx={{ mb: 1 }}>
                          <Button
                            size="small"
                            variant={allSelected ? 'contained' : 'outlined'}
                            onClick={() => handleSelectAll(file.filename)}
                          >
                            {allSelected ? '전체 해제' : '전체 선택'}
                          </Button>
                          {Object.keys(fileSelected).length > 0 && (
                            <Chip
                              label={`${Object.keys(fileSelected).length}개 선택`}
                              size="small"
                              color="primary"
                            />
                          )}
                        </Stack>
                        <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 400 }}>
                          <Table size="small" stickyHeader>
                            <TableHead>
                              <TableRow>
                                <TableCell padding="checkbox">
                                  <Checkbox
                                    indeterminate={someSelected}
                                    checked={allSelected}
                                    onChange={() => handleSelectAll(file.filename)}
                                  />
                                </TableCell>
                                <TableCell width={50}>#</TableCell>
                                <TableCell>제목</TableCell>
                                <TableCell width={150}>발행일</TableCell>
                                <TableCell width={100}>원문</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {fileContents[file.filename].map((item, index) => {
                                const isSelected = !!fileSelected[`${index}`];
                                const hasContent = item.origainal_news && item.origainal_news.length >= 50;

                                return (
                                  <TableRow
                                    key={index}
                                    hover
                                    selected={isSelected}
                                    sx={{ cursor: hasContent ? 'pointer' : 'default' }}
                                  >
                                    <TableCell padding="checkbox">
                                      <Tooltip title={hasContent ? '' : '원본 기사가 없거나 너무 짧습니다'}>
                                        <span>
                                          <Checkbox
                                            checked={isSelected}
                                            disabled={!hasContent}
                                            onChange={() => handleSelectItem(file.filename, index, item)}
                                          />
                                        </span>
                                      </Tooltip>
                                    </TableCell>
                                    <TableCell>{index + 1}</TableCell>
                                    <TableCell>
                                      <Typography variant="body2" noWrap sx={{ maxWidth: 400 }}>
                                        {item.title}
                                      </Typography>
                                      {item.origainal_news && (
                                        <Typography
                                          variant="caption"
                                          color="text.secondary"
                                          sx={{
                                            display: 'block',
                                            maxWidth: 400,
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            whiteSpace: 'nowrap',
                                          }}
                                        >
                                          {item.origainal_news.substring(0, 100)}...
                                        </Typography>
                                      )}
                                    </TableCell>
                                    <TableCell>
                                      <Typography variant="caption">
                                        {item.pubDate || '-'}
                                      </Typography>
                                    </TableCell>
                                    <TableCell>
                                      {item.originallink && (
                                        <Link
                                          href={item.originallink}
                                          target="_blank"
                                          rel="noopener noreferrer"
                                          sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
                                        >
                                          <OpenInNewIcon fontSize="small" />
                                        </Link>
                                      )}
                                    </TableCell>
                                  </TableRow>
                                );
                              })}
                            </TableBody>
                          </Table>
                        </TableContainer>
                      </>
                    ) : (
                      <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                        <CircularProgress size={24} />
                      </Box>
                    )}
                  </Box>
                </Collapse>
              </Card>
            );
          })}
        </Stack>
      )}

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, filename: null })}>
        <DialogTitle>파일 삭제</DialogTitle>
        <DialogContent>
          <DialogContentText>
            <strong>{deleteDialog.filename}</strong> 파일을 삭제하시겠습니까?
            <br />
            이 작업은 되돌릴 수 없습니다.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, filename: null })}>취소</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* 스낵바 알림 */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}

export default SavedNewsList;
