import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import messages from './local/index';

i18n
  .use(initReactI18next)
  .init({
    lng: 'ko',
    fallbackLng: 'en',
    supportedLngs: ['ko', 'en'],
    debug: false,
    resources: messages,
    react: { useSuspense: false },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
