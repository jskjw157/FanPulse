import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Stack,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Link,
  Paper,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SummarizeIcon from '@mui/icons-material/Summarize';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import ArticleIcon from '@mui/icons-material/Article';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import DeleteIcon from '@mui/icons-material/Delete';
import FolderIcon from '@mui/icons-material/Folder';
import {
  getSummarizedNewsList,
  getSummarizedNewsDetail,
  deleteSummarizedNews,
} from '../api/client';

function SummarizedNews() {
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState('');
  const [newsItems, setNewsItems] = useState([]);
  const [fileInfo, setFileInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingFile, setLoadingFile] = useState(false);
  const [error, setError] = useState(null);
  const [deleteDialog, setDeleteDialog] = useState({ open: false, filename: null });

  // 파일 목록 로드
  const loadFiles = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getSummarizedNewsList();
      if (data.success) {
        setFiles(data.files || []);
        // 선택된 파일이 없거나 삭제된 경우 초기화
        if (selectedFile && !data.files?.find((f) => f.filename === selectedFile)) {
          setSelectedFile('');
          setNewsItems([]);
          setFileInfo(null);
        }
      } else {
        setError(data.error || '파일 목록을 불러오는데 실패했습니다.');
      }
    } catch (err) {
      setError(err.message || '서버 연결에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 파일 내용 로드
  const loadFileContent = async (filename) => {
    if (!filename) {
      setNewsItems([]);
      setFileInfo(null);
      return;
    }

    setLoadingFile(true);
    setError(null);

    try {
      const data = await getSummarizedNewsDetail(filename);
      if (data.success) {
        setNewsItems(data.items || []);
        setFileInfo({
          method: data.method,
          created_at: data.created_at,
          count: data.count,
        });
      } else {
        setError(data.error || '파일을 불러오는데 실패했습니다.');
        setNewsItems([]);
        setFileInfo(null);
      }
    } catch (err) {
      setError(err.message || '서버 연결에 실패했습니다.');
      setNewsItems([]);
      setFileInfo(null);
    } finally {
      setLoadingFile(false);
    }
  };

  // 파일 삭제
  const handleDelete = async () => {
    const { filename } = deleteDialog;
    setDeleteDialog({ open: false, filename: null });

    try {
      const data = await deleteSummarizedNews(filename);
      if (data.success) {
        setFiles((prev) => prev.filter((f) => f.filename !== filename));
        if (selectedFile === filename) {
          setSelectedFile('');
          setNewsItems([]);
          setFileInfo(null);
        }
      } else {
        setError(data.error || '삭제에 실패했습니다.');
      }
    } catch (err) {
      setError(err.message || '삭제에 실패했습니다.');
    }
  };

  // 파일 선택 변경
  const handleFileChange = (event) => {
    const filename = event.target.value;
    setSelectedFile(filename);
    loadFileContent(filename);
  };

  // 날짜 포맷
  const formatDate = (isoString) => {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleString('ko-KR');
  };

  // 요약 방식 표시
  const getMethodLabel = (method) => {
    switch (method) {
      case 'rule':
        return { label: '알고리즘', icon: <SummarizeIcon fontSize="small" />, color: 'info' };
      case 'ai':
        return { label: 'AI 모델', icon: <AutoAwesomeIcon fontSize="small" />, color: 'secondary' };
      default:
        return { label: method, icon: null, color: 'default' };
    }
  };

  useEffect(() => {
    loadFiles();
  }, []);

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            요약된 뉴스
          </Typography>
          <Typography variant="body2" color="text.secondary">
            저장된 뉴스에서 요약한 결과를 확인합니다.
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

      {/* 에러 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* 파일 선택 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction="row" spacing={2} alignItems="center">
            <FormControl fullWidth>
              <InputLabel>요약 결과 파일 선택</InputLabel>
              <Select
                value={selectedFile}
                label="요약 결과 파일 선택"
                onChange={handleFileChange}
                disabled={loading || loadingFile}
              >
                <MenuItem value="">
                  <em>파일을 선택하세요</em>
                </MenuItem>
                {files.map((file) => {
                  const methodInfo = getMethodLabel(file.method);
                  return (
                    <MenuItem key={file.filename} value={file.filename}>
                      <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%' }}>
                        <span>{file.filename}</span>
                        <Chip
                          icon={methodInfo.icon}
                          label={methodInfo.label}
                          size="small"
                          color={methodInfo.color}
                          variant="outlined"
                        />
                        <Chip label={`${file.count}개`} size="small" variant="outlined" />
                      </Stack>
                    </MenuItem>
                  );
                })}
              </Select>
            </FormControl>
            {selectedFile && (
              <Tooltip title="파일 삭제">
                <IconButton
                  color="error"
                  onClick={() => setDeleteDialog({ open: true, filename: selectedFile })}
                >
                  <DeleteIcon />
                </IconButton>
              </Tooltip>
            )}
          </Stack>

          {/* 파일 정보 */}
          {fileInfo && (
            <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
              <Chip
                icon={getMethodLabel(fileInfo.method).icon}
                label={`요약 방식: ${getMethodLabel(fileInfo.method).label}`}
                color={getMethodLabel(fileInfo.method).color}
              />
              <Chip label={`총 ${fileInfo.count}개 뉴스`} variant="outlined" />
              <Chip label={`생성일: ${formatDate(fileInfo.created_at)}`} variant="outlined" />
            </Stack>
          )}
        </CardContent>
      </Card>

      {/* 로딩 */}
      {loadingFile && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {/* 뉴스 목록 */}
      {!loadingFile && newsItems.length > 0 && (
        <Stack spacing={2}>
          {newsItems.map((item, index) => {
            const isSuccess = item.summarized;

            return (
              <Accordion key={index} defaultExpanded={index === 0}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%', pr: 2 }}>
                    <ArticleIcon color={isSuccess ? 'primary' : 'disabled'} />
                    <Typography variant="subtitle1" fontWeight="medium" sx={{ flexGrow: 1 }}>
                      {item.title || '제목 없음'}
                    </Typography>
                    {isSuccess ? (
                      <Chip label="요약 완료" size="small" color="success" />
                    ) : (
                      <Chip label="요약 실패" size="small" color="error" />
                    )}
                    {item.pubDate && (
                      <Chip label={item.pubDate} size="small" variant="outlined" />
                    )}
                  </Stack>
                </AccordionSummary>
                <AccordionDetails>
                  <Stack spacing={2}>
                    {/* 링크 */}
                    {item.originallink && (
                      <Box>
                        <Link
                          href={item.originallink}
                          target="_blank"
                          rel="noopener noreferrer"
                          sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
                        >
                          원문 보기 <OpenInNewIcon fontSize="small" />
                        </Link>
                      </Box>
                    )}

                    {/* 에러 메시지 */}
                    {item.error && (
                      <Alert severity="error">{item.error}</Alert>
                    )}

                    {/* 요약 결과 */}
                    {isSuccess && (
                      <Paper
                        variant="outlined"
                        sx={{ p: 2, bgcolor: 'primary.50', borderColor: 'primary.main' }}
                      >
                        <Stack spacing={2}>
                          <Box>
                            <Typography variant="subtitle2" color="primary" gutterBottom>
                              <SummarizeIcon sx={{ verticalAlign: 'middle', mr: 0.5 }} />
                              요약 결과
                            </Typography>
                            <Typography variant="body2">
                              {item.summary}
                            </Typography>
                          </Box>

                          {item.bullets?.length > 0 && (
                            <Box>
                              <Typography variant="subtitle2" color="primary" gutterBottom>
                                핵심 포인트
                              </Typography>
                              <List dense>
                                {item.bullets.map((bullet, i) => (
                                  <ListItem key={i} sx={{ py: 0 }}>
                                    <ListItemText
                                      primary={`• ${bullet}`}
                                      primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                  </ListItem>
                                ))}
                              </List>
                            </Box>
                          )}

                          {item.keywords?.length > 0 && (
                            <Box>
                              <Typography variant="subtitle2" color="primary" gutterBottom>
                                키워드
                              </Typography>
                              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                                {item.keywords.map((keyword, i) => (
                                  <Chip key={i} label={keyword} size="small" color="primary" variant="outlined" />
                                ))}
                              </Stack>
                            </Box>
                          )}
                        </Stack>
                      </Paper>
                    )}

                    {/* 원본 텍스트 미리보기 */}
                    {item.original_text && (
                      <Accordion>
                        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                          <Typography variant="body2" color="text.secondary">
                            원본 텍스트 미리보기
                          </Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                          <Paper variant="outlined" sx={{ p: 2, maxHeight: 200, overflow: 'auto' }}>
                            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                              {item.original_text}
                            </Typography>
                          </Paper>
                        </AccordionDetails>
                      </Accordion>
                    )}
                  </Stack>
                </AccordionDetails>
              </Accordion>
            );
          })}
        </Stack>
      )}

      {/* 빈 상태 */}
      {!loadingFile && selectedFile && newsItems.length === 0 && (
        <Card variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <ArticleIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography color="text.secondary">
            선택한 파일에 요약된 뉴스가 없습니다.
          </Typography>
        </Card>
      )}

      {/* 파일 미선택 */}
      {!loadingFile && !selectedFile && files.length > 0 && (
        <Card variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <SummarizeIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography color="text.secondary">
            위에서 파일을 선택하면 요약 결과가 표시됩니다.
          </Typography>
        </Card>
      )}

      {/* 저장된 파일 없음 */}
      {!loading && files.length === 0 && (
        <Card variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <FolderIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
          <Typography color="text.secondary">
            요약된 뉴스 파일이 없습니다.
          </Typography>
          <Typography variant="body2" color="text.secondary">
            저장된 뉴스 페이지에서 뉴스를 선택하고 요약하세요.
          </Typography>
        </Card>
      )}

      {/* 삭제 확인 다이얼로그 */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, filename: null })}
      >
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
    </Box>
  );
}

export default SummarizedNews;
