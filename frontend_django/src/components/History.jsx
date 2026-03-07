import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Paper,
  Chip,
  Button,
  Alert,
  Divider,
  IconButton,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import RefreshIcon from '@mui/icons-material/Refresh';
import { getHistory, clearHistory } from '../utils/storage';
import ResultCard from './ResultCard';

function History() {
  const [history, setHistory] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);

  const loadHistory = () => {
    const data = getHistory();
    setHistory(data);
    if (data.length > 0 && !selectedItem) {
      setSelectedItem(data[0]);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const handleClearHistory = () => {
    if (window.confirm('모든 히스토리를 삭제하시겠습니까?')) {
      clearHistory();
      setHistory([]);
      setSelectedItem(null);
    }
  };

  const formatDate = (isoString) => {
    try {
      const date = new Date(isoString);
      return date.toLocaleString('ko-KR', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return 'Unknown';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">
          히스토리
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton onClick={loadHistory} color="primary">
            <RefreshIcon />
          </IconButton>
          <Button
            variant="outlined"
            color="error"
            startIcon={<DeleteIcon />}
            onClick={handleClearHistory}
            disabled={history.length === 0}
          >
            전체 삭제
          </Button>
        </Box>
      </Box>

      {history.length === 0 ? (
        <Alert severity="info">
          아직 요약 히스토리가 없습니다. URL 또는 텍스트 요약을 실행해보세요.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', md: 'row' } }}>
          <Paper sx={{ width: { xs: '100%', md: 300 }, flexShrink: 0 }}>
            <List>
              {history.map((item, index) => (
                <React.Fragment key={item.id}>
                  <ListItem disablePadding>
                    <ListItemButton
                      selected={selectedItem?.id === item.id}
                      onClick={() => setSelectedItem(item)}
                    >
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Chip
                              label={item.input_type === 'url' ? 'URL' : 'TEXT'}
                              size="small"
                              color={item.input_type === 'url' ? 'primary' : 'secondary'}
                            />
                            <Typography variant="body2" noWrap>
                              {item.title || 'No title'}
                            </Typography>
                          </Box>
                        }
                        secondary={formatDate(item.created_at)}
                      />
                    </ListItemButton>
                  </ListItem>
                  {index < history.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>

          <Box sx={{ flexGrow: 1 }}>
            {selectedItem ? (
              <ResultCard result={selectedItem} />
            ) : (
              <Alert severity="info">
                좌측 목록에서 항목을 선택하세요.
              </Alert>
            )}
          </Box>
        </Box>
      )}
    </Box>
  );
}

export default History;
