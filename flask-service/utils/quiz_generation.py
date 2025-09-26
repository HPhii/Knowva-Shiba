import json
import re
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()
GOOGLE_GEMINI_API_KEY = os.getenv("GOOGLE_GEMINI_API_KEY")  # For generate_quiz
client = OpenAI(base_url="https://generativelanguage.googleapis.com/v1beta/openai/", api_key=GOOGLE_GEMINI_API_KEY)


def _get_system_prompt(language, question_type, max_questions):
    """A helper function to generate the appropriate system prompt."""

    # --- SHARED PRINCIPLES ---
    base_prompt = f"""
You are a master academic assistant and an expert in pedagogy. Your task is to create a high-quality quiz in **{language}** based on the provided text. The goal is to create questions that test deep understanding, not just simple recall.

**Core Pedagogical Principles:**
1.  **Test Understanding, Not Just Memory:** Questions should require the user to apply, analyze, or evaluate information (e.g., "Why...", "What is the main advantage of...").
2.  **Clarity and Precision:** Questions must be unambiguous.
3.  **Plausible Distractors:** For multiple-choice questions, the incorrect options (distractors) should be realistic and address common misconceptions.
4.  **Relevance:** All questions must be directly answerable from the provided text.

**Your Task:**
-   Generate exactly **{max_questions}** questions.
-   The output must be a single, valid JSON object. Do NOT include any text outside the JSON.
-   If the input text is insufficient, return an empty list: `{{"questions": []}}`
"""

    # --- TYPE-SPECIFIC INSTRUCTIONS AND EXAMPLES ---
    if question_type == "MULTIPLE_CHOICE":
        return base_prompt + f"""
**Instructions for Multiple Choice:**
-   Each question must have exactly one correct answer and three plausible, incorrect distractors.
-   Distractors should be common errors or related but incorrect concepts from the text.

---
**Example:**

* **Input Text:** "Trong quang hợp, thực vật sử dụng năng lượng ánh sáng mặt trời, nước và carbon dioxide để tạo ra glucose (năng lượng) và oxy. Diệp lục là sắc tố màu xanh lá cây giúp hấp thụ năng lượng ánh sáng."
* **Your JSON Output:**
    ```json
    {{
      "questions": [
        {{
          "questionText": "Đâu là vai trò chính của diệp lục trong quá trình quang hợp?",
          "questionHtml": null,
          "imageUrl": null,
          "timeLimit": 30,
          "answers": [
            {{"answerText": "Hấp thụ năng lượng ánh sáng", "isCorrect": true}},
            {{"answerText": "Tạo ra khí oxy", "isCorrect": false}},
            {{"answerText": "Tổng hợp nước và carbon dioxide", "isCorrect": false}},
            {{"answerText": "Làm cho lá có màu xanh", "isCorrect": false}}
          ]
        }}
      ]
    }}
    ```
---
"""
    elif question_type == "TRUE_FALSE":
        return base_prompt + f"""
**Instructions for True/False:**
-   Create a statement that is either true or false based *directly* on the text.
-   The statement should test a nuanced point, not just an obvious fact.

---
**Example:**

* **Input Text:** "Dù Sao Hỏa được gọi là 'Hành tinh Đỏ', bầu trời của nó vào ban ngày lại có màu hồng nhạt, và hoàng hôn lại có màu xanh lam."
* **Your JSON Output:**
    ```json
    {{
      "questions": [
        {{
          "questionText": "Trên Sao Hỏa, hoàng hôn có màu đỏ tương tự như màu của hành tinh.",
          "questionHtml": null,
          "imageUrl": null,
          "timeLimit": 30,
          "answers": [
            {{"answerText": "True", "isCorrect": false}},
            {{"answerText": "False", "isCorrect": true}}
          ]
        }}
      ]
    }}
    ```
---
"""
    elif question_type == "MIXED":
        # For MIXED, we combine the instructions and provide both examples.
        return base_prompt + f"""
**Instructions for Mixed Types:**
-   Generate a balanced mix of Multiple Choice and True/False questions.
-   For Multiple Choice, provide one correct answer and three plausible distractors.
-   For True/False, create a nuanced statement that is either true or false.

---
**Example 1 (Multiple Choice):**

* **Input Text:** "Trong quang hợp, thực vật sử dụng năng lượng ánh sáng mặt trời, nước và carbon dioxide để tạo ra glucose (năng lượng) và oxy. Diệp lục là sắc tố màu xanh lá cây giúp hấp thụ năng lượng ánh sáng."
* **Your JSON Output for this part:**
    ```json
    {{
      "questionText": "Đâu là vai trò chính của diệp lục trong quá trình quang hợp?",
      "answers": [
        {{"answerText": "Hấp thụ năng lượng ánh sáng", "isCorrect": true}},
        {{"answerText": "Tạo ra khí oxy", "isCorrect": false}},
        {{"answerText": "Tổng hợp nước và carbon dioxide", "isCorrect": false}},
        {{"answerText": "Làm cho lá có màu xanh", "isCorrect": false}}
      ]
    }}
    ```

**Example 2 (True/False):**

* **Input Text:** "Dù Sao Hỏa được gọi là 'Hành tinh Đỏ', bầu trời của nó vào ban ngày lại có màu hồng nhạt, và hoàng hôn lại có màu xanh lam."
* **Your JSON Output for this part:**
    ```json
    {{
      "questionText": "Trên Sao Hỏa, hoàng hôn có màu đỏ tương tự như màu của hành tinh.",
      "answers": [
        {{"answerText": "True", "isCorrect": false}},
        {{"answerText": "False", "isCorrect": true}}
      ]
    }}
    ```
---
"""
    else:
        raise ValueError(f"Unsupported question type: {question_type}")


def generate_quiz(text, language, question_type, max_questions):
    SYSTEM_PROMPT = _get_system_prompt(language, question_type.upper(), max_questions)

    completion = client.chat.completions.create(
        model="models/gemini-2.5-flash-lite",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text}
        ],
        temperature=0.5,
        top_p=0.95,
        stream=False
    )

    response_text = completion.choices[0].message.content
    json_match = re.search(r'({[\s\S]*})', response_text)
    if json_match:
        json_str = json_match.group(1)
        return json.loads(json_str)
    else:
        raise ValueError("Could not parse JSON response")


def generate_quiz_from_flashcards(flashcards, language, question_type, max_questions):
    text = "\n".join([f"Front: {fc['front']}\nBack: {fc['back']}" for fc in flashcards])
    SYSTEM_PROMPT = _get_system_prompt(language, question_type.upper(), max_questions)

    completion = client.chat.completions.create(
        model="models/gemini-2.5-flash-lite",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text}
        ],
        temperature=0.5,
        top_p=0.95,
        stream=False
    )

    response_text = completion.choices[0].message.content
    json_match = re.search(r'({[\s\S]*})', response_text)
    if json_match:
        json_str = json_match.group(1)
        return json.loads(json_str)
    else:
        raise ValueError("Could not parse JSON response")