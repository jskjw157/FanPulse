import React, { useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Grid,
  Collapse,
  IconButton,
  Paper,
} from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import PublicIcon from '@mui/icons-material/Public';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import TimerIcon from '@mui/icons-material/Timer';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ArticleIcon from '@mui/icons-material/Article';

function ResultCard({ result }) {
  const [showOriginal, setShowOriginal] = useState(false);

  if (!result) return null;

  const formatDate = (isoString) => {
    if (!isoString) return null;
    try {
      return new Date(isoString).toLocaleString('ko-KR');
    } catch {
      return null;
    }
  };

  return (
    <Card elevation={3} sx={{ mt: 3 }}>
      <CardContent>
        {/* Header */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="h5" component="h2" gutterBottom>
            {result.title || '요약 결과'}
          </Typography>

          {/* Metadata */}
          <Grid container spacing={1} sx={{ mt: 1 }}>
            {result.source && (
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <PublicIcon fontSize="small" color="action" />
                  <Typography variant="body2" color="text.secondary">
                    {result.source}
                  </Typography>
                </Box>
              </Grid>
            )}
            {result.published_at && (
              <Grid item xs={12} sm={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CalendarTodayIcon fontSize="small" color="action" />
                  <Typography variant="body2" color="text.secondary">
                    {formatDate(result.published_at)}
                  </Typography>
                </Box>
              </Grid>
            )}
          </Grid>
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Summary */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            요약
          </Typography>
          <Typography variant="body1" paragraph>
            {result.summary}
          </Typography>
        </Box>

        {/* Bullets */}
        {result.bullets && result.bullets.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              주요 포인트
            </Typography>
            <List dense>
              {result.bullets.map((bullet, index) => (
                <ListItem key={index} disablePadding>
                  <ListItemIcon sx={{ minWidth: 32 }}>
                    <FiberManualRecordIcon sx={{ fontSize: 8 }} />
                  </ListItemIcon>
                  <ListItemText primary={bullet} />
                </ListItem>
              ))}
            </List>
          </Box>
        )}

        {/* Keywords */}
        {result.keywords && result.keywords.length > 0 && (
          <Box sx={{ mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              키워드
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {result.keywords.map((keyword, index) => (
                <Chip
                  key={index}
                  label={keyword}
                  size="small"
                  variant="outlined"
                  color="primary"
                />
              ))}
            </Box>
          </Box>
        )}

        {/* Original Text */}
        {result.original_text && (
          <Box sx={{ mb: 2 }}>
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                cursor: 'pointer',
                '&:hover': { bgcolor: 'action.hover' },
                borderRadius: 1,
                p: 1,
                ml: -1,
              }}
              onClick={() => setShowOriginal(!showOriginal)}
            >
              <ArticleIcon sx={{ mr: 1 }} color="action" />
              <Typography variant="h6" sx={{ flexGrow: 1 }}>
                원문 보기
              </Typography>
              <IconButton size="small">
                {showOriginal ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </IconButton>
            </Box>
            <Collapse in={showOriginal}>
              <Paper
                variant="outlined"
                sx={{
                  p: 2,
                  mt: 1,
                  maxHeight: 400,
                  overflow: 'auto',
                  bgcolor: 'grey.50',
                }}
              >
                <Typography
                  variant="body2"
                  sx={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}
                >
                  {result.original_text}
                </Typography>
              </Paper>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                원문 길이: {result.original_text.length}자
              </Typography>
            </Collapse>
          </Box>
        )}

        <Divider sx={{ my: 2 }} />

        {/* Footer metadata */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TimerIcon fontSize="small" color="action" />
            <Typography variant="caption" color="text.secondary">
              처리 시간: {result.elapsed_ms}ms
            </Typography>
          </Box>
          <Typography variant="caption" color="text.secondary">
            ID: {result.request_id}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
}

export default ResultCard;
