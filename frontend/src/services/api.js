export async function analyzeJob(text) {
  const response = await fetch("http://localhost:8080/predict", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ text })
  });

  if (!response.ok) {
    throw new Error("Failed to analyze job");
  }

  return response.json();
}

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