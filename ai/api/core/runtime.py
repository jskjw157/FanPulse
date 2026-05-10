from functools import lru_cache
import logging
import torch
logger = logging.getLogger(__name__)

def env_check():
    import sys
    logger.info("===== ENV CHECK =====")
    logger.info("Python exe : %s", sys.executable)
    logger.info("Torch ver  : %s", torch.__version__)
    logger.info("CUDA avail : %s", torch.cuda.is_available())
    logger.info("CUDA ver   : %s", torch.version.cuda)
    logger.info("GPU name   : %s", torch.cuda.get_device_name(0) if torch.cuda.is_available() else "NO GPU")
    logger.info("=====================")


# ---------------------------
# GPU VRAM
# ---------------------------
def get_gpu_vram_gb():
    if not torch.cuda.is_available():
        return 0
    props = torch.cuda.get_device_properties(0)
    return round(props.total_memory / (1024 ** 3), 2)


#######################
# Pipeline 지연 로딩
#######################
@lru_cache(maxsize=1)
def get_pipeline():
    """
    transformers의 pipeline 함수를 지연 로딩

    왜 지연 로딩?
    - transformers import 자체가 무거움 (수 초 소요)
    - AI 요약을 사용하지 않는 요청에서는 로드 불필요
    - 서버 시작 속도 향상

    Returns:
        pipeline 함수

    Raises:
        ImportError: transformers가 설치되지 않은 경우
    """
    try:
        from transformers import pipeline
        logger.info("Transformers pipeline loaded successfully")
        return pipeline
    except ImportError as e:
        logger.error(f"Failed to import transformers: {e}")
        raise ImportError(
            "transformers library is not installed. "
            "Please run: pip install transformers torch sentencepiece"
        )