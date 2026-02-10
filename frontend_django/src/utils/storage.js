/**
 * localStorage utility for managing app state
 */

const STORAGE_KEYS = {
  HISTORY: 'news_summarizer_history',
  SETTINGS: 'news_summarizer_settings',
};

// Default settings
const DEFAULT_SETTINGS = {
  defaultLanguage: 'ko',
  defaultMaxLength: 200,
  defaultMinLength: 50,
};

/**
 * History management
 */
export const getHistory = () => {
  try {
    const data = localStorage.getItem(STORAGE_KEYS.HISTORY);
    return data ? JSON.parse(data) : [];
  } catch (error) {
    console.error('Error reading history:', error);
    return [];
  }
};

export const saveToHistory = (item) => {
  try {
    const history = getHistory();
    const newItem = {
      ...item,
      created_at: new Date().toISOString(),
      id: Date.now().toString(),
    };

    // Add to beginning, keep last 10
    const updated = [newItem, ...history].slice(0, 10);
    localStorage.setItem(STORAGE_KEYS.HISTORY, JSON.stringify(updated));

    return newItem;
  } catch (error) {
    console.error('Error saving to history:', error);
    return null;
  }
};

export const clearHistory = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.HISTORY);
    return true;
  } catch (error) {
    console.error('Error clearing history:', error);
    return false;
  }
};

/**
 * Settings management
 */
export const getSettings = () => {
  try {
    const data = localStorage.getItem(STORAGE_KEYS.SETTINGS);
    return data ? { ...DEFAULT_SETTINGS, ...JSON.parse(data) } : DEFAULT_SETTINGS;
  } catch (error) {
    console.error('Error reading settings:', error);
    return DEFAULT_SETTINGS;
  }
};

export const saveSettings = (settings) => {
  try {
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(settings));
    return true;
  } catch (error) {
    console.error('Error saving settings:', error);
    return false;
  }
};

export const resetSettings = () => {
  try {
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(DEFAULT_SETTINGS));
    return DEFAULT_SETTINGS;
  } catch (error) {
    console.error('Error resetting settings:', error);
    return DEFAULT_SETTINGS;
  }
};
