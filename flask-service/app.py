from flask import Flask, request, jsonify
import pytesseract
from utils.text_extraction import extract_text
from utils.quiz_generation import generate_quiz, generate_quiz_from_flashcards
from utils.flashcard_generation import generate_flashcards
from utils.exam_feedback import generate_exam_feedback
from concurrent.futures import ThreadPoolExecutor

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

app = Flask(__name__)
MAX_FILES = 5

@app.route("/generate-quiz", methods=["POST"])
def generate_quiz_endpoint():
    language = request.args.get("language", "en")
    source_type = request.args.get("sourceType", "TEXT").upper()
    question_type = request.args.get("questionType", "MULTIPLE_CHOICE").upper()
    max_questions = request.args.get("maxQuestions", 5, type=int)

    try:
        if "text" in request.form and request.form["text"].strip():
            text = request.form["text"]
        elif "files" in request.files:
            files = request.files.getlist("files")
            if len(files) > MAX_FILES:
                return jsonify({"error": f"Too many files. Maximum is {MAX_FILES}"}), 400
            if not files:
                return jsonify({"error": "No files provided"}), 400
            with ThreadPoolExecutor(max_workers=4) as executor:
                extracted_texts = list(executor.map(lambda file: extract_text(file, source_type), files))
            text = "\n".join(filter(None, extracted_texts))
        else:
            return jsonify({"error": "No files or text input provided"}), 400

        if not text.strip():
            return jsonify({"questions": []}), 200

        quiz_data = generate_quiz(text, language, question_type, max_questions)
        return jsonify(quiz_data)

    except ValueError as ve:
        return jsonify({"error": str(ve)}), 400
    except Exception as e:
        app.logger.error(f"Error in generate_quiz_endpoint: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500

@app.route("/generate-flashcards", methods=["POST"])
def generate_flashcards_endpoint():
    language = request.args.get("language", "en")
    source_type = request.args.get("sourceType", "TEXT").upper()
    card_type = request.args.get("cardType", "STANDARD").upper()
    max_flashcards = request.args.get("maxFlashcards", 5, type=int)

    try:
        app.logger.info(f"Received request: source_type={source_type}, card_type={card_type}, max_flashcards={max_flashcards}")
        if "text" in request.form and request.form["text"].strip():
            text = request.form["text"]
        elif "files" in request.files:
            files = request.files.getlist("files")
            if not files:
                return jsonify({"error": "No files provided"}), 400
            if len(files) > MAX_FILES:
                return jsonify({"error": f"Too many files. Maximum is {MAX_FILES}"}), 400
            with ThreadPoolExecutor(max_workers=4) as executor:
                extracted_texts = list(executor.map(lambda file: extract_text(file, source_type), files))
            text = "\n".join(filter(None, extracted_texts))
        else:
            return jsonify({"error": "No files or text input provided"}), 400

        if not text.strip():
            return jsonify({"flashcards": []}), 200

        flashcards_data = generate_flashcards(text, language, card_type, max_flashcards)
        return jsonify(flashcards_data)

    except ValueError as ve:
        app.logger.warning(f"Client error: {str(ve)}")
        return jsonify({"error": str(ve)}), 400
    except Exception as e:
        app.logger.error(f"Server error: {str(e)}", exc_info=True)
        return jsonify({"error": "Internal server error"}), 500

@app.route("/exam-mode-grade", methods=["POST"])
def exam_mode_grade():
    data = request.json
    correct_answer = data.get("correctAnswer")
    user_answer = data.get("userAnswer")

    if not correct_answer or not user_answer:
        return jsonify({"error": "Both correctAnswer and userAnswer are required"}), 400

    try:
        feedback = generate_exam_feedback(correct_answer, user_answer)
        return jsonify(feedback)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/generate-quiz-from-flashcards", methods=["POST"])
def generate_quiz_from_flashcards_endpoint():
    data = request.json
    flashcards = data.get("flashcards")
    language = request.args.get("language", "en")
    question_type = request.args.get("questionType", "MULTIPLE_CHOICE").upper()
    max_questions = request.args.get("maxQuestions", 5, type=int)

    if not flashcards:
        return jsonify({"error": "Flashcards are required"}), 400

    try:
        quiz_data = generate_quiz_from_flashcards(flashcards, language, question_type, max_questions)
        return jsonify(quiz_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)