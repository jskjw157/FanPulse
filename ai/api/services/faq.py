"""FAQ (LLM)"""
from dataclasses import dataclass
import torch

from api.core.llm import get_model
from api.core.embedding import embed_query
from api.models import FAQDocument

from config.settings import USE_POSTGRES

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

[FAQ 검색 결과]
{context}

---

[고객 질문]
{user_input}

---

규칙:
- 검색 결과가 없으면 "관련 정보를 찾을 수 없습니다"라고 답해라
- 간결하고 친절하게 답변해라
- 반드시 한국어로 답변해라

[답변]
"""


def search_faq(user_text: str) -> str:
    vector = embed_query(user_text)
    searched = FAQDocument.similarity_search(vector, 1)
    if not searched:
        return "없음"
    return f"Q: {searched[0].question}\nA: {searched[0].answer}"


# ---------------------------
# 결과 객체
# ---------------------------
@dataclass
class FAQResult:
    faq: str
    answer: str


# ---------------------------
# 서비스
# ---------------------------
class FaqService:
    def __init__(self):
        self._bundle = None

    def _ensure_model(self):
        if self._bundle is None:
            self._bundle = get_model()  # LLM bundle(dict) 반환
        return self._bundle

    def answer_user(self, user_text: str) -> FAQResult:
        bundle = self._ensure_model()
        tokenizer = bundle["tokenizer"]
        model = bundle["model"]

        if USE_POSTGRES:
            faq = search_faq(user_text)
            prompt = build_faq_rag_prompt(faq, user_text)
        else:   # PostgreSQL, pgvector, GOOGLE_API_KEY 준비 안 돼있고 LLM만 테스트할 때
            prompt = faq_prompt_mockup(user_text)

        inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=256,
                do_sample=True,
                pad_token_id=tokenizer.eos_token_id,
            )

        answer = tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[-1]:],
            skip_special_tokens=True,
        ).strip()

        return FAQResult(faq=faq, answer=answer)

faq_service = FaqService()