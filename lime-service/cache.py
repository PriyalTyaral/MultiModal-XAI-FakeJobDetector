"""
In-memory LRU cache for LIME explanations.
Cache key = SHA256(text + str(num_features)) so same text with different depths
gets separate cache entries.
"""

import hashlib
import time
import logging
from cachetools import LRUCache
from threading import Lock

logger = logging.getLogger(__name__)


class ExplanationCache:
    def __init__(self, max_size: int = 500, ttl_seconds: int = 3600):
        self.ttl = ttl_seconds
        self._cache: LRUCache = LRUCache(maxsize=max_size)
        self._lock = Lock()
        self._hits = 0
        self._misses = 0
        self._total_requests = 0

    @staticmethod
    def make_key(text: str, num_features: int) -> str:
        raw = f"{text.strip()}:{num_features}"
        return hashlib.sha256(raw.encode("utf-8")).hexdigest()

    def get(self, key: str):
        """Return cached value if present and not expired, else None."""
        self._total_requests += 1
        with self._lock:
            entry = self._cache.get(key)
            if entry:
                data, ts = entry
                if time.time() - ts < self.ttl:
                    self._hits += 1
                    logger.debug("Cache HIT for key=%s", key[:12])
                    return data
                else:
                    # Expired — evict manually
                    del self._cache[key]
            self._misses += 1
            logger.debug("Cache MISS for key=%s", key[:12])
            return None

    def set(self, key: str, value):
        """Store a value with current timestamp."""
        with self._lock:
            self._cache[key] = (value, time.time())

    def stats(self) -> dict:
        return {
            "total_requests": self._total_requests,
            "hits": self._hits,
            "misses": self._misses,
            "hit_rate": round(self._hits / max(self._total_requests, 1), 3),
            "current_size": len(self._cache),
            "max_size": self._cache.maxsize,
            "ttl_seconds": self.ttl,
        }

    def clear(self):
        with self._lock:
            self._cache.clear()
            self._hits = 0
            self._misses = 0
            self._total_requests = 0
