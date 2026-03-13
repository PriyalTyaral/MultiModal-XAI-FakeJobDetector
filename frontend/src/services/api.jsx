import axios from 'axios';

const API_BASE = "http://localhost:8081/api";

export const analyzeJob = async (jobText) => {
  const response = await axios.post(
    `${API_BASE}/analyze`,
    jobText,
    {
      headers: {
        "Content-Type": "text/plain"
      }
    }
  );
  return response.data;
};

// ✅ ONLY CHANGE IS HERE
export const analyzeJobFile = async (file, fileType) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', fileType); // 🔥 REQUIRED FOR OCR

    const response = await axios.post(
      `${API_BASE}/analyze-file`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );

    return response.data;
  } catch (error) {
    console.error("File Upload Error:", error);
    throw error;
  }
};
