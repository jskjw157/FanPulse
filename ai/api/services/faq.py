"""FAQ (LLM)"""
from dataclasses import dataclass
import logging

from api.core.llm import get_llm

logger = logging.getLogger(__name__)


# ---------------------------
# 프롬프트
# ---------------------------
def faq_prompt_mockup(user_input: str) -> str:
    import json
    with open("api/tests/faq_mockup.json", "r", encoding="utf-8") as f:
        faq_dict = json.load(f)
    faq_mockup = "\n\n".join([f"{item['order']}. Q: {item['question']}\nA: {item['answer']}" for item in faq_dict])
    return f"""너는 FAQ 기반 고객 지원 챗봇이다.

[규칙]
- 반드시 아래 FAQ만 참고해서 답변해라.
- FAQ에 없는 내용은 절대 추측하지 말 것
- 없는 경우: "해당 내용은 현재 FAQ에 없습니다."라고 답변

[FAQ]
{faq_mockup}

[답변 방식]
1. 사용자 질문과 가장 유사한 FAQ를 선택
2. 해당 내용을 기반으로 자연스럽게 재작성
3. 필요하면 단계별로 정리

[사용자 질문]
{user_input}"""


def build_faq_rag_prompt(context: str, user_input: str) -> str:
    return f"""
너는 고객 상담 AI다.

아래 "검색 결과"만을 근거로 답변하셈.
검색 결과에 없는 내용은 절대 추측하지 마라.

---

[검색 결과]
{context}

---

[고객 질문]
{user_input}

---

규칙:
- 검색 결과에 없는 내용은 "관련 정보를 찾을 수 없습니다"라고 답해라
- 간결하고 친절하게 답변해라
- 반드시 한국어로 답변해라

[답변]
"""


# ---------------------------
# 결과 객체
# ---------------------------
@dataclass
class FAQResult:
    answer: str


# ---------------------------
# 서비스
# ---------------------------
class FaqService:
    def rag_search(self):
        pass

    def filter_comment(self, text: str) -> FAQResult:
        llm = get_llm()

        # prompt = build_faq_rag_prompt(text)
        prompt = faq_prompt_mockup(text)
        answer = llm.generate(prompt, max_new_tokens=256, do_sample=True)

        return FAQResult(answer=answer)

faq_service = FaqService()