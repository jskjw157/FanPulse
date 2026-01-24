"""
#######################
# 규칙 기반 요약 서비스
#######################
# 이 파일은 단어 빈도 기반의 추출형(Extractive) 요약을 수행합니다.
#
# 요약 방식: 추출형 (Extractive Summarization)
# - 원문에서 중요한 문장을 그대로 추출하여 요약 생성
# - AI 모델 없이 알고리즘만으로 동작 (빠르고 가벼움)
#
# 알고리즘 흐름:
# 1. 텍스트를 문장 단위로 분리
# 2. 단어 빈도수 계산 (자주 나오는 단어 = 중요한 단어)
# 3. 각 문장의 점수 계산 (중요 단어가 많은 문장 = 높은 점수)
# 4. 점수가 높은 문장들을 선택하여 요약 구성
# 5. 키워드와 핵심 포인트 추출
#######################
"""
import logging
import re
from collections import Counter

logger = logging.getLogger(__name__)


#######################
# 규칙 기반 요약기 클래스
#######################
class ArticleSummarizer:
    """
    규칙 기반 추출형 요약기

    특징:
    - AI 모델 불필요 (빠른 처리 속도)
    - 단어 빈도 기반으로 중요 문장 추출
    - 한국어/영어 지원

    사용법:
        summarizer = ArticleSummarizer(language='ko')
        result = summarizer.summarize(text, max_length=200, min_length=50)
    """

    def __init__(self, language='ko'):
        """
        요약기 초기화

        Args:
            language: 텍스트 언어 ('ko': 한국어, 'en': 영어)
        """
        self.language = language

    #######################
    # 메인 요약 함수
    #######################
    def summarize(self, text, max_length=200, min_length=50):
        """
        텍스트 요약 수행

        Args:
            text: 요약할 원문 텍스트
            max_length: 요약 최대 길이 (글자 수)
            min_length: 요약 최소 길이 (글자 수)

        Returns:
            dict: {
                'summary': 요약된 텍스트,
                'bullets': 핵심 포인트 목록 (최대 6개),
                'keywords': 추출된 키워드 목록 (최대 10개)
            }
        """
        return self._summarize_rule_based(text, max_length, min_length)

    #######################
    # 규칙 기반 요약 로직
    #######################
    def _summarize_rule_based(self, text, max_length, min_length):
        """
        규칙 기반 추출형 요약 수행

        처리 과정:
        1. 텍스트 정제 (공백 정리 등)
        2. 문장 분리
        3. 단어 빈도 계산
        4. 문장 점수 계산
        5. 상위 점수 문장 선택
        6. 핵심 포인트 및 키워드 추출
        """

        #######################
        # 1단계: 텍스트 정제
        #######################
        text = self._clean_text(text)

        #######################
        # 2단계: 문장 분리
        #######################
        sentences = self._split_sentences(text)

        # 문장이 없으면 원문 일부 반환
        if not sentences:
            return {
                'summary': text[:max_length],
                'bullets': ['내용 분석 실패'],
                'keywords': []
            }

        #######################
        # 3단계: 단어 빈도 계산
        #######################
        word_freq = self._calculate_word_frequency(text)

        #######################
        # 4단계: 문장 점수 계산
        #######################
        sentence_scores = self._score_sentences(sentences, word_freq)

        #######################
        # 5단계: 상위 문장 선택
        #######################
        summary_sentences = self._select_top_sentences(
            sentences,
            sentence_scores,
            max_length
        )

        # 선택된 문장들을 연결하여 요약 생성
        summary = ' '.join(summary_sentences)

        # 최소 길이 보장: 요약이 너무 짧으면 문장 추가
        if len(summary) < min_length and len(sentences) > len(summary_sentences):
            remaining = [s for s in sentences if s not in summary_sentences]
            for sent in remaining:
                if len(summary) + len(sent) <= max_length:
                    summary += ' ' + sent
                if len(summary) >= min_length:
                    break

        #######################
        # 6단계: 핵심 포인트 추출
        #######################
        bullets = self._extract_bullets(sentences, sentence_scores)

        #######################
        # 7단계: 키워드 추출
        #######################
        keywords = self._extract_keywords(text)

        return {
            'summary': summary.strip(),
            'bullets': bullets,
            'keywords': keywords
        }

    #######################
    # 텍스트 정제
    #######################
    def _clean_text(self, text):
        """
        텍스트 정규화 및 정제

        처리 내용:
        - 연속된 공백을 하나로 통합
        - 앞뒤 공백 제거
        """
        text = re.sub(r'\s+', ' ', text)  # 연속 공백 → 단일 공백
        return text.strip()

    #######################
    # 문장 분리
    #######################
    def _split_sentences(self, text):
        """
        텍스트를 문장 단위로 분리

        언어별 처리:
        - 한국어: 마침표, 느낌표, 물음표 기준 분리
        - 영어: 문장 부호 뒤 공백 기준 분리
        """
        if self.language == 'ko':
            # 한국어: 문장 부호 + 공백으로 분리
            sentences = re.split(r'[.!?]\s+', text)
        else:
            # 영어: 문장 부호 뒤 공백으로 분리 (부호 유지)
            sentences = re.split(r'(?<=[.!?])\s+', text)

        # 너무 짧은 문장 제거 (10자 미만)
        sentences = [s.strip() for s in sentences if len(s.strip()) > 10]
        return sentences

    #######################
    # 단어 빈도 계산
    #######################
    def _calculate_word_frequency(self, text):
        """
        단어 빈도수 계산

        처리 과정:
        1. 텍스트에서 단어 추출
        2. 불용어(stop words) 제거
        3. 빈도수 계산
        4. 최대 빈도 기준 정규화 (0~1 범위)

        Returns:
            dict: {단어: 정규화된_빈도} 형태의 딕셔너리
        """
        # 단어 추출 (영문자, 한글 등)
        words = re.findall(r'\b\w+\b', text.lower())

        #######################
        # 불용어 정의
        #######################
        # 불용어: 의미 없이 자주 등장하는 단어 (조사, 관사 등)
        stop_words = set([
            # 영어 불용어
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            # 한국어 불용어 (조사)
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도'
        ])

        # 불용어 및 1글자 단어 제거
        words = [w for w in words if w not in stop_words and len(w) > 1]

        # 빈도수 계산
        freq = Counter(words)

        # 정규화 (최대 빈도 = 1.0)
        max_freq = max(freq.values()) if freq else 1
        for word in freq:
            freq[word] = freq[word] / max_freq

        return freq

    #######################
    # 문장 점수 계산
    #######################
    def _score_sentences(self, sentences, word_freq):
        """
        각 문장의 중요도 점수 계산

        점수 계산 방식:
        - 문장에 포함된 단어들의 빈도 점수 합산
        - 문장 길이로 정규화 (긴 문장에 불이익 방지)

        Args:
            sentences: 문장 리스트
            word_freq: 단어별 빈도 점수 딕셔너리

        Returns:
            list: 각 문장의 점수 리스트
        """
        scores = []

        for sentence in sentences:
            words = re.findall(r'\b\w+\b', sentence.lower())
            # 문장 내 단어들의 빈도 점수 합산
            score = sum(word_freq.get(word, 0) for word in words)

            # 문장 길이로 정규화
            if len(words) > 0:
                score = score / len(words)

            scores.append(score)

        return scores

    #######################
    # 상위 문장 선택
    #######################
    def _select_top_sentences(self, sentences, scores, max_length):
        """
        점수가 높은 문장들을 선택하여 요약 구성

        선택 기준:
        1. 점수가 높은 순서로 정렬
        2. max_length를 초과하지 않는 범위에서 선택
        3. 원문에서의 순서대로 재정렬 (자연스러운 흐름 유지)

        Args:
            sentences: 전체 문장 리스트
            scores: 문장별 점수 리스트
            max_length: 요약 최대 길이

        Returns:
            list: 선택된 문장 리스트 (원문 순서)
        """
        # 점수 기준 내림차순 정렬
        ranked = sorted(zip(sentences, scores), key=lambda x: x[1], reverse=True)

        selected = []
        total_length = 0

        # 점수 높은 순서로 문장 선택
        for sentence, score in ranked:
            if total_length + len(sentence) <= max_length:
                selected.append(sentence)
                total_length += len(sentence) + 1  # +1은 공백

            # 최대 길이의 90% 도달 시 중단
            if total_length >= max_length * 0.9:
                break

        # 원문 순서대로 재정렬
        result = []
        for sent in sentences:
            if sent in selected:
                result.append(sent)

        return result

    #######################
    # 핵심 포인트 추출
    #######################
    def _extract_bullets(self, sentences, scores=None):
        """
        핵심 포인트(불릿) 추출

        추출 방식:
        - 점수가 있으면: 상위 5개 문장 선택
        - 점수가 없으면: 처음 5개 문장 선택

        Args:
            sentences: 문장 리스트
            scores: 문장별 점수 리스트 (선택)

        Returns:
            list: 핵심 포인트 문장 리스트 (최대 6개)
        """
        if scores:
            # 점수 기준 상위 5개 선택
            ranked = sorted(zip(sentences, scores), key=lambda x: x[1], reverse=True)
            bullets = [sent for sent, score in ranked[:5]]
        else:
            # 처음 5개 문장 선택
            bullets = sentences[:5]

        # 긴 문장 자르기 (100자 초과 시)
        bullets = [b[:100] + '...' if len(b) > 100 else b for b in bullets]

        return bullets[:6]  # 최대 6개 반환

    #######################
    # 키워드 추출
    #######################
    def _extract_keywords(self, text):
        """
        텍스트에서 주요 키워드 추출

        추출 방식:
        - 단어 빈도수 기반
        - 불용어 제거
        - 상위 10개 단어 반환

        Args:
            text: 원문 텍스트

        Returns:
            list: 키워드 리스트 (최대 10개)
        """
        words = re.findall(r'\b\w+\b', text.lower())

        #######################
        # 확장된 불용어 목록
        #######################
        stop_words = set([
            # 영어 불용어
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'this', 'that', 'with', 'from', 'by', 'as', 'be', 'have', 'has',
            # 한국어 불용어
            '은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도',
            '있다', '없다', '하다', '되다', '이다'
        ])

        # 불용어 및 2글자 이하 단어 제거
        words = [w for w in words if w not in stop_words and len(w) > 2]

        # 빈도수 기준 상위 10개 추출
        freq = Counter(words)
        keywords = [word for word, count in freq.most_common(10)]

        return keywords
