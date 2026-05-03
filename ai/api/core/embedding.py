import numpy as np
from google import genai

client = genai.Client()

def get_embedding(text: str):
    result = client.models.embed_content(
        model="gemini-embedding-2",
        contents=text,
        config=genai.types.EmbedContentConfig(output_dimensionality=768)
    )

    return result.embeddings[0].values


# 사용자 쿼리
def prepare_query(query):
    return f"task: question answering | query: {query}"


# FAQ 추가
def prepare_document(q: str, a: str):
    return f"title: {q} | text: {a}"


if __name__ == "__main__":
    import json
    with open("api/tests/faq_mockup.json", "r", encoding="utf-8") as f:
        faq_dict = json.load(f)
    vectors = []
    for faq in faq_dict:
        qna = prepare_document(faq["question"], faq["answer"])
        vectors.append(get_embedding(qna))

    np.save("embedding.npy", vectors)