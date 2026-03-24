import axios from 'axios';

// API Base URL
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

/**
 * Analyzes plain text job description.
 */
export const analyzeJob = async (jobText, domain = '', userId = '') => {
  let url = `${API_BASE}/analyze?`;
  if (domain) url += `domain=${encodeURIComponent(domain)}&`;
  if (userId) url += `userId=${encodeURIComponent(userId)}&`;
  const response = await axios.post(url, jobText, {
    headers: { 'Content-Type': 'text/plain' },
  });
  return response.data;
};

/**
 * Uploads a file (PDF, image, audio) for job analysis.
 */
export const analyzeJobFile = async (file, fileType, domain = '', userId = '') => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', fileType);
    if (domain) formData.append('domain', domain);
    if (userId) formData.append('userId', userId);

    const response = await axios.post(`${API_BASE}/analyze-file`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  } catch (error) {
    console.error('File Upload Error:', error);
    throw error;
  }
};

/**
 * Registers a new user.
 */
export const signupUser = async (name, email, password) => {
  const response = await axios.post(`${API_BASE}/users/signup`, { name, email, password });
  return response.data;
};

/**
 * Authenticates an existing user.
 */
export const signinUser = async (email, password) => {
  const response = await axios.post(`${API_BASE}/users/signin`, { email, password });
  return response.data;
};

/**
 * Fetches dashboard data for a user.
 */
export const getUserDashboard = async (userId) => {
  const response = await axios.get(`${API_BASE}/dashboard/${userId}`);
  return response.data;
};

/**
 * Analyzes a text job posting with configurable LIME explanation depth & format.
 *
 * @param {string} jobText      - The job description text.
 * @param {string} domain       - Optional domain/email for verification.
 * @param {string} userId       - Optional user ID.
 * @param {number} numFeatures  - Number of LIME feature words (default 10).
 * @param {string} format       - Output format: 'json' | 'visual' (default 'json').
 */
export const analyzeJobWithExplanation = async (
  jobText,
  domain = '',
  userId = '',
  numFeatures = 10,
  format = 'json'
) => {
  let url = `${API_BASE}/analyze?numFeatures=${numFeatures}&format=${format}`;
  if (domain) url += `&domain=${encodeURIComponent(domain)}`;
  if (userId) url += `&userId=${encodeURIComponent(userId)}`;

  const response = await axios.post(url, jobText, {
    headers: { 'Content-Type': 'text/plain' },
  });
  return response.data;
};

/**
 * Fetches a fresh LIME explanation for a given depth (ResultPage depth slider).
 * Does NOT re-run PMML prediction.
 *
 * @param {string} text         - Job description text.
 * @param {number} numFeatures  - Number of LIME features to return.
 * @param {string} format       - 'json' | 'visual'.
 * @returns {Promise<{lime_explanations, cache_status, explanation_latency_ms}>}
 */
export const getExplanation = async (text, numFeatures = 10, format = 'json') => {
  const response = await axios.get(`${API_BASE}/explain`, {
    params: { text, numFeatures, format },
  });
  return response.data;
};
