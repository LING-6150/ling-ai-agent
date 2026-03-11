from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import CrossEncoder
from typing import List

app = FastAPI()

model = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')

class RerankRequest(BaseModel):
    query: str
    documents: List[str]
    top_k: int = 3

class RerankResponse(BaseModel):
    reranked_documents: List[str]
    scores: List[float]

@app.post("/rerank", response_model=RerankResponse)
def rerank(request: RerankRequest):
    pairs = [(request.query, doc) for doc in request.documents]
    scores = model.predict(pairs).tolist()
    scored_docs = sorted(
        zip(scores, request.documents),
        key=lambda x: x[0],
        reverse=True
    )
    top_docs = scored_docs[:request.top_k]
    return RerankResponse(
        reranked_documents=[doc for _, doc in top_docs],
        scores=[score for score, _ in top_docs]
    )

@app.get("/health")
def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
