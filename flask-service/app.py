import os
import platform
from flask import Flask, request, jsonify
import pytesseract
from utils.text_extraction import extract_text
from utils.quiz_generation import generate_quiz

# pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'
# Cấu hình đường dẫn Tesseract linh hoạt
if platform.system() == "Windows":
    # Đường dẫn mặc định trên Windows, có thể ghi đè bằng biến môi trường
    tesseract_cmd = os.getenv("TESSERACT_CMD", r"C:\Program Files\Tesseract-OCR\tesseract.exe")
    pytesseract.pytesseract.tesseract_cmd = tesseract_cmd
else:
    # Trên Linux (Docker), Tesseract thường có sẵn trong PATH
    pytesseract.pytesseract.tesseract_cmd = os.getenv("TESSERACT_CMD", "tesseract")

app = Flask(__name__)

@app.route("/generate-quiz", methods=["POST"])
def generate_quiz_endpoint():
    language = request.args.get("language", "en")
    source_type = request.args.get("sourceType", "TEXT").upper()
    question_type = request.args.get("questionType", "MULTIPLE_CHOICE").upper()
    max_questions = request.args.get("maxQuestions", 5, type=int)

    if "file" not in request.files:
        return jsonify({"error": "No file provided"}), 400

    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No file selected"}), 400

    try:
        text = extract_text(file, source_type)
        if not text.strip():
            return jsonify({"questions": []}), 200

        quiz_data = generate_quiz(text, language, question_type, max_questions)
        return jsonify(quiz_data)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
