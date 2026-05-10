import numpy as np
from google import genai
from config.settings import GOOGLE_API_KEY
from google.genai.errors import ClientError

try:
    client = genai.Client(api_key=GOOGLE_API_KEY)
    client.models.list()
except ClientError:
    raise RuntimeError('.env 파일에 GOOGLE_API_KEY를 입력하세요.')


def _get_embedding(text: str):
    result = client.models.embed_content(
        model="gemini-embedding-2",
        contents=text,
        config=genai.types.EmbedContentConfig(output_dimensionality=768)
    )

    return result.embeddings[0].values


# 사용자 쿼리
def embed_query(query):
    return _get_embedding(f"task: question answering | query: {query}")


# FAQ 추가
def embed_document(q: str, a: str):
    return _get_embedding(f"title: {q} | text: {a}")


if __name__ == "__main__":
    import json
    with open("api/tests/faq_mockup.json", "r", encoding="utf-8") as f:
        faq_dict = json.load(f)
    vectors = []
    for faq in faq_dict:
        qna = embed_document(faq["question"], faq["answer"])
        vectors.append(qna)

    np.save("embedding.npy", vectors)