"""
Performance monitoring for the LIME microservice.
Tracks request counts, latency, errors, and uptime.
"""

import time
import logging
from threading import Lock
from collections import deque

logger = logging.getLogger(__name__)

_start_time = time.time()


class PerformanceMonitor:
    def __init__(self, window_size: int = 100):
        self._lock = Lock()
        self._request_count = 0
        self._error_count = 0
        self._latencies = deque(maxlen=window_size)  # rolling window

    def record_request(self, latency_ms: float, success: bool = True):
        with self._lock:
            self._request_count += 1
            self._latencies.append(latency_ms)
            if not success:
                self._error_count += 1

    def get_stats(self) -> dict:
        with self._lock:
            lats = list(self._latencies)
            avg_lat = round(sum(lats) / len(lats), 2) if lats else 0.0
            p95_lat = round(sorted(lats)[int(len(lats) * 0.95)], 2) if len(lats) >= 2 else avg_lat
            return {
                "uptime_seconds": round(time.time() - _start_time, 1),
                "total_requests": self._request_count,
                "error_count": self._error_count,
                "error_rate": round(self._error_count / max(self._request_count, 1), 3),
                "avg_latency_ms": avg_lat,
                "p95_latency_ms": p95_lat,
            }
