'''
backend\src\main\resources\db\migration\V100__seed_faq.sql랑
ai\sql\002_more_faq.sql 이후에
pgvector로 임베딩된 벡터를 faq 테이블에 업데이트
'''
import psycopg2
import numpy as np

from config.settings import DATABASES

db = DATABASES['default']

# 미리 임베딩한 벡터
vectors = np.load("api/tests/faq_embedding.npy", allow_pickle=True)

conn = psycopg2.connect(
    dbname='fanpulse',
    user=db.get('USER'),
    password=db.get('PASSWORD', 'postgres'),
    host=db.get('HOST'),
    port=db.get('PORT')
)

cur = conn.cursor()
cur.execute('SELECT display_order, question, answer FROM faq')
rows = cur.fetchall()

for row in rows:
    id, question, answer = row
    cur.execute(
        "UPDATE faq SET embedding = %s WHERE display_order = %s",
        (vectors[id - 1].tolist(), id)
    )
    print(f"ID {id} 업데이트 완료")

conn.commit()
cur.close()
conn.close()