"""
Google Cloud Storage + Cloud Logging client for LIME service.
All methods are non-blocking — GCP failures are caught and logged locally.
If GCP credentials are missing or invalid, the service operates in OFFLINE mode.
"""

import os
import json
import logging

logger = logging.getLogger(__name__)

# Lazy imports — only loaded when GCP env vars are present
_storage_client = None
_logging_client = None
_gcp_bucket = None
_gcp_logger = None

GCP_PROJECT = os.environ.get("GCP_PROJECT_ID", "")
GCP_BUCKET = os.environ.get("GCP_BUCKET_NAME", "")
GCP_OFFLINE = not (GCP_PROJECT and GCP_BUCKET)


def _init_gcp():
    """Initialise GCP clients lazily and only once."""
    global _storage_client, _logging_client, _gcp_bucket, _gcp_logger

    if GCP_OFFLINE:
        logger.info("GCP_PROJECT_ID or GCP_BUCKET_NAME not set — running in OFFLINE mode.")
        return False

    try:
        from google.cloud import storage as gcs
        from google.cloud import logging as cloud_logging

        if _storage_client is None:
            _storage_client = gcs.Client(project=GCP_PROJECT)
            _gcp_bucket = _storage_client.bucket(GCP_BUCKET)
            logger.info("GCS client initialised for bucket: %s", GCP_BUCKET)

        if _logging_client is None:
            _logging_client = cloud_logging.Client(project=GCP_PROJECT)
            _gcp_logger = _logging_client.logger("lime-service")
            logger.info("Cloud Logging client initialised for project: %s", GCP_PROJECT)

        return True

    except Exception as exc:
        logger.warning("GCP initialisation failed (offline mode): %s", exc)
        return False


def upload_explanation(job_id: str, explanation: list, metadata: dict = None) -> str:
    """
    Upload a LIME explanation JSON to GCS.

    Returns:
        GCS blob URL string, or empty string on failure / offline mode.
    """
    if GCP_OFFLINE or not _init_gcp():
        logger.debug("GCP offline — skipping explanation upload for job_id=%s", job_id)
        return ""

    try:
        payload = {
            "job_id": job_id,
            "explanation": explanation,
            "metadata": metadata or {}
        }
        blob_name = f"explanations/{job_id}.json"
        blob = _gcp_bucket.blob(blob_name)
        blob.upload_from_string(
            json.dumps(payload, indent=2),
            content_type="application/json"
        )
        url = f"gs://{GCP_BUCKET}/{blob_name}"
        logger.info("Explanation uploaded to GCS: %s", url)
        return url
    except Exception as exc:
        logger.error("GCS upload failed for job_id=%s: %s", job_id, exc)
        return ""


def log_request(event_type: str, data: dict):
    """
    Write a structured log entry to Google Cloud Logging.
    Silently skips if in OFFLINE mode.
    """
    if GCP_OFFLINE or not _init_gcp():
        return

    try:
        _gcp_logger.log_struct({
            "event": event_type,
            **data
        }, severity="INFO")
    except Exception as exc:
        logger.warning("Cloud Logging write failed: %s", exc)


def is_online() -> bool:
    return not GCP_OFFLINE
