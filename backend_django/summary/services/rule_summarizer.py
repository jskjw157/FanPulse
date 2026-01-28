"""
규칙 기반 요약 서비스

단어 빈도 기반의 추출형(Extractive) 요약
AI 모델 없이 알고리즘만으로 동작
"""
import logging
import re
from collections import Counter

logger = logging.getLogger(__name__)


class ArticleSummarizer:
    """규칙 기반 추출형 요약기"""

    def __init__(self, language='ko'):
        self.language = language

    def summarize(self, text, max_length=200, min_length=50):
        """텍스트 요약 수행"""
        return self._summarize_rule_based(text, max_length, min_length)

    def _summarize_rule_based(self, text, max_length, min_length):
        text = self._clean_text(text)
        sentences = self._split_sentences(text)

        if not sentences:
            return {
                'summary': text[:max_length],
                'bullets': ['내용 분석 실패'],
                'keywords': []
            }

        word_freq = self._calculate_word_frequency(text)
        sentence_scores = self._score_sentences(sentences, word_freq)
        summary_sentences = self._select_top_sentences(sentences, sentence_scores, max_length)

        summary = ' '.join(summary_sentences)

        if len(summary) < min_length and len(sentences) > len(summary_sentences):
            remaining = [s for s in sentences if s not in summary_sentences]
            for sent in remaining:
                if len(summary) + len(sent) <= max_length:
                    summary += ' ' + sent
                if len(summary) >= min_length:
                    break

        bullets = self._extract_bullets(sentences, sentence_scores)
        keywords = self._extract_keywords(text)

        return {
            'summary': summary.strip(),
            'bullets': bullets,
            'keywords': keywords
        }

    def _clean_text(self, text):
        text = re.sub(r'\s+', ' ', text)
        return text.strip()

    def _split_sentences(self, text):
        if self.language == 'ko':
            sentences = re.split(r'[.!?]\s+', text)
        else:
            sentences = re.split(r'(?<=[.!?])\s+', text)

        sentences = [s.strip() for s in sentences if len(s.strip()) > 10]
        return sentences

    def _calculate_word_frequency(self, text):
        words = re.findall(r'\b\w+\b', text.lower())

        stop_words = set([
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도'
        ])

        words = [w for w in words if w not in stop_words and len(w) > 1]
        freq = Counter(words)

        max_freq = max(freq.values()) if freq else 1
        for word in freq:
            freq[word] = freq[word] / max_freq

        return freq

    def _score_sentences(self, sentences, word_freq):
        scores = []
        for sentence in sentences:
            words = re.findall(r'\b\w+\b', sentence.lower())
            score = sum(word_freq.get(word, 0) for word in words)
            if len(words) > 0:
                score = score / len(words)
            scores.append(score)
        return scores

    def _select_top_sentences(self, sentences, scores, max_length):
        ranked = sorted(zip(sentences, scores), key=lambda x: x[1], reverse=True)

        selected = []
        total_length = 0
        for sentence, score in ranked:
            if total_length + len(sentence) <= max_length:
                selected.append(sentence)
                total_length += len(sentence) + 1
            if total_length >= max_length * 0.9:
                break

        result = []
        for sent in sentences:
            if sent in selected:
                result.append(sent)
        return result

    def _extract_bullets(self, sentences, scores=None):
        if scores:
            ranked = sorted(zip(sentences, scores), key=lambda x: x[1], reverse=True)
            bullets = [sent for sent, score in ranked[:5]]
        else:
            bullets = sentences[:5]

        bullets = [b[:100] + '...' if len(b) > 100 else b for b in bullets]
        return bullets[:6]

    def _extract_keywords(self, text):
        words = re.findall(r'\b\w+\b', text.lower())

        stop_words = set([
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'this', 'that', 'with', 'from', 'by', 'as', 'be', 'have', 'has',
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도',
            '있다', '없다', '하다', '되다', '이다'
        ])

        words = [w for w in words if w not in stop_words and len(w) > 2]
        freq = Counter(words)
        return [word for word, count in freq.most_common(10)]
