import json
import re
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()
LLAMDA_API_KEY3 = os.getenv("LLAMDA_API_KEY3")  # For exam feedback
client3 = OpenAI(base_url="https://integrate.api.nvidia.com/v1", api_key=LLAMDA_API_KEY3)

def generate_exam_feedback(correct_answer, user_answer):
    SYSTEM_PROMPT = """
You are an advanced academic assistant specialized in grading subjective answers and providing constructive feedback. Your task is to compare the user's answer with the correct answer and provide a score along with feedback on what was incorrect and what could have been included. The output should be in JSON format:

{
    "score": <float between 0 and 100>,
    "feedback": {
        "whatWasIncorrect": "<explanation of incorrect parts>",
        "whatCouldHaveIncluded": "<suggestions for improvement>"
    }
}

Guidelines:
1. Be fair and objective in scoring.
2. Provide clear and concise feedback.
3. Highlight specific mistakes and suggest improvements.
"""

    user_message = f"Correct Answer: {correct_answer}\nUser Answer: {user_answer}"

    completion = client3.chat.completions.create(
        model="nvidia/llama-3.1-nemotron-ultra-253b-v1",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_message}
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