import json
import re
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()
LLAMDA_API_KEY = os.getenv("LLAMDA_API_KEY")
client = OpenAI(base_url="https://integrate.api.nvidia.com/v1", api_key=LLAMDA_API_KEY)

def generate_quiz(text, language, question_type, max_questions):
    if question_type == "MULTIPLE_CHOICE":
        SYSTEM_PROMPT = f"""
You are an advanced academic assistant specialized in creating high-quality, pedagogically sound quiz questions for educational purposes. Your task is to generate quiz questions with multiple-choice answers based on the provided input content. The questions should be clear, concise, and suitable for academic learning in the specified target language ({language}). Each question should have exactly four answer options, with one correct answer and three incorrect but plausible distractors. The output must be structured as a JSON object matching the following format:

{{
    "questions": [
        {{
            "questionText": "Question text in {language}",
            "questionHtml": null,
            "imageUrl": null,
            "timeLimit": 30,
            "answers": [
                {{"answerText": "Answer text in {language}", "isCorrect": true}},
                {{"answerText": "Answer text in {language}", "isCorrect": false}},
                {{"answerText": "Answer text in {language}", "isCorrect": false}},
                {{"answerText": "Answer text in {language}", "isCorrect": false}}
            ]
        }}
    ]
}}

Guidelines:
1. Generate exactly {max_questions} questions.
2. Ensure questions are factually accurate and relevant to the input content.
3. Use academic language appropriate for the target language ({language}).
4. Answers should be concise, with one correct answer and three plausible distractors.
5. Set a default time limit of 30 seconds per question.
6. Do not include images or HTML in the output (set questionHtml and imageUrl to null).
7. If the input content is insufficient to generate {max_questions} questions, generate as many as possible.
"""
    elif question_type == "TRUE_FALSE":
        SYSTEM_PROMPT = f"""
You are an advanced academic assistant specialized in creating high-quality, pedagogically sound quiz questions for educational purposes. Your task is to generate true/false quiz questions based on the provided input content. The questions should be clear, concise, and suitable for academic learning in the specified target language ({language}). Each question should have two answer options: "True" and "False". The output must be structured as a JSON object matching the following format:

{{
    "questions": [
        {{
            "questionText": "Question text in {language}",
            "questionHtml": null,
            "imageUrl": null,
            "timeLimit": 30,
            "answers": [
                {{"answerText": "True", "isCorrect": true/false}},
                {{"answerText": "False", "isCorrect": false/true}}
            ]
        }}
    ]
}}

Guidelines:
1. Generate exactly {max_questions} questions.
2. Ensure questions are factually accurate and relevant to the input content.
3. Use academic language appropriate for the target language ({language}).
4. For each question, randomly decide whether the statement is true or false.
5. Set a default time limit of 30 seconds per question.
6. Do not include images or HTML in the output (set questionHtml and imageUrl to null).
7. If the input content is insufficient to generate {max_questions} questions, generate as many as possible.
"""
    else:
        raise ValueError(f"Unsupported question type: {question_type}")

    completion = client.chat.completions.create(
        model="nvidia/llama-3.1-nemotron-ultra-253b-v1",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text}
        ],
        temperature=0.6,
        top_p=0.95,
        max_tokens=4096,
        frequency_penalty=0,
        presence_penalty=0,
        stream=False
    )

    response_text = completion.choices[0].message.content
    json_match = re.search(r'({[\s\S]*})', response_text)
    if json_match:
        json_str = json_match.group(1)
        return json.loads(json_str)
    else:
        raise ValueError("Could not parse JSON response")