from flask import Flask, request, jsonify
import whisper
import os

app = Flask(__name__)
model = whisper.load_model("base")

@app.route("/transcribe", methods=["POST"])
def transcribe():
    file = request.files["file"]
    file_path = "temp_audio.mp3"
    file.save(file_path)

    result = model.transcribe(file_path)
    os.remove(file_path)

    return jsonify({"text": result["text"]})

if __name__ == "__main__":
    app.run(port=5001)