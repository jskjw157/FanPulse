const modules = import.meta.glob('./*/*.ts', { eager: true });

const messages: Record<string, { translation: Record<string, string> }> = {};

Object.keys(modules).forEach((path) => {
  const match = path.match(/\.\/([^/]+)\/([^/]+)\.ts$/);
  if (match) {
    const [, lang] = match;
    const langModule = modules[path] as { default?: Record<string, string> };

    if (!messages[lang]) {
      messages[lang] = { translation: {} };
    }

    // 合并翻译内容
    if (langModule.default) {
      messages[lang].translation = {
        ...messages[lang].translation,
        ...langModule.default
      };
    }
  }
});

export default messages; 