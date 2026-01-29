import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Alert,
  Divider,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import RestoreIcon from '@mui/icons-material/Restore';
import { getSettings, saveSettings, resetSettings } from '../utils/storage';

function Settings() {
  const [language, setLanguage] = useState('ko');
  const [maxLength, setMaxLength] = useState(200);
  const [minLength, setMinLength] = useState(50);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = () => {
    const settings = getSettings();
    setLanguage(settings.defaultLanguage);
    setMaxLength(settings.defaultMaxLength);
    setMinLength(settings.defaultMinLength);
  };

  const handleSave = () => {
    const settings = {
      defaultLanguage: language,
      defaultMaxLength: maxLength,
      defaultMinLength: minLength,
    };

    if (saveSettings(settings)) {
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    }
  };

  const handleReset = () => {
    if (window.confirm('설정을 기본값으로 초기화하시겠습니까?')) {
      const defaults = resetSettings();
      setLanguage(defaults.defaultLanguage);
      setMaxLength(defaults.defaultMaxLength);
      setMinLength(defaults.defaultMinLength);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        설정
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        요약 기본 옵션을 설정합니다. 이 설정은 URL 요약과 텍스트 요약 페이지에 자동으로 적용됩니다.
      </Typography>

      <Paper elevation={2} sx={{ p: 3, maxWidth: 600 }}>
        <Typography variant="h6" gutterBottom>
          기본 옵션
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <FormControl fullWidth>
            <InputLabel>기본 언어</InputLabel>
            <Select
              value={language}
              label="기본 언어"
              onChange={(e) => setLanguage(e.target.value)}
            >
              <MenuItem value="ko">한국어</MenuItem>
              <MenuItem value="en">English</MenuItem>
            </Select>
          </FormControl>

          <TextField
            fullWidth
            type="number"
            label="기본 최소 길이"
            value={minLength}
            onChange={(e) => setMinLength(parseInt(e.target.value) || 50)}
            inputProps={{ min: 10, max: 500 }}
            helperText="요약의 최소 길이 (10-500자)"
          />

          <TextField
            fullWidth
            type="number"
            label="기본 최대 길이"
            value={maxLength}
            onChange={(e) => setMaxLength(parseInt(e.target.value) || 200)}
            inputProps={{ min: 50, max: 1000 }}
            helperText="요약의 최대 길이 (50-1000자)"
          />

          {minLength > maxLength && (
            <Alert severity="warning">
              최소 길이는 최대 길이보다 작아야 합니다.
            </Alert>
          )}

          <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              onClick={handleSave}
              disabled={minLength > maxLength}
            >
              저장
            </Button>
            <Button
              variant="outlined"
              startIcon={<RestoreIcon />}
              onClick={handleReset}
            >
              초기화
            </Button>
          </Box>

          {saved && (
            <Alert severity="success">
              설정이 저장되었습니다.
            </Alert>
          )}
        </Box>
      </Paper>

      <Paper elevation={2} sx={{ p: 3, maxWidth: 600, mt: 3 }}>
        <Typography variant="h6" gutterBottom>
          정보
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Typography variant="body2" paragraph>
          <strong>버전:</strong> 1.0.0
        </Typography>
        <Typography variant="body2" paragraph>
          <strong>백엔드 API:</strong> http://localhost:8000
        </Typography>
        <Typography variant="body2" paragraph>
          <strong>프론트엔드:</strong> React + Vite + Material-UI
        </Typography>
        <Typography variant="body2" color="text.secondary">
          모든 설정은 브라우저의 localStorage에 저장됩니다.
        </Typography>
      </Paper>
    </Box>
  );
}

export default Settings;
