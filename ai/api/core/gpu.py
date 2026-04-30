import torch

# ---------------------------
# GPU VRAM
# ---------------------------
def get_gpu_vram_gb():
    if not torch.cuda.is_available():
        return 0
    props = torch.cuda.get_device_properties(0)
    return round(props.total_memory / (1024 ** 3), 2)