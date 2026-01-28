"""
공통 베이스 모델
"""
import uuid
from django.db import models


class BaseModel(models.Model):
    """공통 베이스 모델 - 모든 도메인 모델의 부모"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        abstract = True
