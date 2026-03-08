"""
Pytest configuration for Django API tests.
Forces SQLite for test isolation (no PostgreSQL dependency).
Mocks heavy ML dependencies (torch, transformers, etc.) that aren't needed for API tests.
"""
import os
import sys
from unittest.mock import MagicMock

# Force SQLite for tests regardless of .env
os.environ["USE_POSTGRES"] = "false"
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")

# Mock heavy ML modules before Django loads views → services → torch
_MOCK_MODULES = [
    "torch", "torch.cuda", "torch.nn", "torch.utils",
    "transformers", "transformers.pipelines",
    "bitsandbytes",
    "accelerate",
    "sentencepiece",
    "tokenizers",
]
for mod in _MOCK_MODULES:
    sys.modules.setdefault(mod, MagicMock())

# Fix dunder attributes that MagicMock doesn't handle well
sys.modules["torch"].__version__ = "2.1.0"
sys.modules["torch"].cuda.is_available.return_value = False
sys.modules["transformers"].__version__ = "4.36.0"

import django
django.setup()
