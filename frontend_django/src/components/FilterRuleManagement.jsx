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
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Card,
  CardContent,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
  FilterAlt as FilterIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
} from '@mui/icons-material';
import {
  getFilterRules,
  createFilterRule,
  updateFilterRule,
  deleteFilterRule,
} from '../api/client';

const FILTER_TYPES = [
  { value: 'keyword', label: '금칙어', description: '쉼표로 구분된 키워드 목록' },
  { value: 'regex', label: '정규식', description: '정규식 패턴' },
  { value: 'spam', label: '스팸', description: '스팸 패턴 (이모지, 특수문자 과다)' },
  { value: 'url', label: 'URL 차단', description: '차단할 도메인 목록 (빈 값이면 모든 URL 차단)' },
  { value: 'repeat', label: '반복 문자', description: '반복 허용 횟수 (기본 5)' },
];

const ACTIONS = [
  { value: 'block', label: '차단', color: 'error' },
  { value: 'hide', label: '숨김', color: 'warning' },
  { value: 'review', label: '검토 대기', color: 'info' },
];

const initialFormData = {
  name: '',
  filter_type: 'keyword',
  pattern: '',
  action: 'block',
  is_active: true,
  priority: 0,
  description: '',
};

function FilterRuleManagement() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 다이얼로그 상태
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // create | edit
  const [formData, setFormData] = useState(initialFormData);
  const [editingId, setEditingId] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  // 삭제 확인 다이얼로그
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingRule, setDeletingRule] = useState(null);

  // 규칙 목록 로드
  const loadRules = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getFilterRules();
      setRules(data.rules || []);
    } catch (err) {
      if (err.request) {
        setError('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
      } else {
        setError('규칙 목록을 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRules();
  }, []);

  // 다이얼로그 열기 (생성)
  const openCreateDialog = () => {
    setDialogMode('create');
    setFormData(initialFormData);
    setEditingId(null);
    setFormError('');
    setDialogOpen(true);
  };

  // 다이얼로그 열기 (수정)
  const openEditDialog = (rule) => {
    setDialogMode('edit');
    setFormData({
      name: rule.name,
      filter_type: rule.filter_type,
      pattern: rule.pattern,
      action: rule.action,
      is_active: rule.is_active,
      priority: rule.priority,
      description: rule.description || '',
    });
    setEditingId(rule.id);
    setFormError('');
    setDialogOpen(true);
  };

  // 다이얼로그 닫기
  const closeDialog = () => {
    setDialogOpen(false);
    setFormData(initialFormData);
    setEditingId(null);
    setFormError('');
  };

  // 폼 입력 변경
  const handleFormChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  // 규칙 저장
  const handleSave = async () => {
    if (!formData.name.trim()) {
      setFormError('규칙 이름을 입력해주세요.');
      return;
    }
    if (!formData.pattern.trim() && formData.filter_type !== 'spam') {
      setFormError('필터링 패턴을 입력해주세요.');
      return;
    }

    setFormLoading(true);
    setFormError('');

    try {
      if (dialogMode === 'create') {
        await createFilterRule(formData);
        setSuccess('규칙이 생성되었습니다.');
      } else {
        await updateFilterRule(editingId, formData);
        setSuccess('규칙이 수정되었습니다.');
      }
      closeDialog();
      loadRules();
    } catch (err) {
      if (err.response?.data?.details) {
        setFormError(JSON.stringify(err.response.data.details));
      } else {
        setFormError('저장 중 오류가 발생했습니다.');
      }
    } finally {
      setFormLoading(false);
    }
  };

  // 삭제 확인 다이얼로그 열기
  const openDeleteDialog = (rule) => {
    setDeletingRule(rule);
    setDeleteDialogOpen(true);
  };

  // 삭제 실행
  const handleDelete = async () => {
    if (!deletingRule) return;

    try {
      await deleteFilterRule(deletingRule.id);
      setSuccess('규칙이 삭제되었습니다.');
      setDeleteDialogOpen(false);
      setDeletingRule(null);
      loadRules();
    } catch (err) {
      setError('삭제 중 오류가 발생했습니다.');
    }
  };

  // 활성/비활성 토글
  const handleToggleActive = async (rule) => {
    try {
      await updateFilterRule(rule.id, { is_active: !rule.is_active });
      loadRules();
    } catch (err) {
      setError('상태 변경 중 오류가 발생했습니다.');
    }
  };

  // 필터 타입 라벨 반환
  const getFilterTypeLabel = (type) => {
    const found = FILTER_TYPES.find((t) => t.value === type);
    return found ? found.label : type;
  };

  // 조치 색상 반환
  const getActionColor = (action) => {
    const found = ACTIONS.find((a) => a.value === action);
    return found ? found.color : 'default';
  };

  // 조치 라벨 반환
  const getActionLabel = (action) => {
    const found = ACTIONS.find((a) => a.value === action);
    return found ? found.label : action;
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
        필터링 규칙 관리
      </Typography>

      {/* 상단 액션 바 */}
      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={openCreateDialog}
        >
          새 규칙 추가
        </Button>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={loadRules}
          disabled={loading}
        >
          새로고침
        </Button>
      </Stack>

      {/* 알림 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* 규칙 테이블 */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: 'grey.100' }}>
              <TableCell>상태</TableCell>
              <TableCell>이름</TableCell>
              <TableCell>타입</TableCell>
              <TableCell>패턴</TableCell>
              <TableCell>조치</TableCell>
              <TableCell align="center">우선순위</TableCell>
              <TableCell align="center">작업</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rules.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    등록된 필터링 규칙이 없습니다.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              rules.map((rule) => (
                <TableRow key={rule.id} hover>
                  <TableCell>
                    <Tooltip title={rule.is_active ? '활성화됨 (클릭하여 비활성화)' : '비활성화됨 (클릭하여 활성화)'}>
                      <IconButton
                        size="small"
                        onClick={() => handleToggleActive(rule)}
                        color={rule.is_active ? 'success' : 'default'}
                      >
                        {rule.is_active ? <ActiveIcon /> : <InactiveIcon />}
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    <Typography fontWeight={500}>{rule.name}</Typography>
                    {rule.description && (
                      <Typography variant="caption" color="text.secondary">
                        {rule.description}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      icon={<FilterIcon />}
                      label={getFilterTypeLabel(rule.filter_type)}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>
                    <Typography
                      variant="body2"
                      sx={{
                        maxWidth: 200,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      <code>{rule.pattern}</code>
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getActionLabel(rule.action)}
                      size="small"
                      color={getActionColor(rule.action)}
                    />
                  </TableCell>
                  <TableCell align="center">{rule.priority}</TableCell>
                  <TableCell align="center">
                    <Tooltip title="수정">
                      <IconButton
                        size="small"
                        onClick={() => openEditDialog(rule)}
                        color="primary"
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="삭제">
                      <IconButton
                        size="small"
                        onClick={() => openDeleteDialog(rule)}
                        color="error"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* 규칙 생성/수정 다이얼로그 */}
      <Dialog open={dialogOpen} onClose={closeDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {dialogMode === 'create' ? '새 필터링 규칙 추가' : '필터링 규칙 수정'}
        </DialogTitle>
        <DialogContent>
          {formError && (
            <Alert severity="error" sx={{ mb: 2, mt: 1 }}>
              {formError}
            </Alert>
          )}

          <TextField
            fullWidth
            label="규칙 이름"
            value={formData.name}
            onChange={(e) => handleFormChange('name', e.target.value)}
            sx={{ mt: 2, mb: 2 }}
            required
          />

          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>필터 타입</InputLabel>
            <Select
              value={formData.filter_type}
              label="필터 타입"
              onChange={(e) => handleFormChange('filter_type', e.target.value)}
            >
              {FILTER_TYPES.map((type) => (
                <MenuItem key={type.value} value={type.value}>
                  {type.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {/* 필터 타입 설명 카드 */}
          <Card variant="outlined" sx={{ mb: 2, backgroundColor: 'grey.50' }}>
            <CardContent sx={{ py: 1, '&:last-child': { pb: 1 } }}>
              <Typography variant="caption" color="text.secondary">
                {FILTER_TYPES.find((t) => t.value === formData.filter_type)?.description}
              </Typography>
            </CardContent>
          </Card>

          <TextField
            fullWidth
            multiline
            rows={3}
            label="필터링 패턴"
            value={formData.pattern}
            onChange={(e) => handleFormChange('pattern', e.target.value)}
            sx={{ mb: 2 }}
            placeholder={
              formData.filter_type === 'keyword'
                ? '예: 욕설1, 욕설2, 비속어'
                : formData.filter_type === 'regex'
                ? '예: \\d{3}-\\d{4}-\\d{4}'
                : formData.filter_type === 'url'
                ? '예: spam.com, ad.site.com (빈 값이면 모든 URL 차단)'
                : formData.filter_type === 'repeat'
                ? '예: 5 (같은 문자 5회 이상 반복 시 필터링)'
                : ''
            }
          />

          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>조치</InputLabel>
            <Select
              value={formData.action}
              label="조치"
              onChange={(e) => handleFormChange('action', e.target.value)}
            >
              {ACTIONS.map((action) => (
                <MenuItem key={action.value} value={action.value}>
                  {action.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <TextField
            fullWidth
            type="number"
            label="우선순위"
            value={formData.priority}
            onChange={(e) => handleFormChange('priority', parseInt(e.target.value) || 0)}
            sx={{ mb: 2 }}
            helperText="높을수록 먼저 적용됩니다"
          />

          <TextField
            fullWidth
            multiline
            rows={2}
            label="설명 (선택)"
            value={formData.description}
            onChange={(e) => handleFormChange('description', e.target.value)}
            sx={{ mb: 2 }}
          />

          <FormControlLabel
            control={
              <Switch
                checked={formData.is_active}
                onChange={(e) => handleFormChange('is_active', e.target.checked)}
              />
            }
            label="활성화"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog}>취소</Button>
          <Button
            variant="contained"
            onClick={handleSave}
            disabled={formLoading}
          >
            {dialogMode === 'create' ? '추가' : '저장'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>규칙 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            &quot;{deletingRule?.name}&quot; 규칙을 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>취소</Button>
          <Button variant="contained" color="error" onClick={handleDelete}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default FilterRuleManagement;
