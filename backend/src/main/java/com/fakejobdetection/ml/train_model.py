import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn2pmml import sklearn2pmml, PMMLPipeline

# Example training data
data = pd.DataFrame({
    "keyword_score": [0.9, 0.1, 0.8, 0.2],
    "sentence_count": [2, 6, 3, 8],
    "text_length": [120, 800, 150, 1000],
    "urgent_flag": [1, 0, 1, 0],
    "no_interview_flag": [1, 0, 1, 0],
    "label": [1, 0, 1, 0]  # 1 = FAKE, 0 = REAL
})

X = data.drop("label", axis=1)
y = data["label"]

pipeline = PMMLPipeline([
    ("scaler", StandardScaler()),
    ("model", RandomForestClassifier(n_estimators=100, random_state=42))
])

pipeline.fit(X, y)

sklearn2pmml(pipeline, "fake_job_model.pmml", with_repr=True)
print("PMML model generated successfully")
